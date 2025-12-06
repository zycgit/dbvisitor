package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.mockito.ArgumentMatchers.any;

public class IndexCommandTest extends AbstractJdbcTest {
    @Test
    public void create_index_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            PowerMockito.when(mockColl.createIndex(any(Bson.class), any(IndexOptions.class))).thenReturn("idx_name");
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.mycol.createIndex({name: 1}, {name: 'idx_name'})");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void create_index_fail_no_name() {
        MongoCommandInterceptor.resetInterceptor();
        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeUpdate("db.mycol.createIndex({name: 1})");
        } catch (SQLException e) {
            assert e.getMessage().contains("The index name must be specified.");
        }
    }

    @Test
    public void create_index_1() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            PowerMockito.when(mockColl.createIndex(any(Bson.class), any(IndexOptions.class))).thenAnswer(inv -> {
                IndexOptions opts = inv.getArgument(1);
                assert opts.isUnique();
                assert "my_idx".equals(opts.getName());
                return "my_idx";
            });
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.mycol.createIndex({name: 1}, {unique: true, name: 'my_idx'})");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void create_index_2() {
        // Test background, sparse, expireAfterSeconds, hidden
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            PowerMockito.when(mockColl.createIndex(any(Bson.class), any(IndexOptions.class))).thenAnswer(inv -> {
                IndexOptions opts = inv.getArgument(1);
                assert opts.isBackground();
                assert opts.isSparse();
                assert opts.getExpireAfter(java.util.concurrent.TimeUnit.SECONDS) == 3600;
                assert opts.isHidden();
                return "idx_2";
            });
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.mycol.createIndex({name: 1}, {name: 'idx_2', background: true, sparse: true, expireAfterSeconds: 3600, hidden: true})");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void create_index_3() {
        // Test weights, default_language, language_override, textIndexVersion
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            PowerMockito.when(mockColl.createIndex(any(Bson.class), any(IndexOptions.class))).thenAnswer(inv -> {
                IndexOptions opts = inv.getArgument(1);
                Bson weights = opts.getWeights();
                assert weights.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry()).getNumber("content").intValue() == 10;
                assert "english".equals(opts.getDefaultLanguage());
                assert "lang".equals(opts.getLanguageOverride());
                assert opts.getTextVersion() == 3;
                return "idx_3";
            });
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.mycol.createIndex({content: 'text'}, {name: 'idx_3', weights: {content: 10}, default_language: 'english', language_override: 'lang', textIndexVersion: 3})");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void create_index_4() {
        // Test 2dsphereIndexVersion, bits, min, max
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            PowerMockito.when(mockColl.createIndex(any(Bson.class), any(IndexOptions.class))).thenAnswer(inv -> {
                IndexOptions opts = inv.getArgument(1);
                assert opts.getSphereVersion() == 2;
                assert opts.getBits() == 26;
                assert opts.getMin() == -180.0;
                assert opts.getMax() == 180.0;
                return "idx_4";
            });
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.mycol.createIndex({loc: '2dsphere'}, {name: 'idx_4', 2dsphereIndexVersion: 2, bits: 26, min: -180.0, max: 180.0})");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void create_index_5() {
        // Test partialFilterExpression, collation, storageEngine
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            PowerMockito.when(mockColl.createIndex(any(Bson.class), any(IndexOptions.class))).thenAnswer(inv -> {
                IndexOptions opts = inv.getArgument(1);

                Bson partial = opts.getPartialFilterExpression();
                assert partial.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry()).getDocument("rating").getNumber("$gt").intValue() == 5;

                assert "en".equals(opts.getCollation().getLocale());

                Bson storage = opts.getStorageEngine();
                assert storage.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry()).getDocument("wiredTiger").getString("configString").getValue().equals("block_compressor=zlib");

                return "idx_5";
            });
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.mycol.createIndex({name: 1}, {name: 'idx_5', partialFilterExpression: {rating: {$gt: 5}}, collation: {locale: 'en'}, storageEngine: {wiredTiger: {configString: 'block_compressor=zlib'}}})");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void drop_index_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.mycol.dropIndex('idx_name')");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void drop_index_1() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.mycol.dropIndex({name: 1})");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void get_indexes_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            ListIndexesIterable iterable = PowerMockito.mock(ListIndexesIterable.class);
            MongoCursor cursor = PowerMockito.mock(MongoCursor.class);

            List<Document> indexes = Arrays.asList(new Document("name", "idx_1").append("v", 2).append("key", new Document("a", 1)), new Document("name", "idx_2").append("v", 2).append("key", new Document("b", 1)));
            java.util.Iterator<Document> iterator = indexes.iterator();

            PowerMockito.when(mockColl.listIndexes()).thenReturn(iterable);
            PowerMockito.when(iterable.iterator()).thenReturn(cursor);
            PowerMockito.when(cursor.hasNext()).thenAnswer(inv -> iterator.hasNext());
            PowerMockito.when(cursor.next()).thenAnswer(inv -> iterator.next());

            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            try (ResultSet rs = stmt.executeQuery("db.mycol.getIndexes()")) {
                assert rs.next();
                assert "idx_1".equals(rs.getString("NAME"));
                assert rs.getInt("V") == 2;
                assert rs.getString("KEY").contains("\"a\": 1");
                assert rs.getString("JSON").contains("idx_1");

                assert rs.next();
                assert "idx_2".equals(rs.getString("NAME"));
                assert rs.getInt("V") == 2;
                assert rs.getString("KEY").contains("\"b\": 1");
                assert rs.getString("JSON").contains("idx_2");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }
}
