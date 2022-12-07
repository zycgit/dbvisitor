package net.hasor.scene.jdbc.query;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.scene.UserDTO;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** 使用 Map 来传递 SQL 参数 */
public class MapArgsTestCase {
    @Test
    public void mapArgs_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("age", 40);
            List<Map<String, Object>> result = jdbcTemplate.queryForList("select * from user where age > :age order by id", args);

            assert result.size() == 2;
            assert result.get(0).get("name").equals("jon wes");
            assert result.get(1).get("name").equals("mary");
        }
    }

    @Test
    public void mapArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("age", new Object[] { 41, 66 });
            List<Map<String, Object>> result = jdbcTemplate.queryForList("select * from user where age in (:age) order by id", args);

            assert result.size() == 2;
            assert result.get(0).get("name").equals("jon wes");
            assert result.get(1).get("name").equals("mary");
        }
    }

    @Test
    public void mapArgs_2() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("age", 40);
            List<UserDTO> result = jdbcTemplate.queryForList("select * from user where age > :age order by id", args, UserDTO.class);

            assert result.size() == 2;
            assert result.get(0).getName().equals("jon wes");
            assert result.get(1).getName().equals("mary");
        }
    }

    @Test
    public void mapArgs_3() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("age", new Object[] { 41, 66 });
            List<UserDTO> result = jdbcTemplate.queryForList("select * from user where age in (:age) order by id", args, UserDTO.class);

            assert result.size() == 2;
            assert result.get(0).getName().equals("jon wes");
            assert result.get(1).getName().equals("mary");
        }
    }
}
