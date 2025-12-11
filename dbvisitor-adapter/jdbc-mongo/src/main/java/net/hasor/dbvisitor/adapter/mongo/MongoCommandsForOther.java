package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoBsonVisitor;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import org.bson.Document;

class MongoCommandsForOther extends MongoCommands {
    public static Future<?> execRunCommand(Future<Object> sync, MongoCmd mongoCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx,//
            HintCommandContext h, DatabaseNameContext database, RunCommandOpContext c) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        Map<String, Object> hint = readHints(argIndex, request, h.hint());
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        List<Object> args = (List<Object>) new MongoBsonVisitor(request, argIndex).visit(c.arguments());

        if (args == null || args.isEmpty()) {
            throw new SQLException("runCommand requires at least one argument");
        }
        Document command;
        Object firstArg = toObjBson(args.get(0));
        if (firstArg instanceof Document) {
            command = (Document) firstArg;
        } else {
            throw new SQLException("runCommand argument must be a Document, got " + (firstArg == null ? "null" : firstArg.getClass().getName()));
        }

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        Document result = mongoDB.runCommand(command);
        AdapterResultCursor cursor = new AdapterResultCursor(request, Arrays.asList(COL_ID_STRING, COL_JSON_STRING));
        cursor.pushData(CollectionUtils.asMap(COL_ID_STRING.name, hexObjectId(result), COL_JSON_STRING.name, result.toJson()));
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execShowProfile(Future<Object> sync, MongoCmd mongoCmd, HintCommandContext c, AdapterRequest request, //
            AdapterReceive receive, int startArgIdx) throws SQLException {
        String dbName = mongoCmd.getCatalog();

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> collection = mongoDB.getCollection("system.profile");

        FindIterable<Document> find = collection.find().sort(new Document("$natural", -1));
        if (request.getMaxRows() > 0) {
            find.limit((int) request.getMaxRows());
        }

        AdapterResultCursor cursor = new AdapterResultCursor(request, Arrays.asList(COL_ID_STRING, COL_JSON_STRING));
        for (Document doc : find) {
            cursor.pushData(CollectionUtils.asMap(COL_ID_STRING.name, hexObjectId(doc), COL_JSON_STRING.name, doc.toJson()));
        }
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execServerStatus(Future<Object> sync, MongoCmd mongoCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx,//
            HintCommandContext h, DatabaseNameContext database, ServerStatusOpContext c) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        Map<String, Object> hint = readHints(argIndex, request, h.hint());
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        Document result = mongoDB.runCommand(new Document("serverStatus", 1));
        AdapterResultCursor cursor = new AdapterResultCursor(request, Arrays.asList(COL_ID_STRING, COL_JSON_STRING));
        cursor.pushData(CollectionUtils.asMap(COL_ID_STRING.name, hexObjectId(result), COL_JSON_STRING.name, result.toJson()));
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execVersion(Future<Object> sync, MongoCmd mongoCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx,//
            HintCommandContext h, DatabaseNameContext database, VersionOpContext c) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        Map<String, Object> hint = readHints(argIndex, request, h.hint());
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        Document result = mongoDB.runCommand(new Document("buildInfo", 1));

        AdapterResultCursor cursor = new AdapterResultCursor(request, Arrays.asList(COL_ID_STRING, COL_JSON_STRING));
        cursor.pushData(CollectionUtils.asMap(COL_ID_STRING.name, hexObjectId(result), COL_JSON_STRING.name, result.toJson()));
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execStats(Future<Object> sync, MongoCmd mongoCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx,//
            HintCommandContext h, DatabaseNameContext database, StatsOpContext c) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        Map<String, Object> hint = readHints(argIndex, request, h.hint());
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        Document result = mongoDB.runCommand(new Document("dbStats", 1));

        AdapterResultCursor cursor = new AdapterResultCursor(request, Arrays.asList(COL_ID_STRING, COL_JSON_STRING));
        cursor.pushData(CollectionUtils.asMap(COL_ID_STRING.name, hexObjectId(result), COL_JSON_STRING.name, result.toJson()));
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }
}
