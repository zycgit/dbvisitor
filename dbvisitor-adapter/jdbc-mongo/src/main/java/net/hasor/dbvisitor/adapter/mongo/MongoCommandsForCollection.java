package net.hasor.dbvisitor.adapter.mongo;
import java.io.IOException;
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
            Boolean capped = getOptionBoolean(bson, "capped");
            if (capped != null) {
                options.capped(capped);
            }
            Long size = getOptionLong(bson, "size");
            if (size != null) {
                options.sizeInBytes(size);
            }
            Long max = getOptionLong(bson, "max");
            if (max != null) {
                options.maxDocuments(max);
            }
            if (bson.containsKey("storageEngine")) {
                options.storageEngineOptions((Bson) bson.get("storageEngine"));
            }
            if (hasValidation) {
                options.validationOptions(validation);
            }
            Map<String, Object> collation = getOptionMap(bson, "collation");
            if (collation != null) {
                options.collation(jsonb2Collation(collation));
            }
            Long expireAfterSeconds = getOptionLong(bson, "expireAfterSeconds");
            if (expireAfterSeconds != null) {
                options.expireAfter(expireAfterSeconds, TimeUnit.SECONDS);
            }

            Map<String, Object> tsMap = getOptionMap(bson, "timeseries");
            if (tsMap != null) {
                TimeSeriesOptions tsOptions = new TimeSeriesOptions(getOptionString(tsMap, "timeField"));
                String metaField = getOptionString(tsMap, "metaField");
                if (metaField != null) {
                    tsOptions.metaField(metaField);
                }
                String granularity = getOptionString(tsMap, "granularity");
                if (granularity != null) {
                    tsOptions.granularity(TimeSeriesGranularity.valueOf(granularity.toUpperCase()));
                }
                Long bucketMaxSpanSeconds = getOptionLong(tsMap, "bucketMaxSpanSeconds");
                if (bucketMaxSpanSeconds != null) {
                    tsOptions.bucketMaxSpan(bucketMaxSpanSeconds, TimeUnit.SECONDS);
                }
                Long bucketRoundingSeconds = getOptionLong(tsMap, "bucketRoundingSeconds");
                if (bucketRoundingSeconds != null) {
                    tsOptions.bucketRounding(bucketRoundingSeconds, TimeUnit.SECONDS);
                }
                options.timeSeriesOptions(tsOptions);
            }

            Map<String, Object> ciMap = getOptionMap(bson, "clusteredIndex");
            if (ciMap != null) {
                Bson key = (Bson) ciMap.get("key");
                Boolean unique = getOptionBoolean(ciMap, "unique");
                org.bson.BsonDocument keyDoc;
                if (key instanceof org.bson.BsonDocument) {
                    keyDoc = (org.bson.BsonDocument) key;
                } else {
                    keyDoc = key.toBsonDocument(org.bson.BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
                }

                ClusteredIndexOptions ciOptions = new ClusteredIndexOptions(keyDoc, unique != null ? unique : false);
                String name = getOptionString(ciMap, "name");
                if (name != null) {
                    ciOptions.name(name);
                }
                options.clusteredIndexOptions(ciOptions);
            }

            Map<String, Object> csMap = getOptionMap(bson, "changeStreamPreAndPostImages");
            if (csMap != null) {
                Boolean enabled = getOptionBoolean(csMap, "enabled");
                options.changeStreamPreAndPostImagesOptions(new ChangeStreamPreAndPostImagesOptions(enabled != null ? enabled : false));
            }

            Map<String, Object> iodMap = getOptionMap(bson, "indexOptionDefaults");
            if (iodMap != null) {
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

    public static Future<?> execDrop(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, DropOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);

        MongoDatabase mongoDB = mongoCmd.getMongoDB(dbName);
        MongoCollection<Document> mongoColl = mongoDB.getCollection(collName);
        mongoColl.drop();

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
        List<JdbcColumn> columns = Arrays.asList(COL_NAME_STRING, COL_TYPE_STRING, COL_OPTIONS_STRING, COL_INFO_STRING);
        AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
        for (Document doc : collections) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(COL_NAME_STRING.name, doc.getString("name"));
            row.put(COL_TYPE_STRING.name, doc.getString("type"));
            row.put(COL_OPTIONS_STRING.name, doc.get("options") != null ? doc.get("options").toString() : null);
            row.put(COL_INFO_STRING.name, doc.toJson());
            cursor.pushData(row);
        }
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execShowCollections(Future<Object> sync, MongoCmd mongoCmd, CommandContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(mongoCmd.getCatalog());
        ListCollectionNamesIterable collNames = mongoDB.listCollectionNames();

        receive.responseResult(request, listResult(request, COL_COLLECTION_STRING, collNames));
        return completed(sync);
    }

    public static Future<?> execCreateView(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CreateViewOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        String viewName = (String) args.get(0);
        String viewOn = (String) args.get(1);
        List<? extends org.bson.conversions.Bson> pipeline = (List<? extends org.bson.conversions.Bson>) args.get(2);

        if (args.size() > 3) {
            Map<String, Object> options = (Map<String, Object>) args.get(3);
            CreateViewOptions createOptions = new CreateViewOptions();
            Map<String, Object> collation = getOptionMap(options, "collation");
            if (collation != null) {
                createOptions.collation(jsonb2Collation(collation));
            }
            mongoDB.createView(viewName, viewOn, pipeline, createOptions);
        } else {
            mongoDB.createView(viewName, viewOn, pipeline);
        }

        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }

    public static Future<?> execInsert(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, InsertOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoColl = mongoDB.getCollection(collName);
        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        Object docOrList = args.get(0);
        if (docOrList instanceof List) {
            mongoColl.insertMany((List<Document>) docOrList);
            receive.responseUpdateCount(request, ((List) docOrList).size());
        } else {
            mongoColl.insertOne((Document) docOrList);
            receive.responseUpdateCount(request, 1);
        }
        return completed(sync);
    }

    public static Future<?> execUpdate(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, UpdateOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoColl = mongoDB.getCollection(collName);
        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        Bson filter = (Bson) args.get(0);
        Bson update = (Bson) args.get(1);
        UpdateOptions options = new UpdateOptions();
        boolean multi = false;
        if (args.size() > 2) {
            Map<String, Object> opts = (Map<String, Object>) args.get(2);
            Boolean upsert = getOptionBoolean(opts, "upsert");
            if (upsert != null) {
                options.upsert(upsert);
            }
            Boolean multiOpt = getOptionBoolean(opts, "multi");
            if (multiOpt != null) {
                multi = multiOpt;
            }
            Map<String, Object> collation = getOptionMap(opts, "collation");
            if (collation != null) {
                options.collation(jsonb2Collation(collation));
            }
            List<Bson> arrayFilters = getOptionList(opts, "arrayFilters");
            if (arrayFilters != null) {
                options.arrayFilters(arrayFilters);
            }
        }

        UpdateResult result;
        if (multi) {
            result = mongoColl.updateMany(filter, update, options);
        } else {
            result = mongoColl.updateOne(filter, update, options);
        }
        receive.responseUpdateCount(request, (int) result.getModifiedCount());
        return completed(sync);
    }

    public static Future<?> execRemove(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, RemoveOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoColl = mongoDB.getCollection(collName);
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
                Boolean justOneOpt = getOptionBoolean(options, "justOne");
                if (justOneOpt != null) {
                    justOne = justOneOpt;
                }
            } else {
                throw new SQLException("invalid remove options argument.");
            }
        }

        DeleteResult result;
        if (justOne) {
            result = mongoColl.deleteOne(filter);
        } else {
            result = mongoColl.deleteMany(filter);
        }
        receive.responseUpdateCount(request, (int) result.getDeletedCount());
        return completed(sync);
    }

    public static Future<?> execBulkWrite(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, BulkWriteOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoColl = mongoDB.getCollection(collName);
        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        List<WriteModel<Document>> models = new ArrayList<>();
        List<Map<String, Object>> ops = (List<Map<String, Object>>) args.get(0);
        for (Map<String, Object> op : ops) {
            if (op.containsKey("insertOne")) {
                Map<String, Object> spec = (Map<String, Object>) op.get("insertOne");
                models.add(new InsertOneModel<>((Document) spec.get("document")));
            } else if (op.containsKey("updateOne")) {
                Map<String, Object> spec = (Map<String, Object>) op.get("updateOne");
                UpdateOptions options = new UpdateOptions();
                if (spec.containsKey("upsert")) {
                    options.upsert((Boolean) spec.get("upsert"));
                }
                if (spec.containsKey("collation")) {
                    options.collation(jsonb2Collation((Map<String, Object>) spec.get("collation")));
                }
                if (spec.containsKey("arrayFilters")) {
                    options.arrayFilters((List<? extends Bson>) spec.get("arrayFilters"));
                }
                models.add(new UpdateOneModel<>((Bson) spec.get("filter"), (Bson) spec.get("update"), options));
            } else if (op.containsKey("updateMany")) {
                Map<String, Object> spec = (Map<String, Object>) op.get("updateMany");
                UpdateOptions options = new UpdateOptions();
                if (spec.containsKey("upsert")) {
                    options.upsert((Boolean) spec.get("upsert"));
                }
                if (spec.containsKey("collation")) {
                    options.collation(jsonb2Collation((Map<String, Object>) spec.get("collation")));
                }
                if (spec.containsKey("arrayFilters")) {
                    options.arrayFilters((List<? extends Bson>) spec.get("arrayFilters"));
                }
                models.add(new UpdateManyModel<>((Bson) spec.get("filter"), (Bson) spec.get("update"), options));
            } else if (op.containsKey("deleteOne")) {
                Map<String, Object> spec = (Map<String, Object>) op.get("deleteOne");
                DeleteOptions options = new DeleteOptions();
                if (spec.containsKey("collation")) {
                    options.collation(jsonb2Collation((Map<String, Object>) spec.get("collation")));
                }
                models.add(new DeleteOneModel<>((Bson) spec.get("filter"), options));
            } else if (op.containsKey("deleteMany")) {
                Map<String, Object> spec = (Map<String, Object>) op.get("deleteMany");
                DeleteOptions options = new DeleteOptions();
                if (spec.containsKey("collation")) {
                    options.collation(jsonb2Collation((Map<String, Object>) spec.get("collation")));
                }
                models.add(new DeleteManyModel<>((Bson) spec.get("filter"), options));
            } else if (op.containsKey("replaceOne")) {
                Map<String, Object> spec = (Map<String, Object>) op.get("replaceOne");
                ReplaceOptions options = new ReplaceOptions();
                if (spec.containsKey("upsert")) {
                    options.upsert((Boolean) spec.get("upsert"));
                }
                if (spec.containsKey("collation")) {
                    options.collation(jsonb2Collation((Map<String, Object>) spec.get("collation")));
                }
                models.add(new ReplaceOneModel<>((Bson) spec.get("filter"), (Document) spec.get("replacement"), options));
            }
        }

        BulkWriteOptions options = new BulkWriteOptions();
        if (args.size() > 1) {
            Map<String, Object> opts = (Map<String, Object>) args.get(1);
            Boolean ordered = getOptionBoolean(opts, "ordered");
            if (ordered != null) {
                options.ordered(ordered);
            }
            Boolean bypassDocumentValidation = getOptionBoolean(opts, "bypassDocumentValidation");
            if (bypassDocumentValidation != null) {
                options.bypassDocumentValidation(bypassDocumentValidation);
            }
        }

        com.mongodb.bulk.BulkWriteResult result = mongoColl.bulkWrite(models, options);
        receive.responseUpdateCount(request, result.getModifiedCount() + result.getInsertedCount() + result.getDeletedCount());
        return completed(sync);
    }

    public static Future<?> execCount(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, CountOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoColl = mongoDB.getCollection(collName);
        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        Bson filter = (!args.isEmpty()) ? (Bson) args.get(0) : new Document();
        long count = mongoColl.countDocuments(filter);

        receive.responseResult(request, singleResult(request, COL_COUNT_LONG, count));
        return completed(sync);
    }

    public static Future<?> execDistinct(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, DistinctOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoColl = mongoDB.getCollection(collName);
        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        String fieldName = (String) args.get(0);
        Bson filter = (args.size() > 1) ? (Bson) args.get(1) : new Document();
        DistinctIterable<Object> distinct = mongoColl.distinct(fieldName, filter, Object.class);

        receive.responseResult(request, listResult(request, COL_VALUE_STRING, distinct));
        return completed(sync);
    }

    public static Future<?> execFind(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, FindOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);

        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoColl = mongoDB.getCollection(collName);
        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        Bson filter = (!args.isEmpty()) ? (Bson) args.get(0) : new Document();
        Bson projection = (args.size() > 1) ? (Bson) args.get(1) : null;
        FindIterable<Document> it = mongoColl.find(filter);
        if (projection != null) {
            it.projection(projection);
        }

        if (c.methodCall() != null) {
            for (MethodCallContext method : c.methodCall()) {
                if (method instanceof LimitOpContext) {
                    List<Object> limitArgs = (List<Object>) visitor.visit(((LimitOpContext) method).arguments());
                    it.limit(((Number) limitArgs.get(0)).intValue());
                } else if (method instanceof SkipOpContext) {
                    List<Object> skipArgs = (List<Object>) visitor.visit(((SkipOpContext) method).arguments());
                    it.skip(((Number) skipArgs.get(0)).intValue());
                } else if (method instanceof SortOpContext) {
                    List<Object> sortArgs = (List<Object>) visitor.visit(((SortOpContext) method).arguments());
                    it.sort((Bson) sortArgs.get(0));
                } else if (method instanceof HintOpContext) {
                    List<Object> hintArgs = (List<Object>) visitor.visit(((HintOpContext) method).arguments());
                    it.hint((Bson) hintArgs.get(0));
                } else {
                    throw new SQLException("unknown method call: " + method.getText());
                }
            }
        }

        if (((MongoRequest) request).isPreRead()) {
            try (MongoResultBuffer buffer = new MongoResultBuffer(conn.getPreReadThreshold(), conn.getPreReadMaxFileSize(), conn.getPreReadCacheDir())) {
                return execFindWithPreRead(sync, request, receive, conn, buffer, it);
            } catch (IOException e) {
                throw new SQLException(e);
            }
        } else {
            return execFindWithDirect(sync, request, receive, it);
        }
    }

    private static Future<?> execFindWithPreRead(Future<Object> sync, AdapterRequest request, AdapterReceive receive, MongoConn conn, MongoResultBuffer buffer, FindIterable<Document> it) throws SQLException, IOException {
        Set<String> keySet = new LinkedHashSet<>();
        keySet.add(COL_JSON_STRING.name);
        long maxRows = request.getMaxRows();
        int affectRows = 0;
        for (Document doc : it) {
            buffer.add(doc);
            keySet.addAll(doc.keySet());

            affectRows++;
            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        buffer.finish();

        List<JdbcColumn> columns = new ArrayList<>();
        for (String key : keySet) {
            columns.add(new JdbcColumn(key, AdapterType.String, "", "", ""));
        }

        AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
        for (Document doc : buffer) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(COL_JSON_STRING.name, doc.toJson());
            for (String key : keySet) {
                if (doc.containsKey(key)) {
                    Object val = doc.get(key);
                    row.put(key, val);
                }
            }
            cursor.pushData(row);
        }
        cursor.pushFinish();
        receive.responseResult(request, cursor);
        return completed(sync);
    }

    private static Future<?> execFindWithDirect(Future<Object> sync, AdapterRequest request, AdapterReceive receive, FindIterable<Document> it) throws SQLException {
        AdapterResultCursor cursor = new AdapterResultCursor(request, Collections.singletonList(COL_JSON_STRING));
        long maxRows = request.getMaxRows();
        int affectRows = 0;
        for (Document doc : it) {
            cursor.pushData(Collections.singletonMap(COL_JSON_STRING.name, doc.toJson()));
            affectRows++;
            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        cursor.pushFinish();
        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execAggregate(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CollectionContext collection, AggregateOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        String collName = argAsCollectionName(argIndex, request, collection);

        MongoDatabase mongoDatabase = mongoCmd.getClient().getDatabase(dbName);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        List<Bson> pipeline = (List<Bson>) args.get(0);
        AggregateIterable<Document> aggregate = mongoCollection.aggregate(pipeline);

        if (args.size() > 1) {
            Map<String, Object> options = (Map<String, Object>) args.get(1);
            applyAggregateOptions(aggregate, options);
        }

        AdapterResultCursor cursor = new AdapterResultCursor(request, Arrays.asList(COL_JSON_STRING));
        for (Document doc : aggregate) {
            cursor.pushData(Collections.singletonMap(COL_JSON_STRING.name, doc.toJson()));
        }
        cursor.pushFinish();

        receive.responseResult(request, cursor);
        return completed(sync);
    }

    private static void applyAggregateOptions(AggregateIterable<Document> aggregate, Map<String, Object> options) throws SQLException {
        Boolean allowDiskUse = getOptionBoolean(options, "allowDiskUse");
        if (allowDiskUse != null) {
            aggregate.allowDiskUse(allowDiskUse);
        }

        Integer batchSize = getOptionInt(options, "batchSize");
        if (batchSize != null) {
            aggregate.batchSize(batchSize);
        }

        Long maxTimeMS = getOptionLong(options, "maxTimeMS");
        if (maxTimeMS != null) {
            aggregate.maxTime(maxTimeMS, TimeUnit.MILLISECONDS);
        }

        Long maxAwaitTimeMS = getOptionLong(options, "maxAwaitTimeMS");
        if (maxAwaitTimeMS != null) {
            aggregate.maxAwaitTime(maxAwaitTimeMS, TimeUnit.MILLISECONDS);
        }

        Boolean bypassDocumentValidation = getOptionBoolean(options, "bypassDocumentValidation");
        if (bypassDocumentValidation != null) {
            aggregate.bypassDocumentValidation(bypassDocumentValidation);
        }

        Map<String, Object> collation = getOptionMap(options, "collation");
        if (collation != null) {
            aggregate.collation(jsonb2Collation(collation));
        }

        String comment = getOptionString(options, "comment");
        if (comment != null) {
            aggregate.comment(comment);
        }

        Map<String, Object> hint = getOptionMap(options, "hint");
        if (hint != null) {
            aggregate.hint(new Document(hint));
        }
    }

}
