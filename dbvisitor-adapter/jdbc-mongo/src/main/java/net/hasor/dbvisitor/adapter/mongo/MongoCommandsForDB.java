package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.CommandContext;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.DatabaseNameContext;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.DropDatabaseOpContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MongoCommandsForDB {
    public static Future<?> execDropDatabase(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, DropDatabaseOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execShowDbs(Future<Object> sync, MongoCmd mongoCmd, CommandContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }
}
