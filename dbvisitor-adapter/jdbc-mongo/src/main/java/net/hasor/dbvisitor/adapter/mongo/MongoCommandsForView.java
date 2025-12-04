package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.CreateViewOpContext;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.DatabaseNameContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MongoCommandsForView {
    public static Future<?> execCreateView(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CreateViewOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }
}
