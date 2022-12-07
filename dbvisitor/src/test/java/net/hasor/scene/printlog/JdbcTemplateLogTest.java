package net.hasor.scene.printlog;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTemplateLogTest extends AbstractPrintLogTest {

    @Test
    public void testtest() throws SQLException {
        try (Connection conn = DsUtils.h2Conn()) {
            this.printLog.clear();
            new JdbcTemplate(conn).execute("select 1");
            assert StringUtils.join(this.printLog.toArray()).contains("select 1");
        }

    }
}
