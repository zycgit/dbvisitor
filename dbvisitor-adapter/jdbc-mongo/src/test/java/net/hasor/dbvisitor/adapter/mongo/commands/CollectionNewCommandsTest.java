package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mongodb.MongoNamespace;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
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

public class CollectionNewCommandsTest extends AbstractJdbcTest {
    @Test
    public void insertOne() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                PowerMockito.when(mockColl.insertOne(any(Document.class), any(InsertOneOptions.class))).thenAnswer(inv -> {
                    Document doc = inv.getArgument(0);
                    assert doc.get("name").equals("zhangsan");
                    InsertOneResult res = PowerMockito.mock(InsertOneResult.class);
                    PowerMockito.when(res.getInsertedId()).thenReturn(new BsonInt32(123));
                    PowerMockito.when(res.wasAcknowledged()).thenReturn(true);
                    return res;
                });
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("mydb.mycol.insertOne({name:'zhangsan'})");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void insertMany() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                PowerMockito.when(mockColl.insertMany(any(List.class), any(InsertManyOptions.class))).thenAnswer(inv -> {
                    List<Document> docs = inv.getArgument(0);
                    assert docs.size() == 2;
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

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("mydb.mycol.insertMany([{name:'zhangsan'}, {name:'lisi'}])");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void deleteOne() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                DeleteResult res = PowerMockito.mock(DeleteResult.class);
                PowerMockito.when(res.getDeletedCount()).thenReturn(1L);
                PowerMockito.when(mockColl.deleteOne(any(Bson.class), any(DeleteOptions.class))).thenReturn(res);
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            int count = stmt.executeUpdate("mydb.mycol.deleteOne({name:'zhangsan'})");
            assert count == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void deleteMany() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                DeleteResult res = PowerMockito.mock(DeleteResult.class);
                PowerMockito.when(res.getDeletedCount()).thenReturn(5L);
                PowerMockito.when(mockColl.deleteMany(any(Bson.class), any(DeleteOptions.class))).thenReturn(res);
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            int count = stmt.executeUpdate("mydb.mycol.deleteMany({name:'zhangsan'})");
            assert count == 5;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void updateOne() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                UpdateResult res = PowerMockito.mock(UpdateResult.class);
                PowerMockito.when(res.getModifiedCount()).thenReturn(1L);
                PowerMockito.when(mockColl.updateOne(any(Bson.class), any(Bson.class), any(UpdateOptions.class))).thenReturn(res);
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            int count = stmt.executeUpdate("mydb.mycol.updateOne({name:'zhangsan'}, {$set:{age:20}})");
            assert count == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void updateMany() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                UpdateResult res = PowerMockito.mock(UpdateResult.class);
                PowerMockito.when(res.getModifiedCount()).thenReturn(10L);
                PowerMockito.when(mockColl.updateMany(any(Bson.class), any(Bson.class), any(UpdateOptions.class))).thenReturn(res);
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            int count = stmt.executeUpdate("mydb.mycol.updateMany({name:'zhangsan'}, {$set:{age:20}})");
            assert count == 10;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void replaceOne() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                UpdateResult res = PowerMockito.mock(UpdateResult.class);
                PowerMockito.when(res.getModifiedCount()).thenReturn(1L);
                PowerMockito.when(mockColl.replaceOne(any(Bson.class), any(Document.class), any(ReplaceOptions.class))).thenReturn(res);
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            int count = stmt.executeUpdate("mydb.mycol.replaceOne({name:'zhangsan'}, {name:'lisi', age:20})");
            assert count == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void renameCollection() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                PowerMockito.doNothing().when(mockColl).renameCollection(any(MongoNamespace.class), any(RenameCollectionOptions.class));
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("mydb.mycol.renameCollection('newcol')");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void findOne() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                FindIterable iterable = PowerMockito.mock(FindIterable.class);
                PowerMockito.when(mockColl.find(any(Bson.class))).thenReturn(iterable);
                PowerMockito.when(iterable.limit(1)).thenReturn(iterable);

                MongoCursor cursor = PowerMockito.mock(MongoCursor.class);
                PowerMockito.when(cursor.hasNext()).thenReturn(true).thenReturn(false);
                PowerMockito.when(cursor.next()).thenReturn(new Document("name", "zhangsan"));
                PowerMockito.when(iterable.iterator()).thenReturn(cursor);

                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("mydb.mycol.findOne({name:'zhangsan'})");
            assert rs.next();
            assert rs.getString("_JSON").contains("zhangsan");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void stats() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("runCommand".equals(method.getName())) {
                Document doc = new Document("ok", 1.0);
                return doc;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("mydb.mycol.stats()");
            assert rs.next();
            assert rs.getString("_JSON").contains("ok");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }
}