package net.hasor.scene.jdbc.query;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.LongTypeHandler;
import net.hasor.scene.UserDTO;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** 将参数封装为 SqlParameter 接口来传递 SQL 参数（使用 SqlParameterUtils 工具类） */
public class SqlArgTestCase {
    @Test
    public void paramArgs_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Object[] args = new Object[] { SqlArg.valueOf(40) };
            List<UserDTO> result = jdbcTemplate.queryForList("select * from user_table where age > ? order by id", args, UserDTO.class);

            assert result.size() == 2;
            assert result.get(0).getName().equals("jon wes");
            assert result.get(1).getName().equals("mary");
        }
    }

    @Test
    public void paramArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            SqlArg[] args = new SqlArg[] { SqlArg.valueOf(40) };
            List<UserDTO> result = jdbcTemplate.queryForList("select * from user_table where age > ? order by id", args, UserDTO.class);

            assert result.size() == 2;
            assert result.get(0).getName().equals("jon wes");
            assert result.get(1).getName().equals("mary");
        }
    }

    @Test
    public void paramArgs_2() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            LongTypeHandler handler = new LongTypeHandler();
            SqlArg[] args = new SqlArg[] { SqlArg.valueOf(40L, handler) };
            List<Map<String, Object>> result = jdbcTemplate.queryForList("select * from user_table where age > ? order by id", args);

            assert result.size() == 2;
            assert result.get(0).get("name").equals("jon wes");
            assert result.get(1).get("name").equals("mary");
        }
    }

    @Test
    public void paramArgs_3() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Object[] args = new Object[] { SqlArg.valueOf(40) };
            List<Map<String, Object>> result = jdbcTemplate.queryForList("select * from user_table where age > ? order by id", args);

            assert result.size() == 2;
            assert result.get(0).get("name").equals("jon wes");
            assert result.get(1).get("name").equals("mary");
        }
    }
}
