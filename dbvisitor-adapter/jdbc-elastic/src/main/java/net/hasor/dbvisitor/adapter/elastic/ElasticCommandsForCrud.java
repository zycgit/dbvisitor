/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.ByteArrayEntity;

class ElasticCommandsForCrud extends ElasticCommands {
    public static Future<?> execBulk(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        ObjectMapper mapper = ((ElasticRequest) o.getRequest()).getJson();
        List<String> actions = new ArrayList<>();
        String body = bulkBody(mapper, jsonBody, actions);
        Request request = new Request(o.getMethod().name(), o.getEndpointWithRefresh());
        request.setEntity(new ByteArrayEntity(body.getBytes(StandardCharsets.UTF_8), ContentType.create("application/x-ndjson")));
        Response response = cmd.getClient().performRequest(request);
        JsonNode result;
        try (InputStream input = response.getEntity().getContent()) {
            result = mapper.readTree(input);
        }
        JsonNode items = result == null ? null : result.get("items");
        if (items == null || !items.isArray() || items.size() != actions.size()) {
            throw new SQLException("Bulk response must contain one item per action; write outcome is incomplete");
        }

        int[] counts = new int[actions.size()];
        List<Object> keys = new ArrayList<>();
        SQLException failure = null;
        int total = 0;
        boolean unknownCount = false;
        for (int i = 0; i < counts.length; i++) {
            String action = actions.get(i);
            JsonNode item = items.get(i).get(action);
            if (item == null || !item.isObject() || !item.path("status").isIntegralNumber()) {
                throw new SQLException("Invalid bulk response item " + (i + 1) + "; write outcome is incomplete");
            }
            int status = item.get("status").intValue();
            String outcome = item.path("result").asText();
            boolean missingDelete = "delete".equals(action) && status == 404 && "not_found".equals(outcome);
            if (item.hasNonNull("error") || (status >= 300 && !missingDelete) || status < 200) {
                counts[i] = Statement.EXECUTE_FAILED;
                SQLException error = new SQLException("Bulk item " + (i + 1) + " (" + action + "): " + item,
                        status == 409 ? "23505" : "E" + status, status);
                if (failure == null) {
                    failure = error;
                } else {
                    failure.setNextException(error);
                }
            } else {
                counts[i] = bulkCount(outcome);
                unknownCount |= counts[i] == Statement.SUCCESS_NO_INFO;
                if (counts[i] > 0) {
                    total += counts[i];
                }
                if (("index".equals(action) || "create".equals(action)) && item.hasNonNull("_id")) {
                    keys.add(item.get("_id").asText());
                }
            }
        }
        if (failure != null || result.path("errors").asBoolean(false)) {
            BatchUpdateException error = new BatchUpdateException("Bulk write failed; inspect update counts and chained errors",
                    failure == null ? null : failure.getSQLState(), counts);
            if (failure != null) {
                error.setNextException(failure);
            } else {
                error.setNextException(new SQLException("Bulk response reported errors without item failure details"));
            }
            throw error;
        }
        AdapterResultCursor generatedKeys = o.getRequest().isGeneratedKeys() ? listResult(o.getRequest(), COL_ID_STRING, keys) : null;
        receive.responseUpdateCount(o.getRequest(), unknownCount ? Statement.SUCCESS_NO_INFO : total, generatedKeys);
        return completed(sync);
    }

    private static String bulkBody(ObjectMapper mapper, Object body, List<String> actions) throws Exception {
        if (!(body instanceof List) || ((List<?>) body).isEmpty()) {
            throw new SQLException("Bulk body must be a nonempty JSON array of action and document objects");
        }
        List<?> lines = (List<?>) body;
        StringBuilder ndjson = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            Object line = lines.get(i);
            if (!(line instanceof Map) || ((Map<?, ?>) line).size() != 1) {
                throw new SQLException("Bulk action at array position " + (i + 1) + " must contain one action");
            }
            Map.Entry<?, ?> entry = ((Map<?, ?>) line).entrySet().iterator().next();
            String action = String.valueOf(entry.getKey());
            if (!(entry.getValue() instanceof Map) || !("index".equals(action) || "create".equals(action)
                    || "update".equals(action) || "delete".equals(action))) {
                throw new SQLException("Invalid bulk action at array position " + (i + 1));
            }
            actions.add(action);
            ndjson.append(mapper.writer().without(SerializationFeature.INDENT_OUTPUT).writeValueAsString(line)).append('\n');
            if (!"delete".equals(action)) {
                if (++i >= lines.size() || !(lines.get(i) instanceof Map)) {
                    throw new SQLException("Bulk " + action + " requires a following document object");
                }
                ndjson.append(mapper.writer().without(SerializationFeature.INDENT_OUTPUT).writeValueAsString(lines.get(i))).append('\n');
            }
        }
        return ndjson.toString();
    }

    private static int bulkCount(String result) {
        switch (result) {
            case "created":
            case "updated":
            case "deleted":
                return 1;
            case "noop":
            case "not_found":
                return 0;
            default:
                return Statement.SUCCESS_NO_INFO;
        }
    }

    public static Future<?> execInsert(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        // 只对文档操作（路径包含 /_doc 或 /_create）使用 refresh 参数
        String queryPath = o.getQueryPath();
        boolean isDocumentOp = queryPath.contains("/_doc") || queryPath.contains("/_create");
        if (!isDocumentOp) {
            String[] parts = queryPath.split("/");
            if (parts.length >= 3 && !parts[2].startsWith("_")) {
                isDocumentOp = true;
            }
        }
        String endpoint = isDocumentOp ? o.getEndpointWithRefresh() : o.getEndpoint();
        Object keyColumn = o.getHints().get("document_id_column");
        String strategy = String.valueOf(o.getHints().getOrDefault("duplicate_strategy", "Into"));
        boolean assignedKey = false;
        if (keyColumn instanceof Number && jsonBody instanceof Map) {
            int column = ((Number) keyColumn).intValue() - 1;
            ArrayList<Object> values = new ArrayList<>(((Map<?, ?>) jsonBody).values());
            if (column < 0 || column >= values.size()) {
                throw new IllegalArgumentException("document_id_column is outside the document fields");
            }
            Object id = values.get(column);
            if (id != null) {
                assignedKey = true;
                String encoded = URLEncoder.encode(String.valueOf(id), StandardCharsets.UTF_8).replace("+", "%20");
                int queryIndex = endpoint.indexOf('?');
                endpoint = queryIndex < 0 ? endpoint + "/" + encoded : endpoint.substring(0, queryIndex) + "/" + encoded + endpoint.substring(queryIndex);
                if (!"Update".equals(strategy)) {
                    endpoint += (endpoint.contains("?") ? "&" : "?") + "op_type=create";
                }
            }
        }

        boolean upsert = assignedKey && "Update".equals(strategy);
        if (upsert) {
            int queryIndex = endpoint.indexOf('?');
            endpoint = queryIndex < 0 ? endpoint + "/_update" : endpoint.substring(0, queryIndex) + "/_update" + endpoint.substring(queryIndex);
            Map<String, Object> updateBody = new java.util.LinkedHashMap<>();
            updateBody.put("doc", jsonBody);
            updateBody.put("doc_as_upsert", true);
            jsonBody = updateBody;
        }
        Request esRequest = new Request(upsert ? "POST" : o.getMethod().name(), endpoint);
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response;
        try {
            response = cmd.getClient().performRequest(esRequest);
        } catch (ResponseException error) {
            if (error.getResponse().getStatusLine().getStatusCode() == 409
                    && assignedKey && "Ignore".equals(strategy)) {
                receive.responseUpdateCount(o.getRequest(), 0);
                return completed(sync);
            }
            if (error.getResponse().getStatusLine().getStatusCode() == 409 && assignedKey && !upsert) {
                throw new SQLIntegrityConstraintViolationException("Duplicate document primary key", "23505", error);
            }
            throw error;
        }

        AdapterResultCursor generatedKeys = null;
        int updateCount = 1;
        if (o.getRequest().isGeneratedKeys() || upsert) {
            try (InputStream inputStream = response.getEntity().getContent()) {
                Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);
                Object id = responseMap.get("_id");
                if (id != null && o.getRequest().isGeneratedKeys()) {
                    generatedKeys = listResult(o.getRequest(), COL_ID_STRING, Collections.singletonList(id));
                }
                if (upsert && "noop".equals(responseMap.get("result"))) {
                    updateCount = 0;
                }
            }
        }

        receive.responseUpdateCount(o.getRequest(), updateCount, generatedKeys);
        return completed(sync);
    }

    public static Future<?> execUpdateDoc(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(o.getMethod().name(), o.getEndpointWithRefresh());
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        // 判断是否有 refresh 参数
        boolean hasRefresh = o.hasRefreshParam() || ((ElasticRequest) o.getRequest()).isIndexRefresh();

        int updateCount;
        try (InputStream inputStream = response.getEntity().getContent()) {
            Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);
            Object result = responseMap.get("result");
            if ("updated".equals(result) || "created".equals(result)) {
                updateCount = 1;
            } else if ("noop".equals(result)) {
                updateCount = 0;
            } else {
                updateCount = hasRefresh ? 0 : Statement.SUCCESS_NO_INFO;
            }
        }

        // 无 refresh 时返回 SUCCESS_NO_INFO
        if (!hasRefresh) {
            updateCount = Statement.SUCCESS_NO_INFO;
        }

        receive.responseUpdateCount(o.getRequest(), updateCount);
        return completed(sync);
    }

    public static Future<?> execUpdateByQuery(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        // 使用 getEndpointWithRefresh() 根据配置决定是否添加 refresh 参数
        Request esRequest = new Request(o.getMethod().name(), o.getEndpointWithRefresh());

        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        // 判断是否有 refresh 参数
        boolean hasRefresh = o.hasRefreshParam() || ((ElasticRequest) o.getRequest()).isIndexRefresh();

        int updateCount;
        try (InputStream inputStream = response.getEntity().getContent()) {
            Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);
            Object updated = responseMap.get("updated");
            if (updated instanceof Number) {
                updateCount = ((Number) updated).intValue();
            } else {
                updateCount = 0;
            }
        }

        // 无 refresh 时返回 SUCCESS_NO_INFO
        if (!hasRefresh) {
            updateCount = Statement.SUCCESS_NO_INFO;
        }

        receive.responseUpdateCount(o.getRequest(), updateCount);
        return completed(sync);
    }

    public static Future<?> execDeleteByQuery(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        // 使用 getEndpointWithRefresh() 根据配置决定是否添加 refresh 参数
        Request esRequest = new Request(o.getMethod().name(), o.getEndpointWithRefresh());

        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        // 判断是否有 refresh 参数
        boolean hasRefresh = o.hasRefreshParam() || ((ElasticRequest) o.getRequest()).isIndexRefresh();

        int updateCount;
        try (InputStream inputStream = response.getEntity().getContent()) {
            Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);
            Object deleted = responseMap.get("deleted");
            if (deleted instanceof Number) {
                updateCount = ((Number) deleted).intValue();
            } else {
                updateCount = 0;
            }
        }

        // 无 refresh 时返回 SUCCESS_NO_INFO
        if (!hasRefresh) {
            updateCount = Statement.SUCCESS_NO_INFO;
        }

        receive.responseUpdateCount(o.getRequest(), updateCount);
        return completed(sync);
    }

    public static Future<?> execDelete(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        // 只对文档操作（路径包含 /_doc 或 /_create）使用 refresh 参数
        String queryPath = o.getQueryPath();
        boolean isDocumentOp = queryPath.contains("/_doc") || queryPath.contains("/_create");
        if (!isDocumentOp) {
            String[] parts = queryPath.split("/");
            if (parts.length >= 3 && !parts[2].startsWith("_")) {
                isDocumentOp = true;
            }
        }
        String endpoint = isDocumentOp ? o.getEndpointWithRefresh() : o.getEndpoint();

        Request esRequest = new Request(o.getMethod().name(), endpoint);
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        // 判断是否有 refresh 参数
        boolean hasRefresh = o.hasRefreshParam() || ((ElasticRequest) o.getRequest()).isIndexRefresh();

        int updateCount;
        try (InputStream inputStream = response.getEntity().getContent()) {
            Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);
            Object result = responseMap.get("result");
            if ("deleted".equals(result)) {
                updateCount = 1;
            } else {
                updateCount = 0;
            }
        }

        // 无 refresh 时返回 SUCCESS_NO_INFO
        if (!hasRefresh) {
            updateCount = Statement.SUCCESS_NO_INFO;
        }

        receive.responseUpdateCount(o.getRequest(), updateCount);
        return completed(sync);
    }
}
