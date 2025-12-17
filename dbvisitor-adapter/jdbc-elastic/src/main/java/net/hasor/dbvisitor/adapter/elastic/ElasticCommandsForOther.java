package net.hasor.dbvisitor.adapter.elastic;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.elastic.parser.ElasticParser.GenericContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class ElasticCommandsForOther extends ElasticCommands {
    public static Future<?> execGeneric(Future<Object> sync, ElasticCmd elasticCmd, GenericContext c, AdapterRequest request, AdapterReceive receive, AtomicInteger argIndex, Map<String, Object> hints, ElasticConn conn) throws SQLException {
        return failed(sync, new SQLException("Elastic generic command not implemented."));
    }
}
