package net.hasor.scene.jdbc.query;
import net.hasor.dbvisitor.jdbc.PreparedStatementSetter;
import net.hasor.dbvisitor.jdbc.core.ArgPreparedStatementSetter;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.scene.UserDTO;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** 使用 PreparedStatementSetter 接口来传递 SQL 参数 */
public class PsSetterArgsTestCase {
    @Test
    public void setterArgs_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            PreparedStatementSetter setter = new ArgPreparedStatementSetter(new Object[] { 40 });
            List<Map<String, Object>> result = jdbcTemplate.queryForList("select * from user_table where age > ? order by id", setter);

            assert result.size() == 2;
            assert result.get(0).get("name").equals("jon wes");
            assert result.get(1).get("name").equals("mary");
        }
    }

    @Test
    public void setterArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            PreparedStatementSetter setter = new ArgPreparedStatementSetter(new Object[] { 40 });
            List<UserDTO> result = jdbcTemplate.queryForList("select * from user_table where age > ? order by id", setter, UserDTO.class);

            assert result.size() == 2;
            assert result.get(0).getName().equals("jon wes");
            assert result.get(1).getName().equals("mary");
        }
    }
}