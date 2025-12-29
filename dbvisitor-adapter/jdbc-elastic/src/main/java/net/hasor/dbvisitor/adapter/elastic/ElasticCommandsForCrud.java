package net.hasor.dbvisitor.adapter.elastic;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

class ElasticCommandsForCrud extends ElasticCommands {
    public static Future<?> execInsertDoc(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
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

    public static Future<?> execInsertCreate(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
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

    //
    //
    //

    public static Future<?> execUpdateDoc(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) {
        return execDelete(sync, cmd, o, jsonBody, receive);
    }

    public static Future<?> execUpdateByQuery(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) {
        return execDelete(sync, cmd, o, jsonBody, receive);
    }

    public static Future<?> execDeleteByQuery(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) {
        return execDelete(sync, cmd, o, jsonBody, receive);
    }

    public static Future<?> execDelete(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) {
        try {
            Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
            if (jsonBody != null) {
                ObjectMapper mapper = new ObjectMapper();
                esRequest.setJsonEntity(mapper.writeValueAsString(jsonBody));
            }
            cmd.getClient().performRequest(esRequest);
            receive.responseUpdateCount(o.getRequest(), 1);
            return completed(sync);
        } catch (Exception e) {
            return failed(sync, new SQLException(e));
        }
    }
}
