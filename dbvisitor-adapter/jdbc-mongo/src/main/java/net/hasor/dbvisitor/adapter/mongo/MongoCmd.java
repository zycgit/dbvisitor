package net.hasor.dbvisitor.adapter.mongo;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.sql.SQLException;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.hasor.cobble.StringUtils;
import org.bson.Document;

public class MongoCmd implements AutoCloseable {
    private final MongoClient        client;
    private final InvocationHandler  invocation;
    private final MongoDatabaseProxy catalog;
    //    private final ThreadLocal<MongoCollectionProxy> schema;

    MongoCmd(MongoClient client, InvocationHandler invocation, String defaultDB) throws SQLException {
        this.client = client;
        this.invocation = invocation;
        this.catalog = new MongoDatabaseProxy(invocation);
        //        this.schema = ThreadLocal.withInitial(() -> new MongoCollectionProxy(invocation));

        if (StringUtils.isNotBlank(defaultDB)) {
            this.catalog.updateTarget(defaultDB, client.getDatabase(defaultDB));
        }
    }

    MongoClient getClient() {
        return this.client;
    }

    MongoDatabaseProxy getCatalogProxy() {
        return this.catalog;
    }

    public String getCatalog() {
        return this.catalog != null ? this.catalog.getCatalog() : null;
    }

    public void setCatalog(String catalog) {
        if (StringUtils.isBlank(catalog)) {
            this.catalog.updateTarget(null, null);
        } else {
            MongoDatabase database = this.client.getDatabase(catalog);
            this.catalog.updateTarget(catalog, database);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.client.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    //

    public MongoDatabase getMongoDB(String database) {
        if (StringUtils.equals(database, this.getCatalog())) {
            return this.catalog;
        } else {
            MongoDatabaseProxy tmp = new MongoDatabaseProxy(this.invocation);
            tmp.updateTarget(database, this.client.getDatabase(database));
            return tmp;
        }
    }

    public MongoCollection<Document> getMongoSchema(String database, String schema) {
        //        MongoCollectionProxy local = this.schema.get();
        //        if (StringUtils.equals(database, local.getCatalog()) && StringUtils.equals(schema, local.getSchema())) {
        //            return local;
        //        }

        MongoDatabaseProxy db;
        if (StringUtils.equals(database, this.getCatalog())) {
            db = this.catalog;
        } else {
            MongoDatabaseProxy tmp = new MongoDatabaseProxy(this.invocation);
            tmp.updateTarget(database, this.client.getDatabase(database));
            db = tmp;
        }

        MongoCollectionProxy tmp = new MongoCollectionProxy(this.invocation);
        MongoCollection<Document> schemaObj = db.mongoDB().getCollection(schema);
        tmp.updateTarget(database, schema, schemaObj);
        return tmp;
    }
}

