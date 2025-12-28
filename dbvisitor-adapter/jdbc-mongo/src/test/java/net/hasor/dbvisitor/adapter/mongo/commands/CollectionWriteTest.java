package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.BsonInt32;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.mockito.ArgumentMatchers.any;

public class CollectionWriteTest extends AbstractJdbcTest {
    @Test
    public void insert_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("runCommand".equals(method.getName())) {
                Document doc = (Document) args[0];
                if (doc.containsKey("buildInfo")) {
                    return new Document("version", "4.0.0");
                }
            }
            if ("getCollection".equals(method.getName())) {
                assert ((MongoDatabase) proxy).getName().equals("mydb1");
                assert args[0].equals("mycol");

                MongoCollection<Document> mockColl = PowerMockito.mock(MongoCollection.class);
                PowerMockito.when(mockColl.insertMany(any())).thenAnswer(inv -> {
                    List<Document> docs = inv.getArgument(0);
                    assert docs.size() == 1;
                    assert docs.get(0).get("name").equals("zhangsan");
                    InsertManyResult res = PowerMockito.mock(InsertManyResult.class);
                    Map<Integer, BsonValue> ids = new HashMap<>();
                    ids.put(0, new BsonInt32(123));
                    PowerMockito.when(res.getInsertedIds()).thenReturn(ids);
                    PowerMockito.when(res.wasAcknowledged()).thenReturn(true);
                    return res;
                });
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("mydb1.mycol.insert({name:'zhangsan'})");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void insert_1() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("runCommand".equals(method.getName())) {
                Document doc = (Document) args[0];
                if (doc.containsKey("buildInfo")) {
                    return new Document("version", "4.0.0");
                }
            }
            if ("getCollection".equals(method.getName())) {
                assert ((MongoDatabase) proxy).getName().equals("mydb");
                assert args[0].equals("mycol");

                MongoCollection<Document> mockColl = (MongoCollection<Document>) PowerMockito.mock(MongoCollection.class);
                PowerMockito.when(mockColl.insertMany(any())).thenAnswer(inv -> {
                    List<Document> docs = inv.getArgument(0);
                    assert docs.size() == 2;
                    assert docs.get(0).get("name").equals("zhangsan");
                    assert docs.get(1).get("name").equals("lisi");
                    InsertManyResult res = PowerMockito.mock(InsertManyResult.class);
                    Map<Integer, BsonValue> ids = new HashMap<>();
                    ids.put(0, new BsonInt32(123));
                    ids.put(1, new BsonInt32(124));
                    PowerMockito.when(res.getInsertedIds()).thenReturn(ids);
                    PowerMockito.when(res.wasAcknowledged()).thenReturn(true);
                    return res;
                });
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection("mydb"); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("db.mycol.insert([{name:'zhangsan'}, {name:'lisi'}])");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void update_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("runCommand".equals(method.getName())) {
                Document doc = (Document) args[0];
                if (doc.containsKey("buildInfo")) {
                    return new Document("version", "4.0.0");
                }
            }
            if ("getCollection".equals(method.getName())) {
                assert ((MongoDatabase) proxy).getName().equals("bbb");
                assert args[0].equals("mycol");

                MongoCollection<Document> mockColl = (MongoCollection<Document>) PowerMockito.mock(MongoCollection.class);
                UpdateResult res = PowerMockito.mock(UpdateResult.class);
                PowerMockito.when(res.getModifiedCount()).thenReturn(1L);
                PowerMockito.when(mockColl.updateOne(any(Bson.class), any(Bson.class), any(UpdateOptions.class))).thenReturn(res);
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection("bbb"); Statement stmt = conn.createStatement()) {
            int count = stmt.executeUpdate("db.mycol.update({name:'zhangsan'}, {$set:{age:20}})");
            assert count == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void remove_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("runCommand".equals(method.getName())) {
                Document doc = (Document) args[0];
                if (doc.containsKey("buildInfo")) {
                    return new Document("version", "4.0.0");
                }
            }
            if ("getCollection".equals(method.getName())) {
                assert ((MongoDatabase) proxy).getName().equals("mydb");
                assert args[0].equals("mycol");

                MongoCollection<Document> mockColl = (MongoCollection<Document>) PowerMockito.mock(MongoCollection.class);
                DeleteResult res = PowerMockito.mock(DeleteResult.class);
                PowerMockito.when(res.getDeletedCount()).thenReturn(5L);
                PowerMockito.when(mockColl.deleteMany(any())).thenReturn(res);
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int count = stmt.executeUpdate("db.mycol.remove({age:{$gt:20}})");
            assert count == 5;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void bulk_write_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("runCommand".equals(method.getName())) {
                Document doc = (Document) args[0];
                if (doc.containsKey("buildInfo")) {
                    return new Document("version", "4.0.0");
                }
            }
            if ("getCollection".equals(method.getName())) {
                assert ((MongoDatabase) proxy).getName().equals("mydb");
                assert args[0].equals("mycol");

                MongoCollection<Document> mockColl = (MongoCollection<Document>) PowerMockito.mock(MongoCollection.class);
                BulkWriteResult res = PowerMockito.mock(BulkWriteResult.class);
                PowerMockito.when(res.getInsertedCount()).thenReturn(1);
                PowerMockito.when(res.getModifiedCount()).thenReturn(1);
                PowerMockito.when(res.getDeletedCount()).thenReturn(1);
                PowerMockito.when(mockColl.bulkWrite(any(List.class), any(BulkWriteOptions.class))).thenReturn(res);
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection("mydb"); Statement stmt = conn.createStatement()) {
            int count = stmt.executeUpdate("db.mycol.bulkWrite([\n" + //
                    "   { insertOne: { \"document\": { \"_id\": 1, \"char\": \"Brisbane\", \"class\": \"mammal\", \"water\": false } } },\n" + //
                    "   { updateOne: { \"filter\": { \"char\": \"Eldon\" }, \"update\": { $set: { \"status\": \"Critical\" } } } },\n" + //
                    "   { deleteOne: { \"filter\": { \"char\": \"Manor\" } } },\n" + //
                    "   { replaceOne: { \"filter\": { \"char\": \"Mardon\" }, \"replacement\": { \"char\": \"Mardon\", \"class\": \"mammal\", \"water\": false } } }\n" + //
                    "])");
            assert count == 3;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }
}
