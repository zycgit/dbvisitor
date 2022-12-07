package net.hasor.scene.jdbc.query;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.scene.UserDTO;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class NoArgsTestCase {
    @Test
    public void noArgs_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            List<UserDTO> result = jdbcTemplate.queryForList("select * from user where age > 40 order by id", UserDTO.class);

            assert result.size() == 2;
            assert result.get(0).getName().equals("jon wes");
            assert result.get(1).getName().equals("mary");
        }
    }

    @Test
    public void noArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            List<Map<String, Object>> result = jdbcTemplate.queryForList("select * from user where age > 40 order by id");

            assert result.size() == 2;
            assert result.get(0).get("name").equals("jon wes");
            assert result.get(1).get("name").equals("mary");
        }
    }
}
