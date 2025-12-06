package net.hasor.dbvisitor.adapter.mongo;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.bulk.ClientBulkWriteOptions;
import com.mongodb.client.model.bulk.ClientBulkWriteResult;
import com.mongodb.client.model.bulk.ClientNamespacedWriteModel;
import com.mongodb.connection.ClusterDescription;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

@SuppressWarnings({ "NullableProblems" })
class MongoClientProxy implements MongoClient {
    private final MongoClient       proxy;
    private       MongoClient       target;
    private final InvocationHandler handler;

    public MongoClientProxy(final InvocationHandler handler) {
        this.handler = handler;
        if (handler != null) {
            InvocationHandler h = (proxy, method, args) -> handler.invoke(this.target, method, args);
            this.proxy = (MongoClient) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { MongoClient.class }, h);
        } else {
            this.proxy = null;
        }
    }

    public void updateTarget(MongoClient target) {
        this.target = Objects.requireNonNull(target, "target MongoClient is null");
    }

    public MongoClient mongoDB() {
        if (this.target == null) {
            throw new RuntimeSQLException("No MongoClient selected.");
        } else {
            return this.proxy == null ? this.target : this.proxy;
        }
    }

    //

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
    public MongoCluster withCodecRegistry(CodecRegistry codecRegistry) {
        return this.mongoDB().withCodecRegistry(codecRegistry);
    }

    @Override
    public MongoCluster withReadPreference(ReadPreference readPreference) {
        return this.mongoDB().withReadPreference(readPreference);
    }

    @Override
    public MongoCluster withWriteConcern(WriteConcern writeConcern) {
        return this.mongoDB().withWriteConcern(writeConcern);
    }

    @Override
    public MongoCluster withReadConcern(ReadConcern readConcern) {
        return this.mongoDB().withReadConcern(readConcern);
    }

    @Override
    public MongoCluster withTimeout(long l, TimeUnit timeUnit) {
        return this.mongoDB().withTimeout(l, timeUnit);
    }

    @Override
    public MongoDatabase getDatabase(String databaseName) {
        MongoDatabase db = this.mongoDB().getDatabase(databaseName);
        MongoDatabaseProxy proxy = new MongoDatabaseProxy(this.handler);
        proxy.updateTarget(databaseName, db);
        return proxy;
    }
    @Override
    public ClientSession startSession() {
        return this.mongoDB().startSession();
    }

    @Override
    public ClientSession startSession(ClientSessionOptions options) {
        return this.mongoDB().startSession(options);
    }

    @Override
    public void close() {
        this.mongoDB().close();
    }

    @Override
    public MongoIterable<String> listDatabaseNames() {
        return this.mongoDB().listDatabaseNames();
    }

    @Override
    public MongoIterable<String> listDatabaseNames(ClientSession clientSession) {
        return this.mongoDB().listDatabaseNames(clientSession);
    }

    @Override
    public ListDatabasesIterable<Document> listDatabases() {
        return this.mongoDB().listDatabases();
    }

    @Override
    public ListDatabasesIterable<Document> listDatabases(ClientSession clientSession) {
        return this.mongoDB().listDatabases(clientSession);
    }

    @Override
    public <TResult> ListDatabasesIterable<TResult> listDatabases(Class<TResult> tResultClass) {
        return this.mongoDB().listDatabases(tResultClass);
    }

    @Override
    public <TResult> ListDatabasesIterable<TResult> listDatabases(ClientSession clientSession, Class<TResult> tResultClass) {
        return this.mongoDB().listDatabases(clientSession, tResultClass);
    }

    @Override
    public ChangeStreamIterable<Document> watch() {
        return this.mongoDB().watch();
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(Class<TResult> tResultClass) {
        return this.mongoDB().watch(tResultClass);
    }

    @Override
    public ChangeStreamIterable<Document> watch(List<? extends Bson> pipeline) {
        return this.mongoDB().watch(pipeline);
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(List<? extends Bson> pipeline, Class<TResult> tResultClass) {
        return this.mongoDB().watch(pipeline, tResultClass);
    }

    @Override
    public ChangeStreamIterable<Document> watch(ClientSession clientSession) {
        return this.mongoDB().watch(clientSession);
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, Class<TResult> tResultClass) {
        return this.mongoDB().watch(clientSession, tResultClass);
    }

    @Override
    public ChangeStreamIterable<Document> watch(ClientSession clientSession, List<? extends Bson> pipeline) {
        return this.mongoDB().watch(clientSession, pipeline);
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, List<? extends Bson> pipeline, Class<TResult> tResultClass) {
        return this.mongoDB().watch(clientSession, pipeline, tResultClass);
    }

    @Override
    public ClientBulkWriteResult bulkWrite(List<? extends ClientNamespacedWriteModel> list) throws ClientBulkWriteException {
        return this.mongoDB().bulkWrite(list);
    }

    @Override
    public ClientBulkWriteResult bulkWrite(List<? extends ClientNamespacedWriteModel> list, ClientBulkWriteOptions clientBulkWriteOptions) throws ClientBulkWriteException {
        return this.mongoDB().bulkWrite(list, clientBulkWriteOptions);
    }

    @Override
    public ClientBulkWriteResult bulkWrite(ClientSession clientSession, List<? extends ClientNamespacedWriteModel> list) throws ClientBulkWriteException {
        return this.mongoDB().bulkWrite(clientSession, list);
    }

    @Override
    public ClientBulkWriteResult bulkWrite(ClientSession clientSession, List<? extends ClientNamespacedWriteModel> list, ClientBulkWriteOptions clientBulkWriteOptions) throws ClientBulkWriteException {
        return this.mongoDB().bulkWrite(clientSession, list, clientBulkWriteOptions);
    }

    @Override
    public ClusterDescription getClusterDescription() {
        return this.mongoDB().getClusterDescription();
    }

    @Override
    public void appendMetadata(MongoDriverInformation mongoDriverInformation) {
        this.mongoDB().appendMetadata(mongoDriverInformation);
    }
}
