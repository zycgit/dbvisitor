package net.hasor.dbvisitor.adapter.mongo;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import com.mongodb.MongoNamespace;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

@SuppressWarnings({ "deprecation", "NullableProblems" })
class MongoCollectionProxy implements MongoCollection<Document> {
    private final MongoCollection<Document> proxy;
    private       String                    catalog;
    private       String                    schema;
    private       MongoCollection<Document> target;

    public MongoCollectionProxy(final InvocationHandler handler) {
        if (handler != null) {
            InvocationHandler h = (proxy, method, args) -> handler.invoke(this.target, method, args);
            this.proxy = (MongoCollection<Document>) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { MongoCollection.class }, h);
        } else {
            this.proxy = null;
        }
    }

    public void updateTarget(String catalog, String schema, MongoCollection<Document> target) {
        this.catalog = catalog;
        this.schema = schema;
        this.target = Objects.requireNonNull(target, "target MongoCollection is null");
    }

    public MongoCollection<Document> mongoDB() {
        if (this.target == null) {
            throw new RuntimeSQLException("No collection selected.");
        } else {
            return this.proxy == null ? this.target : this.proxy;
        }
    }

    //

    public String getCatalog() {
        return this.catalog;
    }

    public String getSchema() {
        return this.schema;
    }

    @Override
    public MongoNamespace getNamespace() {
        return this.mongoDB().getNamespace();
    }

    @Override
    public Class<Document> getDocumentClass() {
        return this.mongoDB().getDocumentClass();
    }

    @Override
    public CodecRegistry getCodecRegistry() {
        return this.mongoDB().getCodecRegistry();
    }

    @Override
    public ReadPreference getReadPreference() {
        return this.mongoDB().getReadPreference();
    }

    @Override
    public WriteConcern getWriteConcern() {
        return this.mongoDB().getWriteConcern();
    }

    @Override
    public ReadConcern getReadConcern() {
        return this.mongoDB().getReadConcern();
    }

    @Override
    public Long getTimeout(TimeUnit timeUnit) {
        return this.mongoDB().getTimeout(timeUnit);
    }

    @Override
    public <NewTDocument> MongoCollection<NewTDocument> withDocumentClass(Class<NewTDocument> clazz) {
        return this.mongoDB().withDocumentClass(clazz);
    }

    @Override
    public MongoCollection<Document> withCodecRegistry(CodecRegistry codecRegistry) {
        return this.mongoDB().withCodecRegistry(codecRegistry);
    }

    @Override
    public MongoCollection<Document> withReadPreference(ReadPreference readPreference) {
        return this.mongoDB().withReadPreference(readPreference);
    }

    @Override
    public MongoCollection<Document> withWriteConcern(WriteConcern writeConcern) {
        return this.mongoDB().withWriteConcern(writeConcern);
    }

    @Override
    public MongoCollection<Document> withReadConcern(ReadConcern readConcern) {
        return this.mongoDB().withReadConcern(readConcern);
    }

    @Override
    public MongoCollection<Document> withTimeout(long l, TimeUnit timeUnit) {
        return this.mongoDB().withTimeout(l, timeUnit);
    }

    @Override
    public long countDocuments() {
        return this.mongoDB().countDocuments();
    }

    @Override
    public long countDocuments(Bson filter) {
        return this.mongoDB().countDocuments(filter);
    }

    @Override
    public long countDocuments(Bson filter, CountOptions options) {
        return this.mongoDB().countDocuments(filter, options);
    }

    @Override
    public long countDocuments(ClientSession clientSession) {
        return this.mongoDB().countDocuments(clientSession);
    }

    @Override
    public long countDocuments(ClientSession clientSession, Bson filter) {
        return this.mongoDB().countDocuments(clientSession, filter);
    }

    @Override
    public long countDocuments(ClientSession clientSession, Bson filter, CountOptions options) {
        return this.mongoDB().countDocuments(clientSession, filter, options);
    }

    @Override
    public long estimatedDocumentCount() {
        return this.mongoDB().estimatedDocumentCount();
    }

    @Override
    public long estimatedDocumentCount(EstimatedDocumentCountOptions options) {
        return this.mongoDB().estimatedDocumentCount(options);
    }

    @Override
    public <TResult> DistinctIterable<TResult> distinct(String fieldName, Class<TResult> resultClass) {
        return this.mongoDB().distinct(fieldName, resultClass);
    }

    @Override
    public <TResult> DistinctIterable<TResult> distinct(String fieldName, Bson filter, Class<TResult> resultClass) {
        return this.mongoDB().distinct(fieldName, filter, resultClass);
    }

    @Override
    public <TResult> DistinctIterable<TResult> distinct(ClientSession clientSession, String fieldName, Class<TResult> resultClass) {
        return this.mongoDB().distinct(clientSession, fieldName, resultClass);
    }

    @Override
    public <TResult> DistinctIterable<TResult> distinct(ClientSession clientSession, String fieldName, Bson filter, Class<TResult> resultClass) {
        return this.mongoDB().distinct(clientSession, fieldName, filter, resultClass);
    }

    @Override
    public FindIterable<Document> find() {
        return this.mongoDB().find();
    }

    @Override
    public <TResult> FindIterable<TResult> find(Class<TResult> resultClass) {
        return this.mongoDB().find(resultClass);
    }

    @Override
    public FindIterable<Document> find(Bson filter) {
        return this.mongoDB().find(filter);
    }

    @Override
    public <TResult> FindIterable<TResult> find(Bson filter, Class<TResult> resultClass) {
        return this.mongoDB().find(filter, resultClass);
    }

    @Override
    public FindIterable<Document> find(ClientSession clientSession) {
        return this.mongoDB().find(clientSession);
    }

    @Override
    public <TResult> FindIterable<TResult> find(ClientSession clientSession, Class<TResult> resultClass) {
        return this.mongoDB().find(clientSession, resultClass);
    }

    @Override
    public FindIterable<Document> find(ClientSession clientSession, Bson filter) {
        return this.mongoDB().find(clientSession, filter);
    }

    @Override
    public <TResult> FindIterable<TResult> find(ClientSession clientSession, Bson filter, Class<TResult> resultClass) {
        return this.mongoDB().find(clientSession, filter, resultClass);
    }

    @Override
    public AggregateIterable<Document> aggregate(List<? extends Bson> pipeline) {
        return this.mongoDB().aggregate(pipeline);
    }

    @Override
    public <TResult> AggregateIterable<TResult> aggregate(List<? extends Bson> pipeline, Class<TResult> resultClass) {
        return this.mongoDB().aggregate(pipeline, resultClass);
    }

    @Override
    public AggregateIterable<Document> aggregate(ClientSession clientSession, List<? extends Bson> pipeline) {
        return this.mongoDB().aggregate(clientSession, pipeline);
    }

    @Override
    public <TResult> AggregateIterable<TResult> aggregate(ClientSession clientSession, List<? extends Bson> pipeline, Class<TResult> resultClass) {
        return this.mongoDB().aggregate(clientSession, pipeline, resultClass);
    }

    @Override
    public ChangeStreamIterable<Document> watch() {
        return this.mongoDB().watch();
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(Class<TResult> resultClass) {
        return this.mongoDB().watch(resultClass);
    }

    @Override
    public ChangeStreamIterable<Document> watch(List<? extends Bson> pipeline) {
        return this.mongoDB().watch(pipeline);
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(List<? extends Bson> pipeline, Class<TResult> resultClass) {
        return this.mongoDB().watch(pipeline, resultClass);
    }

    @Override
    public ChangeStreamIterable<Document> watch(ClientSession clientSession) {
        return this.mongoDB().watch(clientSession);
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, Class<TResult> resultClass) {
        return this.mongoDB().watch(clientSession, resultClass);
    }

    @Override
    public ChangeStreamIterable<Document> watch(ClientSession clientSession, List<? extends Bson> pipeline) {
        return this.mongoDB().watch(clientSession, pipeline);
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, List<? extends Bson> pipeline, Class<TResult> resultClass) {
        return this.mongoDB().watch(clientSession, pipeline, resultClass);
    }

    @Override
    public MapReduceIterable<Document> mapReduce(String mapFunction, String reduceFunction) {
        return this.mongoDB().mapReduce(mapFunction, reduceFunction);
    }

    @Override
    public <TResult> MapReduceIterable<TResult> mapReduce(String mapFunction, String reduceFunction, Class<TResult> resultClass) {
        return this.mongoDB().mapReduce(mapFunction, reduceFunction, resultClass);
    }

    @Override
    public MapReduceIterable<Document> mapReduce(ClientSession clientSession, String mapFunction, String reduceFunction) {
        return this.mongoDB().mapReduce(clientSession, mapFunction, reduceFunction);
    }

    @Override
    public <TResult> MapReduceIterable<TResult> mapReduce(ClientSession clientSession, String mapFunction, String reduceFunction, Class<TResult> resultClass) {
        return this.mongoDB().mapReduce(clientSession, mapFunction, reduceFunction, resultClass);
    }

    @Override
    public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends Document>> requests) {
        return this.mongoDB().bulkWrite(requests);
    }

    @Override
    public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends Document>> requests, BulkWriteOptions options) {
        return this.mongoDB().bulkWrite(requests, options);
    }

    @Override
    public BulkWriteResult bulkWrite(ClientSession clientSession, List<? extends WriteModel<? extends Document>> requests) {
        return this.mongoDB().bulkWrite(clientSession, requests);
    }

    @Override
    public BulkWriteResult bulkWrite(ClientSession clientSession, List<? extends WriteModel<? extends Document>> requests, BulkWriteOptions options) {
        return this.mongoDB().bulkWrite(clientSession, requests, options);
    }

    @Override
    public InsertOneResult insertOne(Document document) {
        return this.mongoDB().insertOne(document);
    }

    @Override
    public InsertOneResult insertOne(Document document, InsertOneOptions options) {
        return this.mongoDB().insertOne(document, options);
    }

    @Override
    public InsertOneResult insertOne(ClientSession clientSession, Document document) {
        return this.mongoDB().insertOne(clientSession, document);
    }

    @Override
    public InsertOneResult insertOne(ClientSession clientSession, Document document, InsertOneOptions options) {
        return this.mongoDB().insertOne(clientSession, document, options);
    }

    @Override
    public InsertManyResult insertMany(List<? extends Document> documents) {
        return this.mongoDB().insertMany(documents);
    }

    @Override
    public InsertManyResult insertMany(List<? extends Document> documents, InsertManyOptions options) {
        return this.mongoDB().insertMany(documents, options);
    }

    @Override
    public InsertManyResult insertMany(ClientSession clientSession, List<? extends Document> documents) {
        return this.mongoDB().insertMany(clientSession, documents);
    }

    @Override
    public InsertManyResult insertMany(ClientSession clientSession, List<? extends Document> documents, InsertManyOptions options) {
        return this.mongoDB().insertMany(clientSession, documents, options);
    }

    @Override
    public DeleteResult deleteOne(Bson filter) {
        return this.mongoDB().deleteOne(filter);
    }

    @Override
    public DeleteResult deleteOne(Bson filter, DeleteOptions options) {
        return this.mongoDB().deleteOne(filter, options);
    }

    @Override
    public DeleteResult deleteOne(ClientSession clientSession, Bson filter) {
        return this.mongoDB().deleteOne(clientSession, filter);
    }

    @Override
    public DeleteResult deleteOne(ClientSession clientSession, Bson filter, DeleteOptions options) {
        return this.mongoDB().deleteOne(clientSession, filter, options);
    }

    @Override
    public DeleteResult deleteMany(Bson filter) {
        return this.mongoDB().deleteMany(filter);
    }

    @Override
    public DeleteResult deleteMany(Bson filter, DeleteOptions options) {
        return this.mongoDB().deleteMany(filter, options);
    }

    @Override
    public DeleteResult deleteMany(ClientSession clientSession, Bson filter) {
        return this.mongoDB().deleteMany(clientSession, filter);
    }

    @Override
    public DeleteResult deleteMany(ClientSession clientSession, Bson filter, DeleteOptions options) {
        return this.mongoDB().deleteMany(clientSession, filter, options);
    }

    @Override
    public UpdateResult replaceOne(Bson filter, Document replacement) {
        return this.mongoDB().replaceOne(filter, replacement);
    }

    @Override
    public UpdateResult replaceOne(Bson filter, Document replacement, ReplaceOptions options) {
        return this.mongoDB().replaceOne(filter, replacement, options);
    }

    @Override
    public UpdateResult replaceOne(ClientSession clientSession, Bson filter, Document replacement) {
        return this.mongoDB().replaceOne(clientSession, filter, replacement);
    }

    @Override
    public UpdateResult replaceOne(ClientSession clientSession, Bson filter, Document replacement, ReplaceOptions options) {
        return this.mongoDB().replaceOne(clientSession, filter, replacement, options);
    }

    @Override
    public UpdateResult updateOne(Bson filter, Bson update) {
        return this.mongoDB().updateOne(filter, update);
    }

    @Override
    public UpdateResult updateOne(Bson filter, Bson update, UpdateOptions options) {
        return this.mongoDB().updateOne(filter, update, options);
    }

    @Override
    public UpdateResult updateOne(ClientSession clientSession, Bson filter, Bson update) {
        return this.mongoDB().updateOne(clientSession, filter, update);
    }

    @Override
    public UpdateResult updateOne(ClientSession clientSession, Bson filter, Bson update, UpdateOptions options) {
        return this.mongoDB().updateOne(clientSession, filter, update, options);
    }

    @Override
    public UpdateResult updateMany(Bson filter, Bson update) {
        return this.mongoDB().updateMany(filter, update);
    }

    @Override
    public UpdateResult updateMany(Bson filter, Bson update, UpdateOptions options) {
        return this.mongoDB().updateMany(filter, update, options);
    }

    @Override
    public UpdateResult updateMany(ClientSession clientSession, Bson filter, Bson update) {
        return this.mongoDB().updateMany(clientSession, filter, update);
    }

    @Override
    public UpdateResult updateMany(ClientSession clientSession, Bson filter, Bson update, UpdateOptions options) {
        return this.mongoDB().updateMany(clientSession, filter, update, options);
    }

    @Override
    public Document findOneAndDelete(Bson filter) {
        return this.mongoDB().findOneAndDelete(filter);
    }

    @Override
    public Document findOneAndDelete(Bson filter, FindOneAndDeleteOptions options) {
        return this.mongoDB().findOneAndDelete(filter, options);
    }

    @Override
    public Document findOneAndDelete(ClientSession clientSession, Bson filter) {
        return this.mongoDB().findOneAndDelete(clientSession, filter);
    }

    @Override
    public Document findOneAndDelete(ClientSession clientSession, Bson filter, FindOneAndDeleteOptions options) {
        return this.mongoDB().findOneAndDelete(clientSession, filter, options);
    }

    @Override
    public Document findOneAndReplace(Bson filter, Document replacement) {
        return this.mongoDB().findOneAndReplace(filter, replacement);
    }

    @Override
    public Document findOneAndReplace(Bson filter, Document replacement, FindOneAndReplaceOptions options) {
        return this.mongoDB().findOneAndReplace(filter, replacement, options);
    }

    @Override
    public Document findOneAndReplace(ClientSession clientSession, Bson filter, Document replacement) {
        return this.mongoDB().findOneAndReplace(clientSession, filter, replacement);
    }

    @Override
    public Document findOneAndReplace(ClientSession clientSession, Bson filter, Document replacement, FindOneAndReplaceOptions options) {
        return this.mongoDB().findOneAndReplace(clientSession, filter, replacement, options);
    }

    @Override
    public Document findOneAndUpdate(Bson filter, Bson update) {
        return this.mongoDB().findOneAndUpdate(filter, update);
    }

    @Override
    public Document findOneAndUpdate(Bson filter, Bson update, FindOneAndUpdateOptions options) {
        return this.mongoDB().findOneAndUpdate(filter, update, options);
    }

    @Override
    public Document findOneAndUpdate(ClientSession clientSession, Bson filter, Bson update) {
        return this.mongoDB().findOneAndUpdate(clientSession, filter, update);
    }

    @Override
    public Document findOneAndUpdate(ClientSession clientSession, Bson filter, Bson update, FindOneAndUpdateOptions options) {
        return this.mongoDB().findOneAndUpdate(clientSession, filter, update, options);
    }

    @Override
    public void drop() {
        this.mongoDB().drop();
    }

    @Override
    public void drop(ClientSession clientSession) {
        this.mongoDB().drop(clientSession);
    }

    @Override
    public void drop(DropCollectionOptions dropCollectionOptions) {
        this.mongoDB().drop(dropCollectionOptions);
    }

    @Override
    public void drop(ClientSession clientSession, DropCollectionOptions dropCollectionOptions) {
        this.mongoDB().drop(clientSession, dropCollectionOptions);
    }

    @Override
    public String createSearchIndex(String s, Bson bson) {
        return this.mongoDB().createSearchIndex(s, bson);
    }

    @Override
    public String createSearchIndex(Bson bson) {
        return this.mongoDB().createSearchIndex(bson);
    }

    @Override
    public List<String> createSearchIndexes(List<SearchIndexModel> list) {
        return this.mongoDB().createSearchIndexes(list);
    }

    @Override
    public void updateSearchIndex(String s, Bson bson) {
        this.mongoDB().updateSearchIndex(s, bson);
    }

    @Override
    public void dropSearchIndex(String s) {
        this.mongoDB().dropSearchIndex(s);
    }

    @Override
    public ListSearchIndexesIterable<Document> listSearchIndexes() {
        return this.mongoDB().listSearchIndexes();
    }

    @Override
    public <TResult> ListSearchIndexesIterable<TResult> listSearchIndexes(Class<TResult> aClass) {
        return this.mongoDB().listSearchIndexes(aClass);
    }

    @Override
    public String createIndex(Bson keys) {
        return this.mongoDB().createIndex(keys);
    }

    @Override
    public String createIndex(Bson keys, IndexOptions indexOptions) {
        return this.mongoDB().createIndex(keys, indexOptions);
    }

    @Override
    public String createIndex(ClientSession clientSession, Bson keys) {
        return this.mongoDB().createIndex(clientSession, keys);
    }

    @Override
    public String createIndex(ClientSession clientSession, Bson keys, IndexOptions indexOptions) {
        return this.mongoDB().createIndex(clientSession, keys, indexOptions);
    }

    @Override
    public List<String> createIndexes(List<IndexModel> indexes) {
        return this.mongoDB().createIndexes(indexes);
    }

    @Override
    public List<String> createIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions) {
        return this.mongoDB().createIndexes(indexes, createIndexOptions);
    }

    @Override
    public List<String> createIndexes(ClientSession clientSession, List<IndexModel> indexes) {
        return this.mongoDB().createIndexes(clientSession, indexes);
    }

    @Override
    public List<String> createIndexes(ClientSession clientSession, List<IndexModel> indexes, CreateIndexOptions createIndexOptions) {
        return this.mongoDB().createIndexes(clientSession, indexes, createIndexOptions);
    }

    @Override
    public ListIndexesIterable<Document> listIndexes() {
        return this.mongoDB().listIndexes();
    }

    @Override
    public <TResult> ListIndexesIterable<TResult> listIndexes(Class<TResult> resultClass) {
        return this.mongoDB().listIndexes(resultClass);
    }

    @Override
    public ListIndexesIterable<Document> listIndexes(ClientSession clientSession) {
        return this.mongoDB().listIndexes(clientSession);
    }

    @Override
    public <TResult> ListIndexesIterable<TResult> listIndexes(ClientSession clientSession, Class<TResult> resultClass) {
        return this.mongoDB().listIndexes(clientSession, resultClass);
    }

    @Override
    public void dropIndex(String indexName) {
        this.mongoDB().dropIndex(indexName);
    }

    @Override
    public void dropIndex(String indexName, DropIndexOptions dropIndexOptions) {
        this.mongoDB().dropIndex(indexName, dropIndexOptions);
    }

    @Override
    public void dropIndex(ClientSession clientSession, String indexName) {
        this.mongoDB().dropIndex(clientSession, indexName);
    }

    @Override
    public void dropIndex(ClientSession clientSession, String indexName, DropIndexOptions dropIndexOptions) {
        this.mongoDB().dropIndex(clientSession, indexName, dropIndexOptions);
    }

    @Override
    public void dropIndex(Bson keys) {
        this.mongoDB().dropIndex(keys);
    }

    @Override
    public void dropIndex(Bson keys, DropIndexOptions dropIndexOptions) {
        this.mongoDB().dropIndex(keys, dropIndexOptions);
    }

    @Override
    public void dropIndex(ClientSession clientSession, Bson keys) {
        this.mongoDB().dropIndex(clientSession, keys);
    }

    @Override
    public void dropIndex(ClientSession clientSession, Bson keys, DropIndexOptions dropIndexOptions) {
        this.mongoDB().dropIndex(clientSession, keys, dropIndexOptions);
    }

    @Override
    public void dropIndexes() {
        this.mongoDB().dropIndexes();
    }

    @Override
    public void dropIndexes(ClientSession clientSession) {
        this.mongoDB().dropIndexes(clientSession);
    }

    @Override
    public void dropIndexes(DropIndexOptions dropIndexOptions) {
        this.mongoDB().dropIndexes(dropIndexOptions);
    }

    @Override
    public void dropIndexes(ClientSession clientSession, DropIndexOptions dropIndexOptions) {
        this.mongoDB().dropIndexes(clientSession, dropIndexOptions);
    }

    @Override
    public void renameCollection(MongoNamespace newCollectionNamespace) {
        this.mongoDB().renameCollection(newCollectionNamespace);
    }

    @Override
    public void renameCollection(MongoNamespace newCollectionNamespace, RenameCollectionOptions renameCollectionOptions) {
        this.mongoDB().renameCollection(newCollectionNamespace, renameCollectionOptions);
    }

    @Override
    public void renameCollection(ClientSession clientSession, MongoNamespace newCollectionNamespace) {
        this.mongoDB().renameCollection(clientSession, newCollectionNamespace);
    }

    @Override
    public void renameCollection(ClientSession clientSession, MongoNamespace newCollectionNamespace, RenameCollectionOptions renameCollectionOptions) {
        this.mongoDB().renameCollection(clientSession, newCollectionNamespace, renameCollectionOptions);
    }

    @Override
    public UpdateResult updateOne(ClientSession clientSession, Bson filter, List<? extends Bson> updatePipeline, UpdateOptions updateOptions) {
        return this.mongoDB().updateOne(clientSession, filter, updatePipeline, updateOptions);
    }

    @Override
    public UpdateResult updateOne(ClientSession clientSession, Bson filter, List<? extends Bson> updatePipeline) {
        return this.mongoDB().updateOne(clientSession, filter, updatePipeline);
    }

    @Override
    public UpdateResult updateOne(Bson filter, List<? extends Bson> updatePipeline, UpdateOptions updateOptions) {
        return this.mongoDB().updateOne(filter, updatePipeline, updateOptions);
    }

    @Override
    public UpdateResult updateOne(Bson filter, List<? extends Bson> updatePipeline) {
        return this.mongoDB().updateOne(filter, updatePipeline);
    }

    @Override
    public UpdateResult updateMany(ClientSession clientSession, Bson filter, List<? extends Bson> updatePipeline, UpdateOptions updateOptions) {
        return this.mongoDB().updateMany(clientSession, filter, updatePipeline, updateOptions);
    }

    @Override
    public UpdateResult updateMany(ClientSession clientSession, Bson filter, List<? extends Bson> updatePipeline) {
        return this.mongoDB().updateMany(clientSession, filter, updatePipeline);
    }

    @Override
    public UpdateResult updateMany(Bson filter, List<? extends Bson> updatePipeline, UpdateOptions updateOptions) {
        return this.mongoDB().updateMany(filter, updatePipeline, updateOptions);
    }

    @Override
    public UpdateResult updateMany(Bson filter, List<? extends Bson> updatePipeline) {
        return this.mongoDB().updateMany(filter, updatePipeline);
    }

    @Override
    public Document findOneAndUpdate(ClientSession clientSession, Bson filter, List<? extends Bson> updatePipeline, FindOneAndUpdateOptions options) {
        return this.mongoDB().findOneAndUpdate(clientSession, filter, updatePipeline, options);
    }

    @Override
    public Document findOneAndUpdate(ClientSession clientSession, Bson filter, List<? extends Bson> updatePipeline) {
        return this.mongoDB().findOneAndUpdate(clientSession, filter, updatePipeline);
    }

    @Override
    public Document findOneAndUpdate(Bson filter, List<? extends Bson> updatePipeline, FindOneAndUpdateOptions options) {
        return this.mongoDB().findOneAndUpdate(filter, updatePipeline, options);
    }

    @Override
    public Document findOneAndUpdate(Bson filter, List<? extends Bson> updatePipeline) {
        return this.mongoDB().findOneAndUpdate(filter, updatePipeline);
    }
}
