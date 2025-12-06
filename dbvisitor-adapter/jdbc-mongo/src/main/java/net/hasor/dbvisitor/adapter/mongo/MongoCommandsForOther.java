package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoBsonVisitor;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import org.bson.Document;

class MongoCommandsForOther extends MongoCommands {
    public static Future<?> execRunCommand(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, RunCommandOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName;
        if (database != null) {
            dbName = argAsDbName(argIndex, request, database, mongoCmd);
        } else {
            dbName = mongoCmd.getCatalog();
        }
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        Object args = visitor.visit(c.arguments());
        Document command;
        if (args instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) args;
            if (list.isEmpty()) {
                throw new SQLException("runCommand requires at least one argument");
            }
            Object firstArg = toObjBson(list.get(0));
            if (firstArg instanceof Document) {
                command = (Document) firstArg;
            } else {
                throw new SQLException("runCommand argument must be a Document, got " + (firstArg == null ? "null" : firstArg.getClass().getName()));
            }
        } else {
            throw new SQLException("Unexpected return type from visitor: " + (args == null ? "null" : args.getClass().getName()));
        }

        Document result = mongoDB.runCommand(command);
        AdapterResultCursor cursor = new AdapterResultCursor(request, Collections.singletonList(COL_JSON_STRING));
        cursor.pushData(Collections.singletonMap(COL_JSON_STRING.name, result.toJson()));
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execShowProfile(Future<Object> sync, MongoCmd mongoCmd, CommandContext c, AdapterRequest request, //
            AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        String dbName = mongoCmd.getCatalog();
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> collection = mongoDB.getCollection("system.profile");

        FindIterable<Document> find = collection.find().sort(new Document("$natural", -1));
        if (request.getMaxRows() > 0) {
            find.limit((int) request.getMaxRows());
        }

        AdapterResultCursor cursor = new AdapterResultCursor(request, Collections.singletonList(COL_JSON_STRING));
        for (Document doc : find) {
            cursor.pushData(Collections.singletonMap(COL_JSON_STRING.name, doc.toJson()));
        }
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execServerStatus(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, ServerStatusOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName;
        if (database != null) {
            dbName = argAsDbName(argIndex, request, database, mongoCmd);
        } else {
            dbName = mongoCmd.getCatalog();
        }
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        Document result = mongoDB.runCommand(new Document("serverStatus", 1));
        AdapterResultCursor cursor = new AdapterResultCursor(request, Collections.singletonList(COL_JSON_STRING));
        cursor.pushData(Collections.singletonMap(COL_JSON_STRING.name, result.toJson()));
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execVersion(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, VersionOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName;
        if (database != null) {
            dbName = argAsDbName(argIndex, request, database, mongoCmd);
        } else {
            dbName = mongoCmd.getCatalog();
        }
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        Document result = mongoDB.runCommand(new Document("buildInfo", 1));
        AdapterResultCursor cursor = new AdapterResultCursor(request, Collections.singletonList(COL_JSON_STRING));
        cursor.pushData(Collections.singletonMap(COL_JSON_STRING.name, result.toJson()));
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execStats(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, StatsOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName;
        if (database != null) {
            dbName = argAsDbName(argIndex, request, database, mongoCmd);
        } else {
            dbName = mongoCmd.getCatalog();
        }
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        Document result = mongoDB.runCommand(new Document("dbStats", 1));
        AdapterResultCursor cursor = new AdapterResultCursor(request, Collections.singletonList(COL_JSON_STRING));
        cursor.pushData(Collections.singletonMap(COL_JSON_STRING.name, result.toJson()));
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }
}
