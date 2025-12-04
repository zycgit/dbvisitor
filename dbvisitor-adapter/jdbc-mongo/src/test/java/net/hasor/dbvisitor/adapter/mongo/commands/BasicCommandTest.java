package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.SQLException;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import org.junit.Test;

public class BasicCommandTest extends AbstractJdbcTest {

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
