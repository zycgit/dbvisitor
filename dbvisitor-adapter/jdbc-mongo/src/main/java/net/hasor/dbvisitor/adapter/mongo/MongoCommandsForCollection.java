package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MongoCommandsForCollection extends MongoCommands {
    public static Future<?> execCreateCollection(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CreateCollectionOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execGetCollectionNames(Future<Object> sync, MongoCmd mongoCmd, MongoParser.GetCollectionNamesOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execGetCollectionInfos(Future<Object> sync, MongoCmd mongoCmd, MongoParser.GetCollectionInfosOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execShowCollections(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execInsert(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CollectionContext collection, MongoParser.InsertOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execRemove(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CollectionContext collection, MongoParser.RemoveOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execUpdate(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CollectionContext collection, MongoParser.UpdateOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execFind(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CollectionContext collection, MongoParser.FindOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execCount(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CollectionContext collection, MongoParser.CountOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execDistinct(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CollectionContext collection, MongoParser.DistinctOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execAggregate(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CollectionContext collection, MongoParser.AggregateOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execBulkWrite(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CollectionContext collection, MongoParser.BulkWriteOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }
}
