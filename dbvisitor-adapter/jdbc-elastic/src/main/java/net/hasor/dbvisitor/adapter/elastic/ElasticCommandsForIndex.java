package net.hasor.dbvisitor.adapter.elastic;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.JdbcColumn;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

class ElasticCommandsForIndex extends ElasticCommands {
    // POST or GET /_aliases
    public static Future<?> execAliases(Future<Object> sync, ElasticCmd cmd, ElasticOperation o, Object jsonBody, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(o.getMethod().name(), o.getEndpoint());
        if (jsonBody != null) {
            ObjectMapper mapper = new ObjectMapper();
            esRequest.setJsonEntity(mapper.writeValueAsString(jsonBody));
        }

        if (o.getMethod() == ElasticHttpMethod.GET) {
            Response response = cmd.getClient().performRequest(esRequest);
            try (InputStream content = response.getEntity().getContent()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(content);
                List<String> indices = new ArrayList<>();
                Iterator<String> fieldNames = root.fieldNames();
                while (fieldNames.hasNext()) {
                    indices.add(fieldNames.next());
                }
                receive.responseResult(o.getRequest(), listResult(o.getRequest(), new JdbcColumn("NAME", "VARCHAR", "", "", ""), indices));
            }
        } else {
            cmd.getClient().performRequest(esRequest);
            receive.responseUpdateCount(o.getRequest(), 1);
        }
        return completed(sync);
    }

    public static Future<?> execIndexOpen(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(ElasticHttpMethod.POST.name(), operation.getEndpoint());
        elasticCmd.getClient().performRequest(esRequest);
        receive.responseUpdateCount(operation.getRequest(), 1);
        return completed(sync);
    }

    public static Future<?> execIndexClose(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, AdapterReceive receive) throws Exception {
        Request esRequest = new Request(ElasticHttpMethod.POST.name(), operation.getEndpoint());
        elasticCmd.getClient().performRequest(esRequest);
        receive.responseUpdateCount(operation.getRequest(), 1);
        return completed(sync);
    }

    public static Future<?> execIndexMapping(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) throws Exception {
        try {
            Request esRequest = new Request(operation.getMethod().name(), operation.getEndpoint());
            if (jsonBody != null) {
                ObjectMapper mapper = new ObjectMapper();
                esRequest.setJsonEntity(mapper.writeValueAsString(jsonBody));
            }

            if (operation.getMethod() == ElasticHttpMethod.GET) {
                Response response = elasticCmd.getClient().performRequest(esRequest);
                try (InputStream content = response.getEntity().getContent()) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(content);
                    receive.responseResult(operation.getRequest(), singleResult(operation.getRequest(), COL_JSON_STRING, root.toString()));
                }
            } else {
                elasticCmd.getClient().performRequest(esRequest);
                receive.responseUpdateCount(operation.getRequest(), 1);
            }
            return completed(sync);
        } catch (Exception e) {
            return failed(sync, new SQLException(e));
        }
    }

    public static Future<?> execIndexSettings(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) throws Exception {
        try {
            Request esRequest = new Request(operation.getMethod().name(), operation.getEndpoint());
            if (jsonBody != null) {
                ObjectMapper mapper = new ObjectMapper();
                esRequest.setJsonEntity(mapper.writeValueAsString(jsonBody));
            }

            if (operation.getMethod() == ElasticHttpMethod.GET) {
                Response response = elasticCmd.getClient().performRequest(esRequest);
                try (InputStream content = response.getEntity().getContent()) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(content);
                    receive.responseResult(operation.getRequest(), singleResult(operation.getRequest(), COL_JSON_STRING, root.toString()));
                }
            } else {
                elasticCmd.getClient().performRequest(esRequest);
                receive.responseUpdateCount(operation.getRequest(), 1);
            }
            return completed(sync);
        } catch (Exception e) {
            return failed(sync, new SQLException(e));
        }
    }
}
