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
    private final MongoClient          client;
    private final InvocationHandler    invocation;
    private final MongoDatabaseProxy   catalog;
    private final MongoCollectionProxy schema;

    MongoCmd(MongoClient client, InvocationHandler invocation, String defaultDB, String defaultSchema) throws SQLException {
        this.client = client;
        this.invocation = invocation;
        this.catalog = new MongoDatabaseProxy(invocation);
        this.schema = new MongoCollectionProxy(invocation);

        if (StringUtils.isNotBlank(defaultDB)) {
            this.catalog.updateTarget(defaultDB, client.getDatabase(defaultDB));
        }
        if (StringUtils.isNotBlank(defaultSchema)) {
            if (this.catalog.mongoDB() == null) {
                throw new SQLException("No database selected when selecting schema.");
            }

            MongoCollection<Document> collection = this.catalog.mongoDB().getCollection(defaultSchema);
            this.schema.updateTarget(defaultSchema, collection);
        }
    }

    MongoClient getClient() {
        return this.client;
    }

    MongoDatabaseProxy getCatalogProxy() {
        return this.catalog;
    }

    MongoCollectionProxy getSchemaProxy() {
        return this.schema;
    }

    public String getCatalog() {
        return this.catalog != null ? this.catalog.getCatalog() : null;
    }

    public void setCatalog(String catalog) {
        if (StringUtils.isBlank(catalog)) {
            this.catalog.updateTarget(null, null);
            this.schema.updateTarget(null, null);
        } else {
            MongoDatabase database = this.client.getDatabase(catalog);
            this.catalog.updateTarget(catalog, database);
            this.schema.updateTarget(null, null);
        }
    }

    public String getSchema() {
        return this.schema != null ? this.schema.getSchema() : null;
    }

    public void setSchema(String schema) throws SQLException {
        if (StringUtils.isBlank(schema)) {
            this.schema.updateTarget(null, null);
        } else if (this.catalog.mongoDB() == null) {
            throw new SQLException("No database selected when selecting schema.");
        } else {
            MongoCollection<Document> schemaObj = this.catalog.mongoDB().getCollection(schema);
            this.schema.updateTarget(schema, schemaObj);
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

    public MongoCollection<Document> getMongoSchema() {
        return this.schema;
    }

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
        if (StringUtils.equals(database, this.getCatalog()) && StringUtils.equals(schema, this.getSchema())) {
            return this.schema;
        }

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
        tmp.updateTarget(schema, schemaObj);
        return tmp;
    }
}

