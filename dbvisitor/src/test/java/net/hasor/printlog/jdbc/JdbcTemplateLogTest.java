package net.hasor.printlog.jdbc;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.printlog.AbstractPrintLogTest;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTemplateLogTest extends AbstractPrintLogTest {

    @Test
    public void testtest() throws SQLException {
        try (Connection conn = DsUtils.h2Conn()) {
            printLog.clear();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("select 1");
        }

        assert printLog.isEmpty();
    }
}
