package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.mongodb.client.MongoDatabase;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.junit.Test;

public class DatabaseCommandTest extends AbstractJdbcTest {

    //@Test
    public void move_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getName", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("show collections") == 123L;
            }

            assert argList.equals(Arrays.asList("mykey", 123));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void test_show_collections() {
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.executeQuery("show collections");
                assert false;
            }
        } catch (SQLException e) {
            assert e.getMessage().contains("not implemented yet");
        }
    }

    @Test
    public void test_param_mismatch() {
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement stmt = conn.prepareStatement("db.createCollection(?)")) {
                stmt.executeUpdate();
                assert false;
            }
        } catch (SQLException e) {
            assert e.getMessage().contains("param size not match.");
        }
    }

    @Test
    public void test_empty_command() {
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.executeQuery("");
                assert false;
            }
        } catch (SQLException e) {
            assert e.getMessage().contains("query command is empty.");
        }
    }
}
