/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands.query;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import io.milvus.grpc.DataType;
import io.milvus.grpc.FieldSchema;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.v2.common.IndexParam.MetricType;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.MilvusRequest;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusExpression.Filter;
import net.hasor.dbvisitor.adapter.milvus.mapping.MilvusSchema;
import net.hasor.dbvisitor.adapter.milvus.mapping.MilvusVectorCodec;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.JdbcColumn;
import static net.hasor.dbvisitor.adapter.milvus.MilvusRequest.checkActive;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusExpression.parseWhere;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusVector.*;

public final class MilvusCommandsForDQL extends MilvusCommands {
    private MilvusCommandsForDQL() {
    }

    public static Future<?> execSelectCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, SelectCmdContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        Map<String, Object> hints = readHints(argIndex, request, h.hint());
        String collectionName = getIdentifier(c.collectionName.getText());
        String partitionName = c.partitionName != null ? getIdentifier(c.partitionName.getText()) : null;
        List<String> outFields = new ArrayList<>();
        if (c.selectElements().STAR() != null) {
            outFields.add("*");
        } else {
            for (SelectElementContext element : c.selectElements().selectElement()) {
                outFields.add(getIdentifier(element.fieldName.getText()));
            }
        }

        // Bind once, in SQL order, including arguments subsequently overridden by a hint.
        VectorRangeExpr range = parseVectorRange(c.expression(), argIndex, request);
        Filter filter = range == null ? parseWhere(c.expression(), argIndex, request) : range.scalarFilter;
        SortClauseContext sort = c.sortClause();
        Object rawVector = null;
        if (sort != null) {
            if (range != null) {
                throw new SQLException("Combining a vector range with ORDER BY is not supported.");
            }
            rawVector = readVectorValue(sort.vectorValue(), argIndex, request);
        }
        List<MilvusHybridSearch.Candidate> candidates = MilvusHybridSearch.bind(c.hybridClause(), argIndex, request);
        if (!candidates.isEmpty() && range != null) {
            throw new SQLException("Hybrid Search requires a scalar WHERE filter.");
        }
        Long sqlLimit = readLimit(c.limit, argIndex, request);
        Long sqlOffset = readBound(c.offset, argIndex, request, "OFFSET", 0);
        Map<String, Object> properties = readProperties(argIndex, request, c.propertiesList());
        MilvusSearchGrouping grouping = MilvusSearchGrouping.extract(properties);
        QueryWindow window = new QueryWindow(sqlLimit, sqlOffset, hints, request);

        if (hints.containsKey(MilvusCommandKeys.OVERWRITE_FIND_AS_COUNT)) {
            if (range != null || !candidates.isEmpty() || grouping != null) {
                throw new SQLException("COUNT of a vector range, hybrid or grouped search is not supported.");
            }
            return execCountQuery(future, cmd, collectionName, partitionName, filter, MilvusQueryOptions.extract(properties, false), request, receive);
        }

        int batchSize = window.batchSize;
        boolean iterator = window.useIterator;
        boolean vectorSearch = range != null || sort != null || !candidates.isEmpty();
        if (grouping != null && !vectorSearch) {
            throw new SQLException("Grouped search requires vector ORDER BY; it is not scalar GROUP BY.");
        }
        MilvusQueryOptions scalarOptions = vectorSearch ? null : MilvusQueryOptions.extract(properties, false);
        Map<String, FieldSchema> fields = cmd.describeFields(collectionName, request);
        MilvusResultCursor.SourceFactory source;
        if (!candidates.isEmpty()) {
            if (sqlLimit == null && grouping == null) {
                throw new SQLException("Hybrid Search requires an explicit LIMIT; the SDK has no hybrid iterator.");
            }
            List<String> storedFields = outFields.stream().filter(name -> !"score".equals(name)).collect(Collectors.toList());
            HybridSearchReq query = MilvusHybridSearch.build(cmd, collectionName, partitionName, filter, storedFields, fields, candidates, window.limit, window.offset, properties, grouping, (MilvusRequest) request);
            source = () -> singlePage(searchRows(cmd.hybridSearch(query)));
            iterator = false; // No hybrid iterator; grouped SQL row OFFSET is handled below.
        } else if (range != null || sort != null) {
            String field = range != null ? range.fieldName : getIdentifier(sort.fieldName.getText());
            MetricType metric = range != null ? range.metricType : vectorMetric(sort.distanceOperator());
            BaseVector vector = MilvusVectorCodec.searchValue(fields.get(field), range != null ? range.vectorValue : rawVector, metric);
            validateSearchProperties(properties, metric, range);
            List<String> storedFields = outFields.stream().filter(name -> !"score".equals(name)).collect(Collectors.toList());
            source = searchSource(cmd, collectionName, partitionName, filter, storedFields, field, vector, metric, properties, grouping, window, request);
        } else {
            source = querySource(cmd, collectionName, partitionName, filter, outFields, scalarOptions, window, request);
        }

        List<JdbcColumn> columns = resultColumns(fields, collectionName, cmd.getCatalog(), outFields, vectorSearch);
        // Grouped search returns one native group window; SQL OFFSET still skips rows within it.
        long rowOffset = iterator || grouping != null ? window.offset : 0;
        MilvusResultCursor cursor = new MilvusResultCursor((MilvusRequest) request, columns, batchSize, window.limit, rowOffset, source);
        receive.responseResult(request, cursor);
        return completed(future);
    }

    // Open a bounded request or SDK iterator now; ResultSet.next() consumes pages on demand.

    private static void validateSearchProperties(Map<String, Object> properties, MetricType metric, VectorRangeExpr range) throws SQLException {
        Object propertyMetric = properties.get(MilvusCommandKeys.METRIC_TYPE);
        if (properties.containsKey(MilvusCommandKeys.METRIC_TYPE) && !metric.name().equalsIgnoreCase(String.valueOf(propertyMetric))) {
            throw new SQLException("WITH " + MilvusCommandKeys.METRIC_TYPE + " must agree with the SQL distance operator (" + metric + ").");
        }
        properties.put(MilvusCommandKeys.METRIC_TYPE, metric.name());
        if (range != null) {
            if (properties.containsKey(MilvusCommandKeys.RADIUS) || properties.containsKey(MilvusCommandKeys.RANGE_FILTER)) {
                throw new SQLException("WITH " + MilvusCommandKeys.RADIUS + "/" + MilvusCommandKeys.RANGE_FILTER + " cannot override a WHERE vector range.");
            }
            properties.put(MilvusCommandKeys.RADIUS, range.radius);
        }
        if (properties.containsKey(MilvusCommandKeys.OFFSET)) {
            throw new SQLException("Use SQL OFFSET or " + MilvusCommandKeys.OVERWRITE_FIND_SKIP + " instead of WITH " + MilvusCommandKeys.OFFSET + ".");
        }
    }

    private static MilvusResultCursor.SourceFactory searchSource(MilvusCmd cmd, String collectionName, String partitionName, Filter filter, List<String> outFields, String field, BaseVector vector, MetricType metric, Map<String, Object> properties, MilvusSearchGrouping grouping, QueryWindow window, AdapterRequest request) throws SQLException {
        MilvusQueryOptions options = MilvusQueryOptions.extract(properties, true);
        if (grouping == null && window.useIterator) {
            SearchIteratorReqV2.SearchIteratorReqV2Builder builder = SearchIteratorReqV2.builder().databaseName(cmd.getCatalog()).collectionName(collectionName).filter(filter.expression()).filterTemplateValues(filter.parameters()).vectorFieldName(field).vectors(Collections.singletonList(vector)).metricType(metric).outputFields(outFields).searchParams(new LinkedHashMap<>(properties)).batchSize(window.batchSize);
            options.apply(builder);
            if (StringUtils.isNotBlank(partitionName)) {
                builder.partitionNames(Collections.singletonList(partitionName));
            }
            applyConsistencyLevel(((MilvusRequest) request).getConsistencyLevel(), builder);
            // SDK 2.6.22 narrows the V2 iterator limit to int; larger windows are bounded by the JDBC cursor.
            if (window.limit != null && window.selectionLimit() <= Integer.MAX_VALUE) {
                builder.limit(window.selectionLimit());
            }
            SearchIteratorReqV2 query = builder.build();
            return () -> {
                SearchIteratorV2 iterator = cmd.searchIteratorV2(query);
                if (iterator == null) {
                    throw new SQLException("Milvus returned no search iterator.");
                }
                return new MilvusResultCursor.PageSource(() -> rowMaps(searchRecords(iterator.next())), iterator::close);
            };
        }

        SearchReq.SearchReqBuilder builder = SearchReq.builder().databaseName(cmd.getCatalog()).collectionName(collectionName).filter(filter.expression()).filterTemplateValues(filter.parameters()).annsField(field).data(Collections.singletonList(vector)).metricType(metric).outputFields(outFields).searchParams(properties);
        if (grouping == null) {
            builder.topK(window.limit.intValue()).offset(window.offset);
        } else {
            grouping.apply(builder);
        }
        options.apply(builder);
        applyConsistencyLevel(((MilvusRequest) request).getConsistencyLevel(), builder);
        if (StringUtils.isNotBlank(partitionName)) {
            builder.partitionNames(Collections.singletonList(partitionName));
        }
        SearchReq query = builder.build();
        return () -> {
            SearchResp response = cmd.search(query);
            return singlePage(searchRows(response));
        };
    }

    private static List<Map<String, Object>> searchRows(SearchResp response) throws SQLException {
        if (response.getSearchResults().size() > 1) {
            throw new SQLException("Expected one query result group, received multiple groups.");
        }
        return response.getSearchResults().isEmpty() ? Collections.emptyList() : rowMaps(searchRecords(response.getSearchResults().get(0)));
    }

    private static MilvusResultCursor.SourceFactory querySource(MilvusCmd cmd, String collectionName, String partitionName, Filter filter, List<String> outFields, MilvusQueryOptions options, QueryWindow window, AdapterRequest request) throws SQLException {
        if (window.useIterator) {
            QueryIteratorReq.QueryIteratorReqBuilder builder = QueryIteratorReq.builder().databaseName(cmd.getCatalog()).collectionName(collectionName).expr(filter.expression()).filterTemplateValues(filter.parameters()).outputFields(outFields).batchSize(window.batchSize);
            options.apply(builder);
            if (StringUtils.isNotBlank(partitionName)) {
                builder.partitionNames(Collections.singletonList(partitionName));
            }
            applyConsistencyLevel(((MilvusRequest) request).getConsistencyLevel(), builder);
            // Skip in the JDBC cursor so cancellation and fetchSize also apply while seeking OFFSET.
            if (window.limit != null) {
                builder.limit(window.selectionLimit());
            }
            QueryIteratorReq query = builder.build();
            return () -> {
                QueryIterator iterator = cmd.queryIterator(query);
                if (iterator == null) {
                    throw new SQLException("Milvus returned no query iterator.");
                }
                return new MilvusResultCursor.PageSource(() -> rowMaps(iterator.next()), iterator::close);
            };
        }

        QueryReq.QueryReqBuilder builder = QueryReq.builder().databaseName(cmd.getCatalog()).collectionName(collectionName).filter(filter.expression()).filterTemplateValues(filter.parameters()).outputFields(outFields).limit(window.limit).offset(window.offset);
        options.apply(builder);
        applyConsistencyLevel(((MilvusRequest) request).getConsistencyLevel(), builder);
        if (StringUtils.isNotBlank(partitionName)) {
            builder.partitionNames(Collections.singletonList(partitionName));
        }
        QueryReq query = builder.build();
        return () -> singlePage(cmd.query(query).getQueryResults().stream().map(QueryResp.QueryResult::getEntity).collect(Collectors.toList()));
    }

    private static List<Map<String, Object>> rowMaps(List<QueryResultsWrapper.RowRecord> rows) {
        if (rows == null) {
            return Collections.emptyList();
        }
        return rows.stream().map(QueryResultsWrapper.RowRecord::getFieldValues).collect(Collectors.toList());
    }

    private static MilvusResultCursor.PageSource singlePage(List<Map<String, Object>> rows) {
        Iterator<List<Map<String, Object>>> pages = Collections.singletonList(rows).iterator();
        return new MilvusResultCursor.PageSource(() -> pages.hasNext() ? pages.next() : Collections.emptyList(), () -> {
        });
    }

    // Schema metadata is available before fetching rows, including for empty results.

    private static List<JdbcColumn> resultColumns(Map<String, FieldSchema> fields, String collectionName, String catalog, List<String> outFields, boolean vectorSearch) throws SQLException {
        Map<String, FieldSchema> types = new LinkedHashMap<>(fields);
        if (vectorSearch) {
            types.put("score", FieldSchema.newBuilder().setName("score").setDataType(DataType.Float).build());
        }
        List<String> names = outFields.contains("*") ? new ArrayList<>(types.keySet()) : outFields;
        List<JdbcColumn> columns = new ArrayList<>();
        for (String name : names) {
            FieldSchema field = types.get(name);
            if (field == null) {
                throw new SQLException("Unknown output field: " + name);
            }
            columns.add(MilvusSchema.column(field, collectionName, catalog));
        }
        return columns;
    }

    // COUNT

    public static Future<?> execCountCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CountCmdContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        if (isVectorRange(c.expression())) {
            throw new SQLException("COUNT of a vector range is not supported.");
        }
        String collectionName = getIdentifier(c.collectionName.getText());
        String partitionName = c.partitionName != null ? getIdentifier(c.partitionName.getText()) : null;
        Filter filter = parseWhere(c.expression(), argIndex, request);
        Map<String, Object> properties = readProperties(argIndex, request, c.propertiesList());
        return execCountQuery(future, cmd, collectionName, partitionName, filter, MilvusQueryOptions.extract(properties, false), request, receive);
    }

    private static Future<?> execCountQuery(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, //
            Filter expr, MilvusQueryOptions options, AdapterRequest request, AdapterReceive receive) throws SQLException {
        QueryReq.QueryReqBuilder builder = QueryReq.builder().databaseName(cmd.getCatalog()).collectionName(collectionName).filter(expr.expression()).filterTemplateValues(expr.parameters()).outputFields(Collections.singletonList("count(*)"));
        options.apply(builder);
        applyConsistencyLevel(((MilvusRequest) request).getConsistencyLevel(), builder);
        if (StringUtils.isNotBlank(partitionName)) {
            builder.partitionNames(Collections.singletonList(partitionName));
        }
        checkActive(request);
        QueryResp callback = cmd.query(builder.build());
        checkActive(request);
        receive.responseResult(request, singleResult(request, COL_COUNT_LONG, readCountValue(callback)));
        return completed(future);
    }

    private static long readCountValue(QueryResp response) {
        if (response.getQueryResults().isEmpty()) {
            return 0;
        }
        Object count = response.getQueryResults().get(0).getEntity().get("count(*)");
        return count instanceof Number ? ((Number) count).longValue() : 0;
    }

    // Query-local state: effective window and result metadata

    private static final class QueryWindow {
        private final Long    limit;
        private final long    offset;
        private final boolean useIterator;
        private final int     batchSize;

        private QueryWindow(Long sqlLimit, Long sqlOffset, Map<String, Object> hints, AdapterRequest request) throws SQLException {
            Long effectiveLimit = sqlLimit;
            if (hints.containsKey(MilvusCommandKeys.OVERWRITE_FIND_LIMIT)) {
                effectiveLimit = integerBound(hints.get(MilvusCommandKeys.OVERWRITE_FIND_LIMIT), MilvusCommandKeys.OVERWRITE_FIND_LIMIT, 1);
            }
            boolean unlimited = effectiveLimit == null;
            long effectiveOffset = sqlOffset == null ? 0 : sqlOffset;
            if (hints.containsKey(MilvusCommandKeys.OVERWRITE_FIND_SKIP)) {
                effectiveOffset = integerBound(hints.get(MilvusCommandKeys.OVERWRITE_FIND_SKIP), MilvusCommandKeys.OVERWRITE_FIND_SKIP, 0);
            }
            this.offset = effectiveOffset;
            if (request.getMaxRows() > 0) {
                effectiveLimit = effectiveLimit == null ? request.getMaxRows() : Math.min(effectiveLimit, request.getMaxRows());
            }
            this.limit = effectiveLimit;
            long sdkBatchSize = QueryIteratorReq.builder().build().getBatchSize();
            this.batchSize = request.getFetchSize() > 0 ? Math.min(request.getFetchSize(), io.milvus.param.Constant.MAX_BATCH_SIZE) : (int) sdkBatchSize;
            this.useIterator = unlimited || effectiveLimit > this.batchSize || effectiveOffset > 0;
        }

        private long selectionLimit() throws SQLException {
            try {
                return Math.addExact(offset, limit);
            } catch (ArithmeticException e) {
                throw new SQLException("OFFSET plus result limit exceeds the SDK long range.", e);
            }
        }

    }

}
