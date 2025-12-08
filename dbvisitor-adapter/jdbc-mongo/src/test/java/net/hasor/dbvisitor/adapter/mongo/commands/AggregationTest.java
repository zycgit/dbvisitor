package net.hasor.dbvisitor.adapter.mongo.commands;
import com.mongodb.client.MongoDatabase;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import static org.mockito.ArgumentMatchers.any;

@SuppressWarnings("unchecked")
public class AggregationTest extends AbstractJdbcTest {
    @Test
    public void aggregate_options_test() {
        AggregateIterable<Document> iterable = Mockito.mock(AggregateIterable.class);
        Mockito.when(iterable.batchSize(Mockito.anyInt())).thenReturn(iterable);
        Mockito.when(iterable.maxTime(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(iterable);
        Mockito.when(iterable.allowDiskUse(Mockito.anyBoolean())).thenReturn(iterable);
        Mockito.when(iterable.iterator()).thenReturn(Mockito.mock(com.mongodb.client.MongoCursor.class));

        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler(new String[] { "runCommand", "getCollection" }, (name, args) -> {
            if ("runCommand".equals(name)) {
                Document doc = (Document) args[0];
                if (doc.containsKey("buildInfo")) {
                    return new Document("version", "4.0.0");
                }
            }
            if ("getCollection".equals(name)) {
                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                PowerMockito.when(mockColl.aggregate(any(List.class))).thenReturn(iterable);
                return mockColl;
            }
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeQuery("db.mycol.aggregate([{$match: {}}], { allowDiskUse: true, batchSize: 100, maxTimeMS: 5000 })");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }

        Mockito.verify(iterable).allowDiskUse(true);
        Mockito.verify(iterable).batchSize(100);
        Mockito.verify(iterable).maxTime(5000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void aggregate_prepared_statement_test() {
        AggregateIterable<Document> iterable = Mockito.mock(AggregateIterable.class);
        Mockito.when(iterable.batchSize(Mockito.anyInt())).thenReturn(iterable);
        Mockito.when(iterable.maxTime(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(iterable);
        Mockito.when(iterable.allowDiskUse(Mockito.anyBoolean())).thenReturn(iterable);
        Mockito.when(iterable.iterator()).thenReturn(Mockito.mock(com.mongodb.client.MongoCursor.class));

        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler(new String[] { "runCommand", "getCollection" }, (name, args) -> {
            if ("runCommand".equals(name)) {
                Document doc = (Document) args[0];
                if (doc.containsKey("buildInfo")) {
                    return new Document("version", "4.0.0");
                }
            }
            if ("getCollection".equals(name)) {
                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                PowerMockito.when(mockColl.aggregate(any(List.class))).thenReturn(iterable);
                return mockColl;
            }
            return null;
        }));

        try (Connection conn = redisConnection("mydb"); java.sql.PreparedStatement stmt = conn.prepareStatement("db.mycol.aggregate([{$match: {}}], { allowDiskUse: ?, batchSize: ? })")) {
            stmt.setBoolean(1, true);
            stmt.setInt(2, 100);
            stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }

        Mockito.verify(iterable).allowDiskUse(Mockito.anyBoolean());
        Mockito.verify(iterable).batchSize(100);
    }

    @Test
    public void aggregate_options_error_test() {
        AggregateIterable<Document> iterable = Mockito.mock(AggregateIterable.class);
        Mockito.when(iterable.iterator()).thenReturn(Mockito.mock(com.mongodb.client.MongoCursor.class));

        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler(new String[] { "runCommand", "getCollection" }, (name, args) -> {
            if ("runCommand".equals(name)) {
                Document doc = (Document) args[0];
                if (doc.containsKey("buildInfo")) {
                    return new Document("version", "4.0.0");
                }
            }
            if ("getCollection".equals(name)) {
                MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
                PowerMockito.when(mockColl.aggregate(any(List.class))).thenReturn(iterable);
                return mockColl;
            }
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeQuery("db.mycol.aggregate([{$match: {}}], { allowDiskUse: 123 })");
            Assert.fail("Should throw SQLException");
        } catch (SQLException e) {
            Assert.assertEquals("allowDiskUse must be boolean", e.getMessage());
        }
    }
}
