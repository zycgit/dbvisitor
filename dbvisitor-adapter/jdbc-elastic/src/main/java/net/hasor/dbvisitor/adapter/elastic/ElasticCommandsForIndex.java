package net.hasor.dbvisitor.adapter.elastic;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser.IndexContext;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser.IndexMgmtPathContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class ElasticCommandsForIndex extends ElasticCommands {
    public static Future<?> execAliases(Future<Object> sync, ElasticCmd elasticCmd, IndexContext c, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic aliases command not implemented."));
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
