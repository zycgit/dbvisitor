package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MongoCommandsForCollection {
    public static Future<?> execCreateCollection(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CreateCollectionOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execGetCollectionNames(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, GetCollectionNamesOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execGetCollectionInfos(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, GetCollectionInfosOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execShowCollections(Future<Object> sync, MongoCmd mongoCmd, CommandContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execInsert(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, InsertOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execRemove(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, RemoveOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execUpdate(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, UpdateOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execFind(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, FindOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execCount(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, CountOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execDistinct(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, DistinctOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execAggregate(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, AggregateOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execBulkWrite(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, BulkWriteOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }
}
