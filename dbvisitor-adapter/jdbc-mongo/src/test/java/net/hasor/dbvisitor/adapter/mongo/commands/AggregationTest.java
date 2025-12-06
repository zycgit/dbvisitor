package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("unchecked")
public class AggregationTest extends AbstractJdbcTest {
    @Test
    public void aggregate_options_test() throws SQLException {
        AggregateIterable<Document> iterable = Mockito.mock(AggregateIterable.class);
        Mockito.when(iterable.batchSize(Mockito.anyInt())).thenReturn(iterable);
        Mockito.when(iterable.maxTime(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(iterable);
        Mockito.when(iterable.allowDiskUse(Mockito.anyBoolean())).thenReturn(iterable);
        Mockito.when(iterable.iterator()).thenReturn(Mockito.mock(com.mongodb.client.MongoCursor.class));

        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoCollection.class, createInvocationHandler("aggregate", (name, args) -> {
            return iterable;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            stmt.executeQuery("db.mycol.aggregate([{$match: {}}], { allowDiskUse: true, batchSize: 100, maxTimeMS: 5000 })");
        }

        Mockito.verify(iterable).allowDiskUse(true);
        Mockito.verify(iterable).batchSize(100);
        Mockito.verify(iterable).maxTime(5000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void aggregate_prepared_statement_test() throws SQLException {
        AggregateIterable<Document> iterable = Mockito.mock(AggregateIterable.class);
        Mockito.when(iterable.batchSize(Mockito.anyInt())).thenReturn(iterable);
        Mockito.when(iterable.maxTime(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(iterable);
        Mockito.when(iterable.allowDiskUse(Mockito.anyBoolean())).thenReturn(iterable);
        Mockito.when(iterable.iterator()).thenReturn(Mockito.mock(com.mongodb.client.MongoCursor.class));

        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoCollection.class, createInvocationHandler("aggregate", (name, args) -> {
            return iterable;
        }));

        try (Connection conn = redisConnection("mydb"); java.sql.PreparedStatement stmt = conn.prepareStatement("db.mycol.aggregate([{$match: {}}], { allowDiskUse: ?, batchSize: ? })")) {
            stmt.setBoolean(1, true);
            stmt.setInt(2, 100);
            stmt.executeQuery();
        }

        Mockito.verify(iterable).allowDiskUse(true);
        Mockito.verify(iterable).batchSize(100);
    }

    @Test
    public void aggregate_options_error_test() throws SQLException {
        AggregateIterable<Document> iterable = Mockito.mock(AggregateIterable.class);
        Mockito.when(iterable.iterator()).thenReturn(Mockito.mock(com.mongodb.client.MongoCursor.class));

        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoCollection.class, createInvocationHandler("aggregate", (name, args) -> {
            return iterable;
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
