package net.hasor.dbvisitor.adapter.elastic;
import java.io.InputStream;
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
        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
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

        receive.responseUpdateCount(o.getRequest(), 1, generatedKeys);
        return completed(sync);
    }

    public static Future<?> execUpdateDoc(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        int updateCount = 0;
        try (InputStream inputStream = response.getEntity().getContent()) {
            Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);
            Object result = responseMap.get("result");
            if ("updated".equals(result) || "created".equals(result)) {
                updateCount = 1;
            } else if ("noop".equals(result)) {
                updateCount = 0;
            }
        }

        receive.responseUpdateCount(o.getRequest(), updateCount);
        return completed(sync);
    }

    public static Future<?> execUpdateByQuery(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        String endpoint = o.getEndpoint();
        if (!endpoint.contains("refresh=")) {
            if (endpoint.contains("?")) {
                endpoint += "&refresh=true";
            } else {
                endpoint += "?refresh=true";
            }
        }
        Request esRequest = new Request(o.getMethod().name(), endpoint);

        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        int updateCount = 0;
        try (InputStream inputStream = response.getEntity().getContent()) {
            Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);
            Object updated = responseMap.get("updated");
            if (updated instanceof Number) {
                updateCount = ((Number) updated).intValue();
            }
        }

        receive.responseUpdateCount(o.getRequest(), updateCount);
        return completed(sync);
    }

    public static Future<?> execDeleteByQuery(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        String endpoint = o.getEndpoint();
        if (!endpoint.contains("refresh=")) {
            if (endpoint.contains("?")) {
                endpoint += "&refresh=true";
            } else {
                endpoint += "?refresh=true";
            }
        }
        Request esRequest = new Request(o.getMethod().name(), endpoint);

        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        int updateCount = 0;
        try (InputStream inputStream = response.getEntity().getContent()) {
            Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);
            Object deleted = responseMap.get("deleted");
            if (deleted instanceof Number) {
                updateCount = ((Number) deleted).intValue();
            }
        }

        receive.responseUpdateCount(o.getRequest(), updateCount);
        return completed(sync);
    }

    public static Future<?> execDelete(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
        ObjectMapper jsonMapper = ((ElasticRequest) o.getRequest()).getJson();
        if (jsonBody != null) {
            esRequest.setJsonEntity(jsonMapper.writeValueAsString(jsonBody));
        }
        Response response = cmd.getClient().performRequest(esRequest);

        int updateCount = 0;
        try (InputStream inputStream = response.getEntity().getContent()) {
            Map<String, Object> responseMap = jsonMapper.readValue(inputStream, Map.class);
            Object result = responseMap.get("result");
            if ("deleted".equals(result)) {
                updateCount = 1;
            }
        }

        receive.responseUpdateCount(o.getRequest(), updateCount);
        return completed(sync);
    }
}
