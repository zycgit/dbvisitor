package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.mongodb.client.*;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.mockito.ArgumentMatchers.any;

public class CollectionQueryTest extends AbstractJdbcTest {

    @Test
    public void count_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            PowerMockito.when(mockColl.countDocuments(any(Bson.class))).thenReturn(10L);
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            try (ResultSet rs = stmt.executeQuery("db.mycol.count()")) {
                assert rs.next();
                assert rs.getLong("COUNT") == 10L;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void count_1() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            PowerMockito.when(mockColl.countDocuments(any(Bson.class))).thenAnswer(inv -> {
                return 5L;
            });
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            try (ResultSet rs = stmt.executeQuery("db.mycol.count({age: {$gt: 20}})")) {
                assert rs.next();
                assert rs.getLong("COUNT") == 5L;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void distinct_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            List<String> values = Arrays.asList("zhangsan", "lisi");
            DistinctIterable iterable = PowerMockito.mock(DistinctIterable.class);
            MongoCursor cursor = PowerMockito.mock(MongoCursor.class);
            java.util.Iterator<String> iterator = values.iterator();

            PowerMockito.when(iterable.iterator()).thenReturn(cursor);
            PowerMockito.when(cursor.hasNext()).thenAnswer(inv -> iterator.hasNext());
            PowerMockito.when(cursor.next()).thenAnswer(inv -> iterator.next());

            PowerMockito.when(mockColl.distinct(any(String.class), any(Bson.class), any(Class.class))).thenReturn(iterable);
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            try (ResultSet rs = stmt.executeQuery("db.mycol.distinct('name')")) {
                assert rs.next();
                assert "zhangsan".equals(rs.getString("VALUE"));
                assert rs.next();
                assert "lisi".equals(rs.getString("VALUE"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void distinct_1() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            List<String> values = Arrays.asList("wangwu");
            DistinctIterable iterable = PowerMockito.mock(DistinctIterable.class);
            MongoCursor cursor = PowerMockito.mock(MongoCursor.class);
            java.util.Iterator<String> iterator = values.iterator();

            PowerMockito.when(iterable.iterator()).thenReturn(cursor);
            PowerMockito.when(cursor.hasNext()).thenAnswer(inv -> iterator.hasNext());
            PowerMockito.when(cursor.next()).thenAnswer(inv -> iterator.next());

            PowerMockito.when(mockColl.distinct(any(String.class), any(Bson.class), any(Class.class))).thenReturn(iterable);
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            try (ResultSet rs = stmt.executeQuery("db.mycol.distinct('name', {age: {$gt: 20}})")) {
                assert rs.next();
                assert "wangwu".equals(rs.getString("VALUE"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void find_objectId_param() {
        MongoCommandInterceptor.resetInterceptor();
        AtomicReference<Bson> filterRef = new AtomicReference<>();

        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            FindIterable iterable = PowerMockito.mock(FindIterable.class);
            MongoCursor cursor = PowerMockito.mock(MongoCursor.class);

            PowerMockito.when(cursor.hasNext()).thenReturn(false);
            PowerMockito.when(iterable.iterator()).thenReturn(cursor);
            PowerMockito.when(mockColl.find(any(Bson.class))).thenAnswer(inv -> {
                filterRef.set(inv.getArgument(0));
                return iterable;
            });
            return mockColl;
        }));

        String oidHex = "69399ba1c488515851cecdfb";
        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement(); PreparedStatement ps = conn.prepareStatement("db.complex_order.find({_id: ObjectId(?)})")) {
            stmt.execute("use mydb");
            ps.setString(1, oidHex);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // no-op; mock cursor returns empty
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }

        Bson filter = filterRef.get();
        assert filter != null;
        org.bson.BsonDocument doc = filter.toBsonDocument(org.bson.BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
        Assert.assertTrue(doc.containsKey("_id"));
        Assert.assertEquals("filter: " + doc.toJson(), new org.bson.types.ObjectId(oidHex), doc.getObjectId("_id").getValue());
    }
}
