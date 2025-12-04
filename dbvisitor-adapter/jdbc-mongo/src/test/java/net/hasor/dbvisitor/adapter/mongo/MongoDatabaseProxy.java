package net.hasor.dbvisitor.adapter.mongo;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.*;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.CreateViewOptions;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

@SuppressWarnings({ "NullableProblems" })
class MongoDatabaseProxy implements MongoDatabase {
    private final MongoDatabase     proxy;
    private       String            catalog;
    private       MongoDatabase     target;
    private final InvocationHandler handler;

    public MongoDatabaseProxy(final InvocationHandler handler) {
        this.handler = handler;
        if (handler != null) {
            InvocationHandler h = (proxy, method, args) -> handler.invoke(this.target, method, args);
            this.proxy = (MongoDatabase) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { MongoDatabase.class }, h);
        } else {
            this.proxy = null;
        }
    }

    public void updateTarget(String catalog, MongoDatabase target) {
        this.catalog = catalog;
        this.target = Objects.requireNonNull(target, "target MongoDatabase is null");
    }

    public MongoDatabase mongoDB() {
        if (this.target == null) {
            throw new RuntimeSQLException("No database selected when selecting schema.");
        } else {
            return this.proxy == null ? this.target : this.proxy;
        }
    }

    //

    public String getCatalog() {
        return this.catalog;
    }

    @Override
    public String getName() {
        return this.mongoDB().getName();
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
    public MongoDatabase withCodecRegistry(CodecRegistry codecRegistry) {
        return this.mongoDB().withCodecRegistry(codecRegistry);
    }

    @Override
    public MongoDatabase withReadPreference(ReadPreference readPreference) {
        return this.mongoDB().withReadPreference(readPreference);
    }

    @Override
    public MongoDatabase withWriteConcern(WriteConcern writeConcern) {
        return this.mongoDB().withWriteConcern(writeConcern);
    }

    @Override
    public MongoDatabase withReadConcern(ReadConcern readConcern) {
        return this.mongoDB().withReadConcern(readConcern);
    }

    @Override
    public MongoDatabase withTimeout(long l, TimeUnit timeUnit) {
        return this.mongoDB().withTimeout(l, timeUnit);
    }

    @Override
    public Document runCommand(Bson command) {
        return this.mongoDB().runCommand(command);
    }

    @Override
    public Document runCommand(Bson command, ReadPreference readPreference) {
        return this.mongoDB().runCommand(command, readPreference);
    }

    @Override
    public <TResult> TResult runCommand(Bson command, Class<TResult> resultClass) {
        return this.mongoDB().runCommand(command, resultClass);
    }

    @Override
    public <TResult> TResult runCommand(Bson command, ReadPreference readPreference, Class<TResult> resultClass) {
        return this.mongoDB().runCommand(command, readPreference, resultClass);
    }

    @Override
    public Document runCommand(ClientSession clientSession, Bson command) {
        return this.mongoDB().runCommand(clientSession, command);
    }

    @Override
    public Document runCommand(ClientSession clientSession, Bson command, ReadPreference readPreference) {
        return this.mongoDB().runCommand(clientSession, command, readPreference);
    }

    @Override
    public <TResult> TResult runCommand(ClientSession clientSession, Bson command, Class<TResult> resultClass) {
        return this.mongoDB().runCommand(clientSession, command, resultClass);
    }

    @Override
    public <TResult> TResult runCommand(ClientSession clientSession, Bson command, ReadPreference readPreference, Class<TResult> resultClass) {
        return this.mongoDB().runCommand(clientSession, command, readPreference, resultClass);
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
    public ListCollectionNamesIterable listCollectionNames() {
        return this.mongoDB().listCollectionNames();
    }

    @Override
    public ListCollectionNamesIterable listCollectionNames(ClientSession clientSession) {
        return this.mongoDB().listCollectionNames(clientSession);
    }

    @Override
    public ListCollectionsIterable<Document> listCollections() {
        return this.mongoDB().listCollections();
    }

    @Override
    public ListCollectionsIterable<Document> listCollections(ClientSession clientSession) {
        return this.mongoDB().listCollections(clientSession);
    }

    @Override
    public <TResult> ListCollectionsIterable<TResult> listCollections(Class<TResult> resultClass) {
        return this.mongoDB().listCollections(resultClass);
    }

    @Override
    public <TResult> ListCollectionsIterable<TResult> listCollections(ClientSession clientSession, Class<TResult> resultClass) {
        return this.mongoDB().listCollections(clientSession, resultClass);
    }

    @Override
    public void createCollection(String collectionName) {
        this.mongoDB().createCollection(collectionName);
    }

    @Override
    public void createCollection(String collectionName, CreateCollectionOptions createCollectionOptions) {
        this.mongoDB().createCollection(collectionName, createCollectionOptions);
    }

    @Override
    public void createCollection(ClientSession clientSession, String collectionName) {
        this.mongoDB().createCollection(clientSession, collectionName);
    }

    @Override
    public void createCollection(ClientSession clientSession, String collectionName, CreateCollectionOptions createCollectionOptions) {
        this.mongoDB().createCollection(clientSession, collectionName, createCollectionOptions);
    }

    @Override
    public void createView(String viewName, String viewOn, List<? extends Bson> pipeline) {
        this.mongoDB().createView(viewName, viewOn, pipeline);
    }

    @Override
    public void createView(String viewName, String viewOn, List<? extends Bson> pipeline, CreateViewOptions createViewOptions) {
        this.mongoDB().createView(viewName, viewOn, pipeline, createViewOptions);
    }

    @Override
    public void createView(ClientSession clientSession, String viewName, String viewOn, List<? extends Bson> pipeline) {
        this.mongoDB().createView(clientSession, viewName, viewOn, pipeline);
    }

    @Override
    public void createView(ClientSession clientSession, String viewName, String viewOn, List<? extends Bson> pipeline, CreateViewOptions createViewOptions) {
        this.mongoDB().createView(clientSession, viewName, viewOn, pipeline, createViewOptions);
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
    public MongoCollection<Document> getCollection(String collectionName) {
        return this.mongoDB().getCollection(collectionName);
    }

    @Override
    public <TDocument> MongoCollection<TDocument> getCollection(String collectionName, Class<TDocument> documentClass) {
        return this.mongoDB().getCollection(collectionName, documentClass);
    }
}
