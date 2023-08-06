package net.hasor.scene.jdbc.query;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.jdbc.SqlParameterSource;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.paramer.MapSqlParameterSource;
import net.hasor.scene.UserDTO;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** 使用 SqlParameterSource 接口来传递 SQL 参数 */
public class SourceArgsTestCase {
    @Test
    public void sourceArgs_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            SqlParameterSource argSource = new MapSqlParameterSource(CollectionUtils.asMap("arg", 40));
            List<UserDTO> result = jdbcTemplate.queryForList("select * from user_table where age > :arg order by id", argSource, UserDTO.class);

            assert result.size() == 2;
            assert result.get(0).getName().equals("jon wes");
            assert result.get(1).getName().equals("mary");
        }
    }

    @Test
    public void sourceArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            SqlParameterSource argSource = new MapSqlParameterSource(CollectionUtils.asMap("arg", 40));
            List<Map<String, Object>> result = jdbcTemplate.queryForList("select * from user_table where age > :arg order by id", argSource);

            assert result.size() == 2;
            assert result.get(0).get("name").equals("jon wes");
            assert result.get(1).get("name").equals("mary");
        }
    }
}
