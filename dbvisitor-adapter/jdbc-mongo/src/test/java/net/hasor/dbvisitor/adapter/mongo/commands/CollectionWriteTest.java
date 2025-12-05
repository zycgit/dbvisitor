package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.mockito.ArgumentMatchers.any;

public class CollectionWriteTest extends AbstractJdbcTest {
    @Test
    public void insert_0() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                assert ((MongoDatabase) proxy).getName().equals("mydb1");
                assert args[0].equals("mycol");

                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                PowerMockito.when(mockColl.insertOne(any())).thenAnswer(inv -> {
                    Document doc = inv.getArgument(0);
                    assert doc.get("name").equals("zhangsan");
                    return null;
                });
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("mydb1.mycol.insert({name:'zhangsan'})");
        }
    }

    @Test
    public void insert_1() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                assert ((MongoDatabase) proxy).getName().equals("mydb");
                assert args[0].equals("mycol");

                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                PowerMockito.when(mockColl.insertMany(any())).thenAnswer(inv -> {
                    List<Document> docs = inv.getArgument(0);
                    assert docs.size() == 2;
                    assert docs.get(0).get("name").equals("zhangsan");
                    assert docs.get(1).get("name").equals("lisi");
                    return null;
                });
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection("mydb"); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("db.mycol.insert([{name:'zhangsan'}, {name:'lisi'}])");
        }
    }

    @Test
    public void update_0() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                assert ((MongoDatabase) proxy).getName().equals("bbb");
                assert args[0].equals("mycol");

                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
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
        }
    }

    @Test
    public void remove_0() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                assert ((MongoDatabase) proxy).getName().equals("mydb");
                assert args[0].equals("mycol");

                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
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
        }
    }

    @Test
    public void bulk_write_0() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("getCollection".equals(method.getName())) {
                assert ((MongoDatabase) proxy).getName().equals("mydb");
                assert args[0].equals("mycol");

                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                BulkWriteResult res = PowerMockito.mock(BulkWriteResult.class);
                PowerMockito.when(res.getInsertedCount()).thenReturn(1);
                PowerMockito.when(res.getModifiedCount()).thenReturn(1);
                PowerMockito.when(res.getDeletedCount()).thenReturn(0);
                PowerMockito.when(mockColl.bulkWrite(any(List.class), any(BulkWriteOptions.class))).thenReturn(res);
                return mockColl;
            }
            return null;
        });

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            int count = stmt.executeUpdate("mydb.mycol.bulkWrite([ " +            //
                    "{ insertOne : { document : { name : 'zhangsan' } } }, " +  //
                    "{ updateOne : { filter : { name : 'lisi' }," +             //
                    " update : { $set : { age : 20 } } } } ])");
            assert count == 2;
        }
    }
}
