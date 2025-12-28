package net.hasor.dbvisitor.adapter.elastic.commands;
import java.sql.Connection;
import java.sql.SQLException;
import net.hasor.dbvisitor.adapter.elastic.AbstractJdbcTest;
import org.junit.Test;

public class BasicCommandTest extends AbstractJdbcTest {

    @Test
    public void test_param_mismatch() {
        try (Connection conn = elasticConnection()) {
            try (java.sql.PreparedStatement stmt = conn.prepareStatement("GET /")) {
                stmt.executeUpdate();
                assert false;
            }
        } catch (SQLException e) {
            if (!e.getMessage().contains("param size not match.")) {
                e.printStackTrace();
            }
            assert e.getMessage().contains("param size not match.");
        }
    }
}
