package net.hasor.dbvisitor.adapter.mongo.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicReference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Verify hint arguments consume placeholders in order and are passed through with other parameters.
 */
public class HintCommandTest extends AbstractJdbcTest {

    @Test
    public void hint_args_consumed_before_command_db_and_collection() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();

        AtomicReference<String> dbRef = new AtomicReference<>();
        AtomicReference<String> collRef = new AtomicReference<>();

        // Intercept MongoClient.getDatabase -> capture db name; mock getCollection/drop
        MongoCommandInterceptor.addInterceptor(MongoClient.class, createInvocationHandler("getDatabase", (name, args) -> {
            dbRef.set((String) args[0]);
            MongoDatabase mockDb = PowerMockito.mock(MongoDatabase.class);
            MongoCollection<?> mockColl = PowerMockito.mock(MongoCollection.class);
            PowerMockito.when(mockDb.getCollection(anyString())).thenAnswer(inv -> {
                collRef.set(inv.getArgument(0));
                return mockColl;
            });
            return mockDb;
        }));

        // hint has 1 placeholder + db + collection placeholders => 3 total
        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement();
                PreparedStatement ps = conn.prepareStatement("/*+abc=?,abc=null;abc=aaa*/ ?.?.drop();")) {
            stmt.execute("use mydb");
            ps.setString(1, "hint-val");
            ps.setString(2, "dbX");
            ps.setString(3, "colX");
            ps.execute();
        }

        Assert.assertEquals("db should come from 2nd placeholder (after hint)", "dbX", dbRef.get());
        Assert.assertEquals("collection should come from 3rd placeholder", "colX", collRef.get());
    }

    @Test
    public void multiple_hint_blocks_consume_placeholders_in_order() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();

        AtomicReference<String> dbRef = new AtomicReference<>();
        AtomicReference<String> collRef = new AtomicReference<>();

        MongoCommandInterceptor.addInterceptor(MongoClient.class, createInvocationHandler("getDatabase", (name, args) -> {
            dbRef.set((String) args[0]);
            MongoDatabase mockDb = PowerMockito.mock(MongoDatabase.class);
            MongoCollection<?> mockColl = PowerMockito.mock(MongoCollection.class);
            PowerMockito.when(mockDb.getCollection(anyString())).thenAnswer(inv -> {
                collRef.set(inv.getArgument(0));
                return mockColl;
            });
            return mockDb;
        }));

        // Two hint blocks, each with a single placeholder; then db and collection placeholders
        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement();
                PreparedStatement ps = conn.prepareStatement("/*+abc1=?,abc2=null;abc3=aaa*//*+abc4=?,abc5=null;abc6=bb*/ ?.ccc.drop();")) {
            stmt.execute("use mydb");
            ps.setString(1, "hint1");
            ps.setString(2, "hint2");
            ps.setString(3, "dbY");
            ps.execute();
        }

        Assert.assertEquals("db should be consumed after both hint placeholders", "dbY", dbRef.get());
        Assert.assertEquals("collection should be literal from command", "ccc", collRef.get());
    }
}
