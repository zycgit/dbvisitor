package net.hasor.scene.jdbc.callback;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionCallbackTestCase {
    @Test
    public void callBack_0() throws SQLException {
        try (Connection c = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            String res = jdbcTemplate.execute((ConnectionCallback<String>) con -> {
                return con.getMetaData().getDriverName();
            });

            assert res.toLowerCase().contains("mysql");
        }
    }
}