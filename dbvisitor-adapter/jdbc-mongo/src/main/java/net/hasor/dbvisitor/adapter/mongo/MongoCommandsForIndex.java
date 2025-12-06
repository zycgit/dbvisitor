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

@SuppressWarnings("unchecked")
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

            Boolean background = getOptionBoolean(opts, "background");
            if (background != null) {
                options.background(background);
            }
            Boolean unique = getOptionBoolean(opts, "unique");
            if (unique != null) {
                options.unique(unique);
            }
            String name = getOptionString(opts, "name");
            if (name != null) {
                options.name(name);
            }
            Boolean sparse = getOptionBoolean(opts, "sparse");
            if (sparse != null) {
                options.sparse(sparse);
            }
            Long expireAfterSeconds = getOptionLong(opts, "expireAfterSeconds");
            if (expireAfterSeconds != null) {
                options.expireAfter(expireAfterSeconds, TimeUnit.SECONDS);
            }
            if (opts.containsKey("storageEngine")) {
                Object val = opts.get("storageEngine");
                if (val instanceof Bson) {
                    options.storageEngine((Bson) val);
                } else if (val instanceof Map) {
                    options.storageEngine(new Document((Map<String, Object>) val));
                } else {
                    throw new SQLException("storageEngine must be object");
                }
            }
            if (opts.containsKey("weights")) {
                Object val = opts.get("weights");
                if (val instanceof Bson) {
                    options.weights((Bson) val);
                } else if (val instanceof Map) {
                    options.weights(new Document((Map<String, Object>) val));
                } else {
                    throw new SQLException("weights must be object");
                }
            }
            String defaultLanguage = getOptionString(opts, "default_language");
            if (defaultLanguage != null) {
                options.defaultLanguage(defaultLanguage);
            }
            String languageOverride = getOptionString(opts, "language_override");
            if (languageOverride != null) {
                options.languageOverride(languageOverride);
            }
            Integer textIndexVersion = getOptionInt(opts, "textIndexVersion");
            if (textIndexVersion != null) {
                options.textVersion(textIndexVersion);
            }
            Integer sphereIndexVersion = getOptionInt(opts, "2dsphereIndexVersion");
            if (sphereIndexVersion != null) {
                options.sphereVersion(sphereIndexVersion);
            }
            Integer bits = getOptionInt(opts, "bits");
            if (bits != null) {
                options.bits(bits);
            }
            Double min = getOptionDouble(opts, "min");
            if (min != null) {
                options.min(min);
            }
            Double max = getOptionDouble(opts, "max");
            if (max != null) {
                options.max(max);
            }
            if (opts.containsKey("partialFilterExpression")) {
                Object val = opts.get("partialFilterExpression");
                if (val instanceof Bson) {
                    options.partialFilterExpression((Bson) val);
                } else if (val instanceof Map) {
                    options.partialFilterExpression(new Document((Map<String, Object>) val));
                } else {
                    throw new SQLException("partialFilterExpression must be object");
                }
            }
            Map<String, Object> collation = getOptionMap(opts, "collation");
            if (collation != null) {
                options.collation(jsonb2Collation(collation));
            }
            Boolean hidden = getOptionBoolean(opts, "hidden");
            if (hidden != null) {
                options.hidden(hidden);
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
                COL_IDX_UNIQUE_BOOLEAN, COL_IDX_SPARSE_BOOLEAN, COL_IDX_BACKGROUND_BOOLEAN, COL_IDX_HIDDEN_BOOLEAN, COL_JSON_STRING);

        AdapterResultCursor result = new AdapterResultCursor(request, columns);
        long maxRows = request.getMaxRows();
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
            row.put(COL_JSON_STRING.name, doc.toJson());
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
