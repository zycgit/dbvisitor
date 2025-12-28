package net.hasor.dbvisitor.adapter.elastic;
import java.sql.SQLException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.AdapterReceive;
import org.elasticsearch.client.Request;

class ElasticCommandsForCrud extends ElasticCommands {
    public static Future<?> execInsertDoc(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) {
        return execGeneric(sync, elasticCmd, operation, jsonBody, receive);
    }

    public static Future<?> execInsertCreate(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) {
        return execGeneric(sync, elasticCmd, operation, jsonBody, receive);
    }

    public static Future<?> execUpdateDoc(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) {
        return execGeneric(sync, elasticCmd, operation, jsonBody, receive);
    }

    public static Future<?> execUpdateByQuery(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) {
        return execGeneric(sync, elasticCmd, operation, jsonBody, receive);
    }

    public static Future<?> execDeleteByQuery(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) {
        return execGeneric(sync, elasticCmd, operation, jsonBody, receive);
    }

    public static Future<?> execGeneric(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) {
        try {
            Request esRequest = new Request(operation.getMethod().name(), operation.getEndpoint());
            if (jsonBody != null) {
                ObjectMapper mapper = new ObjectMapper();
                esRequest.setJsonEntity(mapper.writeValueAsString(jsonBody));
            }
            elasticCmd.getClient().performRequest(esRequest);
            receive.responseUpdateCount(operation.getRequest(), 1);
            return completed(sync);
        } catch (Exception e) {
            return failed(sync, new SQLException(e));
        }
    }

    public static Future<?> execMultiSearch(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) {
        return failed(sync, new SQLException("Elastic msearch command not implemented."));
    }

    public static Future<?> execCount(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) {
        return failed(sync, new SQLException("Elastic count command not implemented."));
    }

    public static Future<?> execSearch(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) {
        return failed(sync, new SQLException("Elastic search command not implemented."));
    }
}
