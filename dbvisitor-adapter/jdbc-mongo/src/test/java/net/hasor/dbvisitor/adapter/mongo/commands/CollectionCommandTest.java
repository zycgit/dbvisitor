package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.mongodb.client.MongoDatabase;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.Document;
import org.junit.Test;

public class CollectionCommandTest extends AbstractJdbcTest {
    @Test
    public void show_collections_0() throws SQLException {
        List<String> result = Arrays.asList("col1", "col2");
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("listCollectionNames", (name, args) -> {
            return mockListCollectionNamesIterable(result);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            try (ResultSet rs = stmt.executeQuery("show collections")) {
                List<String> r = new ArrayList<>();
                while (rs.next()) {
                    r.add(rs.getString("COLLECTION"));
                }
                assert result.equals(r);
            }
        }
    }

    @Test
    public void create_collection_0() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("createCollection", (name, args) -> {
            assert "new_col".equals(args[0]);
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeUpdate("db.createCollection('new_col')");
        }
    }

    @Test
    public void create_collection_1() throws SQLException {
        // Test capped, size, max
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("createCollection", (name, args) -> {
            assert "capped_col".equals(args[0]);
            com.mongodb.client.model.CreateCollectionOptions opts = (com.mongodb.client.model.CreateCollectionOptions) args[1];
            assert opts.isCapped();
            assert opts.getSizeInBytes() == 1024;
            assert opts.getMaxDocuments() == 100;
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeUpdate("db.createCollection('capped_col', { capped: true, size: 1024, max: 100 })");
        }
    }

    @Test
    public void create_collection_2() throws SQLException {
        // Test validation
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("createCollection", (name, args) -> {
            assert "valid_col".equals(args[0]);
            com.mongodb.client.model.CreateCollectionOptions opts = (com.mongodb.client.model.CreateCollectionOptions) args[1];
            assert opts.getValidationOptions().getValidator() != null;
            assert opts.getValidationOptions().getValidationLevel() == com.mongodb.client.model.ValidationLevel.STRICT;
            assert opts.getValidationOptions().getValidationAction() == com.mongodb.client.model.ValidationAction.ERROR;
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeUpdate("db.createCollection('valid_col', { validator: { $jsonSchema: { bsonType: 'object' } }, validationLevel: 'strict', validationAction: 'error' })");
        }
    }

    @Test
    public void create_collection_3() throws SQLException {
        // Test collation
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("createCollection", (name, args) -> {
            assert "collation_col".equals(args[0]);
            com.mongodb.client.model.CreateCollectionOptions opts = (com.mongodb.client.model.CreateCollectionOptions) args[1];
            assert "en".equals(opts.getCollation().getLocale());
            assert opts.getCollation().getCaseLevel();
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeUpdate("db.createCollection('collation_col', { collation: { locale: 'en', caseLevel: true } })");
        }
    }

    @Test
    public void create_collection_4() throws SQLException {
        // Test timeseries
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("createCollection", (name, args) -> {
            assert "ts_col".equals(args[0]);
            com.mongodb.client.model.CreateCollectionOptions opts = (com.mongodb.client.model.CreateCollectionOptions) args[1];
            assert "timestamp".equals(opts.getTimeSeriesOptions().getTimeField());
            assert "metadata".equals(opts.getTimeSeriesOptions().getMetaField());
            assert com.mongodb.client.model.TimeSeriesGranularity.SECONDS == opts.getTimeSeriesOptions().getGranularity();
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeUpdate("db.createCollection('ts_col', { timeseries: { timeField: 'timestamp', metaField: 'metadata', granularity: 'seconds' } })");
        }
    }

    @Test
    public void create_collection_5() throws SQLException {
        // Test clusteredIndex
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("createCollection", (name, args) -> {
            assert "clustered_col".equals(args[0]);
            com.mongodb.client.model.CreateCollectionOptions opts = (com.mongodb.client.model.CreateCollectionOptions) args[1];
            assert opts.getClusteredIndexOptions().isUnique();
            assert "c_index".equals(opts.getClusteredIndexOptions().getName());
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeUpdate("db.createCollection('clustered_col', { clusteredIndex: { key: { _id: 1 }, unique: true, name: 'c_index' } })");
        }
    }

    @Test
    public void create_collection_6() {
        // Test viewOn (should fail)
        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeUpdate("db.createCollection('my_view', { viewOn: 'source_col' })");
            assert false : "Should throw SQLException";
        } catch (SQLException e) {
            assert e.getMessage().contains("The 'viewOn' option is not supported");
        }
    }

    @Test
    public void create_collection_7() throws SQLException {
        // Test expireAfterSeconds
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("createCollection", (name, args) -> {
            assert "expire_col".equals(args[0]);
            com.mongodb.client.model.CreateCollectionOptions opts = (com.mongodb.client.model.CreateCollectionOptions) args[1];
            assert opts.getExpireAfter(java.util.concurrent.TimeUnit.SECONDS) == 3600;
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeUpdate("db.createCollection('expire_col', { expireAfterSeconds: 3600 })");
        }
    }

    @Test
    public void create_collection_8() throws SQLException {
        // Test storageEngine, changeStreamPreAndPostImages, indexOptionDefaults
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("createCollection", (name, args) -> {
            assert "complex_col".equals(args[0]);
            com.mongodb.client.model.CreateCollectionOptions opts = (com.mongodb.client.model.CreateCollectionOptions) args[1];

            // storageEngine
            org.bson.BsonDocument se = opts.getStorageEngineOptions().toBsonDocument(org.bson.BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
            assert se.containsKey("wiredTiger");

            // changeStreamPreAndPostImages
            assert opts.getChangeStreamPreAndPostImagesOptions().isEnabled();

            // indexOptionDefaults
            org.bson.BsonDocument iod = opts.getIndexOptionDefaults().getStorageEngine().toBsonDocument(org.bson.BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
            assert iod.containsKey("wiredTiger");

            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeUpdate("db.createCollection('complex_col', { " +                                                //
                    "storageEngine: { wiredTiger: { configString: 'block_compressor=zlib' } }, " +                          //
                    "changeStreamPreAndPostImages: { enabled: true }, " +                                                   //
                    "indexOptionDefaults: { storageEngine: { wiredTiger: { configString: 'block_compressor=zlib' } } } " +  //
                    "})");
        }
    }

    @Test
    public void get_collection_names_0() throws SQLException {
        List<String> result = Arrays.asList("col1", "col2");
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("listCollectionNames", (name, args) -> {
            return mockListCollectionNamesIterable(result);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            try (ResultSet rs = stmt.executeQuery("db.getCollectionNames()")) {
                List<String> r = new ArrayList<>();
                while (rs.next()) {
                    r.add(rs.getString("COLLECTION"));
                }
                assert result.equals(r);
            }
        }
    }

    @Test
    public void get_collection_infos_0() throws SQLException {
        List<Document> result = Arrays.asList(new Document("name", "col1").append("type", "collection").append("options", new Document("capped", true)), new Document("name", "view1").append("type", "view").append("options", new Document("viewOn", "col1")));
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("listCollections", (name, args) -> {
            return mockListCollectionsIterable(result);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            try (ResultSet rs = stmt.executeQuery("db.getCollectionInfos()")) {
                List<String> names = new ArrayList<>();
                List<String> types = new ArrayList<>();
                while (rs.next()) {
                    names.add(rs.getString("NAME"));
                    types.add(rs.getString("TYPE"));
                    // Verify other columns exist
                    rs.getString("OPTIONS");
                    rs.getString("INFO");
                }
                assert Arrays.asList("col1", "view1").equals(names);
                assert Arrays.asList("collection", "view").equals(types);
            }
        }
    }

    @Test
    public void show_collections_1() throws SQLException {
        List<String> result = new ArrayList<>();
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("listCollectionNames", (name, args) -> {
            return mockListCollectionNamesIterable(result);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            try (ResultSet rs = stmt.executeQuery("show collections")) {
                assert !rs.next();
            }
        }
    }

    @Test
    public void get_collection_names_1() throws SQLException {
        List<String> result = new ArrayList<>();
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("listCollectionNames", (name, args) -> {
            return mockListCollectionNamesIterable(result);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            try (ResultSet rs = stmt.executeQuery("db.getCollectionNames()")) {
                assert !rs.next();
            }
        }
    }

    @Test
    public void create_view_0() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("createView", (name, args) -> {
            assert "my_view".equals(args[0]);
            assert "source_col".equals(args[1]);
            List<?> pipeline = (List<?>) args[2];
            assert pipeline.size() == 1;
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeUpdate("db.createView('my_view', 'source_col', [{ $match: { status: 'A' } }])");
        }
    }

    @Test
    public void create_view_1() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("createView", (name, args) -> {
            assert "my_view_2".equals(args[0]);
            assert "source_col".equals(args[1]);
            List<?> pipeline = (List<?>) args[2];
            assert pipeline.isEmpty();
            com.mongodb.client.model.CreateViewOptions opts = (com.mongodb.client.model.CreateViewOptions) args[3];
            assert "en".equals(opts.getCollation().getLocale());
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeUpdate("db.createView('my_view_2', 'source_col', [], { collation: { locale: 'en' } })");
        }
    }
}