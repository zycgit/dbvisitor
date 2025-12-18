package net.hasor.dbvisitor.adapter.elastic;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

class ElasticCommandsForCrud extends ElasticCommands {
    public static Future<?> execSearch(Future<Object> sync, ElasticCmd elasticCmd, SelectContext c, SelectPathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic search command not implemented."));
    }

    public static Future<?> execCount(Future<Object> sync, ElasticCmd elasticCmd, SelectContext c, SelectPathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic count command not implemented."));
    }

    public static Future<?> execMultiSearch(Future<Object> sync, ElasticCmd elasticCmd, SelectContext c, SelectPathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic multi-search command not implemented."));
    }

    public static Future<?> execSelectGeneric(Future<Object> sync, ElasticCmd elasticCmd, SelectContext c, SelectPathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic select generic command not implemented."));
    }

    public static Future<?> execSelectPath(Future<Object> sync, ElasticCmd elasticCmd, SelectContext c, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        String endpoint = c.path().getText();
        if (endpoint.equals("/_aliases") || endpoint.startsWith("/_aliases?")) {
             return ElasticCommandsForIndex.execAliases(sync, elasticCmd, null, request, receive, argIndex, hints, conn);
        }
        return failed(sync, new SQLException("Elastic select path command not implemented."));
    }

    public static Future<?> execInsertDoc(Future<Object> sync, ElasticCmd elasticCmd, InsertContext c, InsertPathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic insert doc command not implemented."));
    }

    public static Future<?> execInsertCreate(Future<Object> sync, ElasticCmd elasticCmd, InsertContext c, InsertPathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic insert create command not implemented."));
    }

    public static Future<?> execInsertGeneric(Future<Object> sync, ElasticCmd elasticCmd, InsertContext c, InsertPathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic insert generic command not implemented."));
    }

    public static Future<?> execInsertPath(Future<Object> sync, ElasticCmd elasticCmd, InsertContext c, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        try {
            String method = "PUT";
            String endpoint = c.path().getText();
            Request esRequest = new Request(method, endpoint);
            if (c.json() != null) {
                esRequest.setJsonEntity(c.json().getText());
            }
            elasticCmd.getClient().performRequest(esRequest);
            receive.responseUpdateCount(request, 1);
            return completed(sync);
        } catch (Exception e) {
            return failed(sync, new SQLException(e));
        }
    }

    public static Future<?> execUpdateByQuery(Future<Object> sync, ElasticCmd elasticCmd, UpdateContext c, UpdatePathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic update by query command not implemented."));
    }

    public static Future<?> execUpdateDoc(Future<Object> sync, ElasticCmd elasticCmd, UpdateContext c, UpdatePathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic update doc command not implemented."));
    }

    public static Future<?> execUpdateGeneric(Future<Object> sync, ElasticCmd elasticCmd, UpdateContext c, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic update generic command not implemented."));
    }

    public static Future<?> execDeleteByQuery(Future<Object> sync, ElasticCmd elasticCmd, DeleteContext c, DeletePathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic delete by query command not implemented."));
    }

    public static Future<?> execDeleteDoc(Future<Object> sync, ElasticCmd elasticCmd, DeleteContext c, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        try {
            String method = "DELETE";
            String endpoint = c.path().getText();
            Request esRequest = new Request(method, endpoint);
            if (c.json() != null) {
                esRequest.setJsonEntity(c.json().getText());
            }
            elasticCmd.getClient().performRequest(esRequest);
            receive.responseUpdateCount(request, 1);
            return completed(sync);
        } catch (Exception e) {
            return failed(sync, new SQLException(e));
        }
    }
}
