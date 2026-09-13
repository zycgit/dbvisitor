/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic;

import java.io.InputStream;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.AdapterCursor;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

/** Native metrics and exact, paged composite aggregation results. */
final class ElasticCommandsForAggregation extends ElasticCommands {
    private static final int DEFAULT_BUCKET_PAGE_SIZE = 100;

    static boolean supports(ElasticOperation operation, Object body) {
        return operation.getHints().containsKey("aggregation_group")
                || body instanceof Map && (((Map<?, ?>) body).containsKey("aggs") || ((Map<?, ?>) body).containsKey("aggregations"));
    }

    static Future<?> execAggregation(Future<Object> sync, ElasticCmd cmd, ElasticOperation operation,
            Object body, AdapterReceive receive, ElasticConn connection) throws Exception {
        ObjectMapper json = ((ElasticRequest) operation.getRequest()).getJson();
        Map<String, Object> search = json.convertValue(body, LinkedHashMap.class);
        if (search == null) {
            search = new LinkedHashMap<>();
        }
        Map<String, Object> aggregations = map(search.remove("aggregations"));
        if (search.containsKey("aggs")) {
            if (!aggregations.isEmpty()) {
                throw new SQLException("Use either aggs or aggregations, not both");
            }
            aggregations = map(search.get("aggs"));
        }
        search.put("aggs", aggregations);
        applyGrouping(operation, search, aggregations);
        search.put("size", 0);
        search.remove("from");

        AggregateCursor cursor = new AggregateCursor(cmd, operation, search, json, connection);
        try {
            if (cursor.countRows) {
                long count = 0;
                while (cursor.next()) {
                    count = Math.addExact(count, 1L);
                }
                JdbcColumn countColumn = new JdbcColumn(cursor.countColumn, AdapterType.Long, "", "", "",
                        ResultSetMetaData.columnNoNulls, false, AdapterType.Array);
                boolean pagingCount = operation.getHints().containsKey("overwrite_find_as_count");
                boolean visible = pagingCount || bound(operation.getHints(), "overwrite_find_skip", 0) == 0
                        && bound(operation.getHints(), "overwrite_find_limit", Long.MAX_VALUE) > 0;
                AdapterResultCursor result = new AdapterResultCursor(operation.getRequest(), Collections.singletonList(countColumn));
                if (visible) {
                    result.pushData(Collections.singletonMap(cursor.countColumn, count));
                }
                result.pushFinish();
                receive.responseResult(operation.getRequest(), result);
                cursor.close();
            } else {
                receive.responseResult(operation.getRequest(), cursor);
            }
        } catch (Exception error) {
            cursor.close();
            throw error;
        }
        return completed(sync);
    }

    private static void applyGrouping(ElasticOperation operation, Map<String, Object> search,
            Map<String, Object> aggregations) throws SQLException {
        Object grouped = operation.getHints().get("aggregation_group");
        if (grouped == null) {
            return;
        }
        List<Object> sources = new ArrayList<>();
        for (String field : grouped.toString().split(",")) {
            Map<String, Object> terms = new LinkedHashMap<>();
            terms.put("field", field);
            terms.put("missing_bucket", true);
            sources.add(Collections.singletonMap(field, Collections.singletonMap("terms", terms)));
        }
        Map<String, Object> rows = new LinkedHashMap<>();
        rows.put("composite", new LinkedHashMap<>(Collections.singletonMap("sources", sources)));
        if (!aggregations.isEmpty()) {
            rows.put("aggs", aggregations);
        }
        search.put("aggs", new LinkedHashMap<>(Collections.singletonMap("rows", rows)));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) throws SQLException {
        if (value == null) {
            return new LinkedHashMap<>();
        }
        if (!(value instanceof Map)) {
            throw new SQLException("Expected an aggregation object, got " + value.getClass().getSimpleName());
        }
        return (Map<String, Object>) value;
    }

    private static Object metric(Object response) throws SQLException {
        Map<String, Object> metric = map(response);
        if (metric.containsKey("value")) {
            return metric.get("value");
        }
        if (metric.containsKey("doc_count") && !metric.containsKey("buckets")) {
            return metric.get("doc_count");
        }
        throw new SQLFeatureNotSupportedException("A result column requires a single-value metric or single-bucket count");
    }

    private static long bound(Map<String, Object> hints, String name, long fallback) throws SQLException {
        Object value = hints.get(name);
        if (value == null) {
            return fallback;
        }
        try {
            long result = Long.parseLong(value.toString());
            if (result < 0) {
                throw new NumberFormatException();
            }
            return result;
        } catch (NumberFormatException error) {
            throw new SQLException(name + " must be a non-negative integer", error);
        }
    }

    private static String type(Object value) {
        if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return AdapterType.Int;
        }
        if (value instanceof Long) {
            return AdapterType.Long;
        }
        if (value instanceof Number) {
            return AdapterType.Double;
        }
        if (value instanceof Boolean) {
            return AdapterType.Boolean;
        }
        return value == null ? AdapterType.Unknown : AdapterType.String;
    }

    private static final class AggregateCursor implements AdapterCursor {
        private final ElasticCmd cmd;
        private final ElasticOperation operation;
        private final Map<String, Object> search;
        private final ObjectMapper json;
        private final ElasticConn connection;
        private final Map<String, Object> definitions;
        private final Map<String, Object> composite;
        private final String compositeName;
        private final int pageSize;
        private final long deadline;
        private final long limit;
        private final boolean countRows;
        private final boolean ignoreNullKeys;
        private final String countColumn;
        private final List<JdbcColumn> columns = new ArrayList<>();
        private List<Map<String, Object>> page = Collections.emptyList();
        private Map<String, Object> after;
        private Map<String, Object> current;
        private int pageIndex;
        private long skip;
        private long returned;
        private boolean exhausted;
        private boolean closed;

        AggregateCursor(ElasticCmd cmd, ElasticOperation operation, Map<String, Object> search,
                ObjectMapper json, ElasticConn connection) throws Exception {
            this.cmd = cmd;
            this.operation = operation;
            this.search = search;
            this.json = json;
            this.connection = connection;
            Map<String, Object> aggregations = map(search.get("aggs"));
            String found = null;
            for (Map.Entry<String, Object> entry : aggregations.entrySet()) {
                if (map(entry.getValue()).containsKey("composite")) {
                    if (found != null || aggregations.size() != 1) {
                        throw new SQLFeatureNotSupportedException("One composite aggregation is required for tabular bucket results");
                    }
                    found = entry.getKey();
                }
            }
            this.compositeName = found;
            Map<String, Object> root = found == null ? Collections.emptyMap() : map(aggregations.get(found));
            this.composite = found == null ? null : map(root.get("composite"));
            this.definitions = found == null ? aggregations : map(root.containsKey("aggs") ? root.get("aggs") : root.get("aggregations"));
            Map<String, Object> settings = map(map(root.get("meta")).get("dbvisitor"));
            String mode = String.valueOf(settings.getOrDefault("mode", "rows"));
            if (!"rows".equals(mode) && !"count".equals(mode)) {
                throw new SQLException("Unknown composite result mode: " + mode);
            }
            this.ignoreNullKeys = "count".equals(mode);
            this.countRows = ignoreNullKeys || operation.getHints().containsKey("overwrite_find_as_count");
            this.countColumn = String.valueOf(settings.getOrDefault("column", "COUNT"));
            AdapterRequest request = operation.getRequest();
            long requestedSize = composite == null ? DEFAULT_BUCKET_PAGE_SIZE
                    : bound(composite, "size", DEFAULT_BUCKET_PAGE_SIZE);
            if (request.getFetchSize() > 0) {
                requestedSize = request.getFetchSize();
            }
            if (requestedSize < 1 || requestedSize > Integer.MAX_VALUE) {
                throw new SQLException("Composite page size must be a positive integer");
            }
            this.pageSize = (int) requestedSize;
            this.deadline = request.getTimeoutSec() > 0
                    ? System.nanoTime() + TimeUnit.SECONDS.toNanos(request.getTimeoutSec()) : Long.MAX_VALUE;
            this.skip = countRows ? 0 : bound(operation.getHints(), "overwrite_find_skip", 0);
            long outputLimit = countRows ? Long.MAX_VALUE : bound(operation.getHints(), "overwrite_find_limit", Long.MAX_VALUE);
            this.limit = !countRows && request.getMaxRows() > 0 ? Math.min(outputLimit, request.getMaxRows()) : outputLimit;
            applyBucketOrder(search.remove("sort"));
            if (composite != null) {
                composite.put("size", pageSize);
                after = composite.containsKey("after") ? new LinkedHashMap<>(map(composite.get("after"))) : null;
            }
            loadPage();
            Map<String, Object> sample = page.isEmpty() ? Collections.emptyMap() : page.get(0);
            if (composite != null) {
                for (Object source : sourceList()) {
                    for (String key : map(source).keySet()) {
                        addColumn(key, sample.get(key));
                    }
                }
            }
            for (String alias : definitions.keySet()) {
                addColumn(alias, sample.get(alias));
            }
        }

        private void addColumn(String name, Object value) throws SQLException {
            for (JdbcColumn column : columns) {
                if (column.name.equalsIgnoreCase(name)) {
                    throw new SQLException("Duplicate aggregation result column: " + name);
                }
            }
            columns.add(new JdbcColumn(name, type(value), "", "", "",
                    ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array));
        }

        private List<?> sourceList() throws SQLException {
            Object sources = composite.get("sources");
            if (!(sources instanceof List) || ((List<?>) sources).isEmpty()) {
                throw new SQLException("Composite aggregation requires nonempty sources");
            }
            return (List<?>) sources;
        }

        private void applyBucketOrder(Object order) throws SQLException {
            if (order == null) {
                return;
            }
            if (composite == null) {
                throw new SQLFeatureNotSupportedException("Document sorting is not a metric result ordering");
            }
            List<?> orders = order instanceof List ? (List<?>) order : Collections.singletonList(order);
            Map<String, Object> requested = new LinkedHashMap<>();
            for (Object item : orders) {
                requested.putAll(map(item));
            }
            List<Object> orderedSources = new ArrayList<>();
            for (Map.Entry<String, Object> entry : requested.entrySet()) {
                boolean matched = false;
                for (Object source : sourceList()) {
                    Map<String, Object> sourceMap = map(source);
                    if (sourceMap.containsKey(entry.getKey())) {
                        Map<String, Object> definition = map(sourceMap.get(entry.getKey()));
                        if (definition.size() != 1) {
                            throw new SQLException("Composite source must have one value source");
                        }
                        Map<String, Object> valueSource = map(definition.values().iterator().next());
                        Object direction = entry.getValue();
                        if (direction instanceof Map) {
                            Map<String, Object> options = map(direction);
                            direction = options.getOrDefault("order", "asc");
                            if (options.containsKey("missing")) {
                                throw new SQLFeatureNotSupportedException("Explicit null placement requires a native composite source supported by the server version");
                            }
                        }
                        if (!"asc".equals(direction) && !"desc".equals(direction)) {
                            throw new SQLException("Composite source order must be asc or desc");
                        }
                        valueSource.put("order", direction);
                        orderedSources.add(source);
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    throw new SQLFeatureNotSupportedException("Global ordering by an aggregate value requires a different server aggregation");
                }
            }
            for (Object source : sourceList()) {
                if (!orderedSources.contains(source)) {
                    orderedSources.add(source);
                }
            }
            composite.put("sources", orderedSources);
        }

        private void loadPage() throws Exception {
            checkActive();
            Request request = new Request(operation.getMethod().name(), operation.getEndpoint());
            request.setJsonEntity(json.writeValueAsString(search));
            Response response = cmd.getClient().performRequest(request);
            Map<String, Object> result;
            try (InputStream stream = response.getEntity().getContent()) {
                result = json.readValue(stream, LinkedHashMap.class);
            }
            checkActive();
            if (Boolean.TRUE.equals(result.get("timed_out")) || Boolean.TRUE.equals(result.get("terminated_early"))
                    || ((Number) map(result.get("_shards")).getOrDefault("failed", 0)).longValue() > 0) {
                throw new SQLException("Elasticsearch returned an incomplete aggregation response");
            }
            if (!result.containsKey("aggregations")) {
                throw new SQLException("Elasticsearch response has no aggregations");
            }
            Map<String, Object> values = map(result.get("aggregations"));
            List<Map<String, Object>> rows = new ArrayList<>();
            if (composite == null) {
                rows.add(metricRow(values));
                exhausted = true;
            } else {
                Map<String, Object> aggregation = map(values.get(compositeName));
                Object buckets = aggregation.get("buckets");
                if (!(buckets instanceof List)) {
                    throw new SQLException("Composite response has no bucket list");
                }
                for (Object item : (List<?>) buckets) {
                    Map<String, Object> bucket = map(item);
                    Map<String, Object> keys = map(bucket.get("key"));
                    if (ignoreNullKeys && keys.containsValue(null)) {
                        continue;
                    }
                    Map<String, Object> row = new LinkedHashMap<>(keys);
                    row.putAll(metricRow(bucket));
                    rows.add(row);
                }
                Object next = aggregation.get("after_key");
                exhausted = ((List<?>) buckets).isEmpty() || next == null;
                if (!exhausted) {
                    Map<String, Object> nextKey = new LinkedHashMap<>(map(next));
                    if (Objects.equals(after, nextKey)) {
                        throw new SQLException("Composite aggregation after_key did not advance");
                    }
                    after = nextKey;
                    composite.put("after", after);
                }
            }
            this.page = rows;
            this.pageIndex = 0;
        }

        private void checkActive() throws SQLException {
            if (closed) {
                throw new SQLException("Aggregation cursor is closed");
            }
            if (connection.isCancelled() || Thread.currentThread().isInterrupted()) {
                throw new SQLException("Aggregation query cancelled");
            }
            if (System.nanoTime() > deadline) {
                throw new SQLTimeoutException("Aggregation query timeout");
            }
        }

        private Map<String, Object> metricRow(Map<String, Object> values) throws SQLException {
            Map<String, Object> row = new LinkedHashMap<>();
            for (String alias : definitions.keySet()) {
                if (!values.containsKey(alias)) {
                    throw new SQLException("Missing aggregation result: " + alias);
                }
                row.put(alias, metric(values.get(alias)));
            }
            return row;
        }

        @Override
        public boolean next() throws SQLException {
            checkActive();
            current = null;
            if (returned >= limit) {
                page = Collections.emptyList();
                exhausted = true;
                return false;
            }
            while (true) {
                while (pageIndex < page.size()) {
                    Map<String, Object> row = page.get(pageIndex++);
                    if (skip > 0) {
                        skip--;
                        continue;
                    }
                    current = row;
                    returned++;
                    return true;
                }
                if (exhausted) {
                    return false;
                }
                try {
                    loadPage();
                } catch (SQLException error) {
                    close();
                    throw error;
                } catch (Exception error) {
                    close();
                    throw new SQLException("Could not fetch the next aggregation page", error);
                }
            }
        }

        @Override
        public Object column(int index) throws SQLException {
            if (closed || current == null || index < 1 || index > columns.size()) {
                throw new SQLException("No aggregation value at column " + index);
            }
            return current.get(columns.get(index - 1).name);
        }

        @Override
        public List<JdbcColumn> columns() { return Collections.unmodifiableList(columns); }
        @Override
        public int batchSize() { return pageSize; }
        @Override
        public void close() { closed = true; current = null; page = Collections.emptyList(); }
        @Override
        public List<String> warnings() { return Collections.emptyList(); }
        @Override
        public void clearWarnings() { }
        @Override
        public boolean isPending() { return !closed && !exhausted; }
        @Override
        public boolean isClose() { return closed; }
    }
}
