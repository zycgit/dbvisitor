package net.hasor.dbvisitor.adapter.elastic;

import java.io.InputStream;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

class ElasticCommandsForCrud extends ElasticCommands {
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

        Request esRequest = new Request(o.getMethod().name(), endpoint);
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        AdapterResultCursor generatedKeys = null;
        if (o.getRequest().isGeneratedKeys()) {
            try (InputStream inputStream = response.getEntity().getContent()) {
                Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);
                Object id = responseMap.get("_id");
                if (id != null) {
                    generatedKeys = listResult(o.getRequest(), COL_ID_STRING, Collections.singletonList(id));
                }
            }
        }

        // Insert 操作始终返回 1（可从响应获取 _id）
        receive.responseUpdateCount(o.getRequest(), 1, generatedKeys);
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
