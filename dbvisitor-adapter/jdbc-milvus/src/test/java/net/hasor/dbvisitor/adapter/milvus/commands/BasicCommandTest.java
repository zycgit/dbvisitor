package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.SQLException;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import org.junit.Test;

public class BasicCommandTest extends AbstractJdbcTest {

    @Test
    public void test_connect() throws SQLException {
        try (Connection conn = milvusConnection()) {
            assert conn != null;
        }
    }
}
