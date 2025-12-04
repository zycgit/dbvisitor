package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoBsonVisitor;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.*;
import net.hasor.dbvisitor.driver.*;
import org.bson.Document;
import org.bson.conversions.Bson;

@SuppressWarnings("unchecked")
class MongoCommandsForCollection extends MongoCommands {
    public static Future<?> execCreateCollection(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CreateCollectionOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);

        List<?> args = (List<?>) new MongoBsonVisitor(request, argIndex).visit(c.arguments());
        CreateCollectionOptions options = null;
        if (args.size() > 1) {
            Map<String, Object> bson = (Map<String, Object>) args.get(1);

            if (bson.containsKey("viewOn")) {
                throw new SQLException("The 'viewOn' option is not supported in db.createCollection.");
            }

            ValidationOptions validation = new ValidationOptions();
            boolean hasValidation = false;
            if (bson.containsKey("validator")) {
                validation.validator((Bson) bson.get("validator"));
                hasValidation = true;
            }
            if (bson.containsKey("validationLevel")) {
                validation.validationLevel(ValidationLevel.valueOf(((String) bson.get("validationLevel")).toUpperCase()));
                hasValidation = true;
            }
            if (bson.containsKey("validationAction")) {
                validation.validationAction(ValidationAction.valueOf(((String) bson.get("validationAction")).toUpperCase()));
                hasValidation = true;
            }

            options = new CreateCollectionOptions();
            if (bson.containsKey("capped")) {
                options.capped((Boolean) bson.get("capped"));
            }
            if (bson.containsKey("size")) {
                options.sizeInBytes(((Number) bson.get("size")).longValue());
            }
            if (bson.containsKey("max")) {
                options.maxDocuments(((Number) bson.get("max")).longValue());
            }
            if (bson.containsKey("storageEngine")) {
                options.storageEngineOptions((Bson) bson.get("storageEngine"));
            }
            if (hasValidation) {
                options.validationOptions(validation);
            }
            if (bson.containsKey("collation")) {
                options.collation(jsonb2Collation((Map<String, Object>) bson.get("collation")));
            }
            if (bson.containsKey("expireAfterSeconds")) {
                options.expireAfter(((Number) bson.get("expireAfterSeconds")).longValue(), TimeUnit.SECONDS);
            }

            if (bson.containsKey("timeseries")) {
                Map<String, Object> tsMap = (Map<String, Object>) bson.get("timeseries");
                TimeSeriesOptions tsOptions = new TimeSeriesOptions((String) tsMap.get("timeField"));
                if (tsMap.containsKey("metaField")) {
                    tsOptions.metaField((String) tsMap.get("metaField"));
                }
                if (tsMap.containsKey("granularity")) {
                    tsOptions.granularity(TimeSeriesGranularity.valueOf(((String) tsMap.get("granularity")).toUpperCase()));
                }
                if (tsMap.containsKey("bucketMaxSpanSeconds")) {
                    tsOptions.bucketMaxSpan(((Number) tsMap.get("bucketMaxSpanSeconds")).longValue(), TimeUnit.SECONDS);
                }
                if (tsMap.containsKey("bucketRoundingSeconds")) {
                    tsOptions.bucketRounding(((Number) tsMap.get("bucketRoundingSeconds")).longValue(), TimeUnit.SECONDS);
                }
                options.timeSeriesOptions(tsOptions);
            }

            if (bson.containsKey("clusteredIndex")) {
                Map<String, Object> ciMap = (Map<String, Object>) bson.get("clusteredIndex");
                Bson key = (Bson) ciMap.get("key");
                boolean unique = (Boolean) ciMap.get("unique");
                org.bson.BsonDocument keyDoc;
                if (key instanceof org.bson.BsonDocument) {
                    keyDoc = (org.bson.BsonDocument) key;
                } else {
                    keyDoc = key.toBsonDocument(org.bson.BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
                }

                ClusteredIndexOptions ciOptions = new ClusteredIndexOptions(keyDoc, unique);
                if (ciMap.containsKey("name")) {
                    ciOptions.name((String) ciMap.get("name"));
                }
                options.clusteredIndexOptions(ciOptions);
            }

            if (bson.containsKey("changeStreamPreAndPostImages")) {
                Map<String, Object> csMap = (Map<String, Object>) bson.get("changeStreamPreAndPostImages");
                boolean enabled = (Boolean) csMap.get("enabled");
                options.changeStreamPreAndPostImagesOptions(new ChangeStreamPreAndPostImagesOptions(enabled));
            }

            if (bson.containsKey("indexOptionDefaults")) {
                Map<String, Object> iodMap = (Map<String, Object>) bson.get("indexOptionDefaults");
                IndexOptionDefaults iod = new IndexOptionDefaults();
                if (iodMap.containsKey("storageEngine")) {
                    iod.storageEngine((Bson) iodMap.get("storageEngine"));
                }
                options.indexOptionDefaults(iod);
            }
        }

        String collectionName = (String) args.get(0);
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        if (options != null) {
            mongoDB.createCollection(collectionName, options);
        } else {
            mongoDB.createCollection(collectionName);
        }

        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }

    public static Future<?> execGetCollectionNames(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, GetCollectionNamesOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        List<String> collNames = new ArrayList<>();
        for (String name : mongoDB.listCollectionNames()) {
            collNames.add(name);
        }

        receive.responseResult(request, listResult(request, COL_COLLECTION_STRING, collNames));
        return completed(sync);
    }

    public static Future<?> execGetCollectionInfos(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, GetCollectionInfosOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        // fetch data
        long maxRows = request.getMaxRows();
        int affectRows = 0;
        List<Document> collections = new ArrayList<>();
        for (Document doc : mongoDB.listCollections()) {
            collections.add(doc);

            affectRows++;
            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }

        // convert data
        JdbcColumn colName = new JdbcColumn("NAME", AdapterType.String, "", "", "");
        JdbcColumn colType = new JdbcColumn("TYPE", AdapterType.String, "", "", "");
        JdbcColumn colOptions = new JdbcColumn("OPTIONS", AdapterType.String, "", "", "");
        JdbcColumn colInfo = new JdbcColumn("INFO", AdapterType.String, "", "", "");

        List<JdbcColumn> columns = Arrays.asList(colName, colType, colOptions, colInfo);
        AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
        for (Document doc : collections) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(colName.name, doc.getString("name"));
            row.put(colType.name, doc.getString("type"));
            row.put(colOptions.name, doc.get("options") != null ? doc.get("options").toString() : null);
            row.put(colInfo.name, doc.toJson());
            cursor.pushData(row);
        }
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execShowCollections(Future<Object> sync, MongoCmd mongoCmd, CommandContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(mongoCmd.getCatalog());

        List<String> collNames = new ArrayList<>();
        for (String name : mongoDB.listCollectionNames()) {
            collNames.add(name);
        }

        receive.responseResult(request, listResult(request, COL_COLLECTION_STRING, collNames));
        return completed(sync);
    }

    public static Future<?> execCreateView(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CreateViewOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        MongoDatabase mongoDatabase = mongoCmd.getClient().getDatabase(dbName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        String viewName = (String) args.get(0);
        String viewOn = (String) args.get(1);
        List<? extends org.bson.conversions.Bson> pipeline = (List<? extends org.bson.conversions.Bson>) args.get(2);

        if (args.size() > 3) {
            Map<String, Object> options = (Map<String, Object>) args.get(3);
            CreateViewOptions createOptions = new CreateViewOptions();
            if (options.containsKey("collation")) {
                createOptions.collation(jsonb2Collation((Map<String, Object>) options.get("collation")));
            }
            mongoDatabase.createView(viewName, viewOn, pipeline, createOptions);
        } else {
            mongoDatabase.createView(viewName, viewOn, pipeline);
        }

        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }

    //
    //
    //

    public static Future<?> execInsert(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, InsertOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);
        MongoDatabase mongoDatabase = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        Object docOrList = args.get(0);
        if (docOrList instanceof List) {
            mongoCollection.insertMany((List<Document>) docOrList);
            receive.responseUpdateCount(request, ((List) docOrList).size());
        } else {
            mongoCollection.insertOne((Document) docOrList);
            receive.responseUpdateCount(request, 1);
        }
        return completed(sync);
    }

    public static Future<?> execUpdate(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, UpdateOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);
        MongoDatabase mongoDatabase = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        Bson filter = (Bson) args.get(0);
        Bson update = (Bson) args.get(1);
        UpdateOptions options = new UpdateOptions();
        boolean multi = false;

        if (args.size() > 2) {
            Map<String, Object> opts = (Map<String, Object>) args.get(2);
            if (opts.containsKey("upsert")) {
                options.upsert((Boolean) opts.get("upsert"));
            }
            if (opts.containsKey("multi")) {
                multi = (Boolean) opts.get("multi");
            }
            if (opts.containsKey("collation")) {
                options.collation(jsonb2Collation((Map<String, Object>) opts.get("collation")));
            }
            if (opts.containsKey("arrayFilters")) {
                options.arrayFilters((List<? extends Bson>) opts.get("arrayFilters"));
            }
        }

        UpdateResult result;
        if (multi) {
            result = mongoCollection.updateMany(filter, update, options);
        } else {
            result = mongoCollection.updateOne(filter, update, options);
        }
        receive.responseUpdateCount(request, (int) result.getModifiedCount());
        return completed(sync);
    }

    public static Future<?> execRemove(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, RemoveOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);
        MongoDatabase mongoDatabase = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        Bson filter = (Bson) args.get(0);
        boolean justOne = false;
        if (args.size() > 1) {
            Object arg1 = args.get(1);
            if (arg1 instanceof Boolean) {
                justOne = (Boolean) arg1;
            } else if (arg1 instanceof Map) {
                Map<String, Object> options = (Map<String, Object>) arg1;
                if (options.containsKey("justOne")) {
                    justOne = (Boolean) options.get("justOne");
                }
            }
        }

        DeleteResult result;
        if (justOne) {
            result = mongoCollection.deleteOne(filter);
        } else {
            result = mongoCollection.deleteMany(filter);
        }
        receive.responseUpdateCount(request, (int) result.getDeletedCount());
        return completed(sync);
    }

    public static Future<?> execFind(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, FindOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);
        MongoDatabase mongoDatabase = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        Bson filter = (args.size() > 0) ? (Bson) args.get(0) : new Document();
        Bson projection = (args.size() > 1) ? (Bson) args.get(1) : null;

        FindIterable<Document> findIterable = mongoCollection.find(filter);
        if (projection != null) {
            findIterable.projection(projection);
        }

        if (c.methodCall() != null) {
            for (MethodCallContext method : c.methodCall()) {
                if (method instanceof LimitOpContext) {
                    List<Object> limitArgs = (List<Object>) visitor.visit(((LimitOpContext) method).arguments());
                    findIterable.limit(((Number) limitArgs.get(0)).intValue());
                } else if (method instanceof SkipOpContext) {
                    List<Object> skipArgs = (List<Object>) visitor.visit(((SkipOpContext) method).arguments());
                    findIterable.skip(((Number) skipArgs.get(0)).intValue());
                } else if (method instanceof SortOpContext) {
                    List<Object> sortArgs = (List<Object>) visitor.visit(((SortOpContext) method).arguments());
                    findIterable.sort((Bson) sortArgs.get(0));
                } else if (method instanceof HintOpContext) {
                    List<Object> hintArgs = (List<Object>) visitor.visit(((HintOpContext) method).arguments());
                    findIterable.hint((Bson) hintArgs.get(0));
                }
            }
        }

        JdbcColumn colJson = new JdbcColumn("json", AdapterType.String, "", "", "");
        AdapterResultCursor cursor = new AdapterResultCursor(request, Arrays.asList(colJson));
        for (Document doc : findIterable) {
            cursor.pushData(Collections.singletonMap("json", doc.toJson()));
        }
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execBulkWrite(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, BulkWriteOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    //

    public static Future<?> execCount(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, CountOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);
        MongoDatabase mongoDatabase = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        Bson filter = (args.size() > 0) ? (Bson) args.get(0) : new Document();
        long count = mongoCollection.countDocuments(filter);

        JdbcColumn colCount = new JdbcColumn("count", AdapterType.Int, "", "", "");
        AdapterResultCursor cursor = new AdapterResultCursor(request, Arrays.asList(colCount));
        cursor.pushData(Collections.singletonMap("count", count));
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execDistinct(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, DistinctOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);
        MongoDatabase mongoDatabase = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        String fieldName = (String) args.get(0);
        Bson filter = (args.size() > 1) ? (Bson) args.get(1) : new Document();

        DistinctIterable<Object> distinct = mongoCollection.distinct(fieldName, filter, Object.class);

        JdbcColumn colValue = new JdbcColumn("value", AdapterType.String, "", "", "");
        AdapterResultCursor cursor = new AdapterResultCursor(request, Arrays.asList(colValue));
        for (Object val : distinct) {
            cursor.pushData(Collections.singletonMap("value", val));
        }
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execAggregate(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, AggregateOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);
        MongoDatabase mongoDatabase = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        List<Bson> pipeline = (List<Bson>) args.get(0);
        AggregateIterable<Document> aggregate = mongoCollection.aggregate(pipeline);

        JdbcColumn colJson = new JdbcColumn("json", AdapterType.String, "", "", "");
        AdapterResultCursor cursor = new AdapterResultCursor(request, Arrays.asList(colJson));
        for (Document doc : aggregate) {
            cursor.pushData(Collections.singletonMap("json", doc.toJson()));
        }
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }
}
