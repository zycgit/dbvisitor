package net.hasor.scene.jdbc.callback;
import net.hasor.dbvisitor.jdbc.PreparedStatementCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.scene.UserNameResultSetExtractor;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PreparedStatementCallbackTestCase {
    @Test
    public void callBack_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.executeCallback("select * from user where age > ? order by id", (PreparedStatementCallback<List<String>>) ps -> {
                ps.setInt(1, 40);
                try (ResultSet rs = ps.executeQuery()) {
                    return new UserNameResultSetExtractor().extractData(rs);
                }
            });

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }
}