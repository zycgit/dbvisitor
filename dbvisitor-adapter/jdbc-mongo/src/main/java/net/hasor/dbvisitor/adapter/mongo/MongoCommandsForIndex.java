package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoBsonVisitor;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import net.hasor.dbvisitor.driver.JdbcColumn;
import org.bson.Document;
import org.bson.conversions.Bson;

class MongoCommandsForIndex extends MongoCommands {
    public static Future<?> execCreateIndex(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, CreateIndexOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoColl = mongoDB.getCollection(collName);
        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        Bson keys = (Bson) args.get(0);
        IndexOptions options = new IndexOptions();
        if (args.size() > 1) {
            Map<String, Object> opts = (Map<String, Object>) args.get(1);
            if (!opts.containsKey("name")) {
                throw new SQLException("The index name must be specified.");
            }

            if (opts.containsKey("background")) {
                options.background((Boolean) opts.get("background"));
            }
            if (opts.containsKey("unique")) {
                options.unique((Boolean) opts.get("unique"));
            }
            if (opts.containsKey("name")) {
                options.name((String) opts.get("name"));
            }
            if (opts.containsKey("sparse")) {
                options.sparse((Boolean) opts.get("sparse"));
            }
            if (opts.containsKey("expireAfterSeconds")) {
                options.expireAfter(((Number) opts.get("expireAfterSeconds")).longValue(), TimeUnit.SECONDS);
            }
            if (opts.containsKey("storageEngine")) {
                options.storageEngine((Bson) opts.get("storageEngine"));
            }
            if (opts.containsKey("weights")) {
                options.weights((Bson) opts.get("weights"));
            }
            if (opts.containsKey("default_language")) {
                options.defaultLanguage((String) opts.get("default_language"));
            }
            if (opts.containsKey("language_override")) {
                options.languageOverride((String) opts.get("language_override"));
            }
            if (opts.containsKey("textIndexVersion")) {
                options.textVersion(((Number) opts.get("textIndexVersion")).intValue());
            }
            if (opts.containsKey("2dsphereIndexVersion")) {
                options.sphereVersion(((Number) opts.get("2dsphereIndexVersion")).intValue());
            }
            if (opts.containsKey("bits")) {
                options.bits(((Number) opts.get("bits")).intValue());
            }
            if (opts.containsKey("min")) {
                options.min(((Number) opts.get("min")).doubleValue());
            }
            if (opts.containsKey("max")) {
                options.max(((Number) opts.get("max")).doubleValue());
            }
            if (opts.containsKey("partialFilterExpression")) {
                options.partialFilterExpression((Bson) opts.get("partialFilterExpression"));
            }
            if (opts.containsKey("collation")) {
                options.collation(jsonb2Collation((Map<String, Object>) opts.get("collation")));
            }
            if (opts.containsKey("hidden")) {
                options.hidden((Boolean) opts.get("hidden"));
            }
        } else {
            throw new SQLException("The index name must be specified.");
        }

        mongoColl.createIndex(keys, options);
        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }

    public static Future<?> execDropIndex(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, DropIndexOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoColl = mongoDB.getCollection(collName);
        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        Object index = args.get(0);
        if (index instanceof String) {
            mongoColl.dropIndex((String) index);
        } else {
            mongoColl.dropIndex((Bson) index);
        }

        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }

    public static Future<?> execGetIndexes(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, GetIndexesOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoColl = mongoDB.getCollection(collName);

        List<JdbcColumn> columns = Arrays.asList(COL_IDX_V_INT, COL_IDX_KEY_STRING, COL_IDX_NAME_STRING, COL_IDX_NS_STRING, //
                COL_IDX_UNIQUE_BOOLEAN, COL_IDX_SPARSE_BOOLEAN, COL_IDX_BACKGROUND_BOOLEAN, COL_IDX_HIDDEN_BOOLEAN, COL_JSON_);

        long maxRows = request.getMaxRows();
        AdapterResultCursor result = new AdapterResultCursor(request, columns);
        int affectRows = 0;
        for (Document doc : mongoColl.listIndexes()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(COL_IDX_V_INT.name, doc.get("v"));
            row.put(COL_IDX_KEY_STRING.name, doc.get("key") != null ? ((Document) doc.get("key")).toJson() : null);
            row.put(COL_IDX_NAME_STRING.name, doc.get("name"));
            row.put(COL_IDX_NS_STRING.name, doc.get("ns"));
            row.put(COL_IDX_UNIQUE_BOOLEAN.name, doc.get("unique"));
            row.put(COL_IDX_SPARSE_BOOLEAN.name, doc.get("sparse"));
            row.put(COL_IDX_BACKGROUND_BOOLEAN.name, doc.get("background"));
            row.put(COL_IDX_HIDDEN_BOOLEAN.name, doc.get("hidden"));
            row.put(COL_JSON_.name, doc.toJson());
            result.pushData(row);

            affectRows++;
            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        result.pushFinish();

        receive.responseResult(request, result);
        return completed(sync);
    }
}
