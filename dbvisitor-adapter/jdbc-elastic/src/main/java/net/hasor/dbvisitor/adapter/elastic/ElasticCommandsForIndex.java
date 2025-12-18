package net.hasor.dbvisitor.adapter.elastic;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.databind.JsonNode;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser.IndexContext;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser.IndexMgmtPathContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

class ElasticCommandsForIndex extends ElasticCommands {
    public static Future<?> execAliases(Future<Object> sync, ElasticCmd elasticCmd, IndexContext c, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        try {
            Response response = elasticCmd.getClient().performRequest(new Request("GET", "/_aliases"));
            List<String> indexNames = new ArrayList<>();
            try (InputStream content = response.getEntity().getContent()) {
                JsonNode root = ((ElasticRequest) request).getJson().readTree(content);
                if (root != null && root.isObject()) {
                    root.fieldNames().forEachRemaining(indexNames::add);
                }
            }

            receive.responseResult(request, listResult(request, COL_NAME_STRING, indexNames));
            return completed(sync);
        } catch (Exception e) {
            return failed(sync, e);
        }
    }

    public static Future<?> execIndexMapping(Future<Object> sync, ElasticCmd elasticCmd, IndexContext c, IndexMgmtPathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic index mapping command not implemented."));
    }

    public static Future<?> execIndexSettings(Future<Object> sync, ElasticCmd elasticCmd, IndexContext c, IndexMgmtPathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic index settings command not implemented."));
    }

    public static Future<?> execIndexOpen(Future<Object> sync, ElasticCmd elasticCmd, IndexContext c, IndexMgmtPathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic index open command not implemented."));
    }

    public static Future<?> execIndexClose(Future<Object> sync, ElasticCmd elasticCmd, IndexContext c, IndexMgmtPathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic index close command not implemented."));
    }

    public static Future<?> execIndexGeneric(Future<Object> sync, ElasticCmd elasticCmd, IndexContext c, IndexMgmtPathContext path, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic index generic command not implemented."));
    }
}
