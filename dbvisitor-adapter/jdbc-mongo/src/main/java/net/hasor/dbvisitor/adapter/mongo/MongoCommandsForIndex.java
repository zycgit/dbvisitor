package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MongoCommandsForIndex extends MongoCommands {
    public static Future<?> execCreateIndex(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, CreateIndexOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execDropIndex(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, DropIndexOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execGetIndexes(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, GetIndexesOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }
}
