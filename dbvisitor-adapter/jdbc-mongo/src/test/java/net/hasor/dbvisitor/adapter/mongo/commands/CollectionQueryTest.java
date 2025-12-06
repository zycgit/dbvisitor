package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.conversions.Bson;
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
}
