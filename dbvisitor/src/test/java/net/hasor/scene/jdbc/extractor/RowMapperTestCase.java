package net.hasor.scene.jdbc.extractor;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.dbvisitor.jdbc.PreparedStatementSetter;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.scene.UserNameRowMapper;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** 使用 RowMapper 接口来接收结果集 */
public class RowMapperTestCase {
    @Test
    public void noArgsExtractor_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowMapper rowMapper = new UserNameRowMapper();
            Object[] args = new Object[] { SqlArg.valueOf(40) };
            List<String> result = jdbcTemplate.queryForList("select * from user_table where age > ? order by id", args, rowMapper);

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void arrayArgsRowMapper_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowMapper rowMapper = new UserNameRowMapper();
            List<String> result = jdbcTemplate.queryForList("select * from user_table where age > 40 order by id", rowMapper);

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void mapArgsRowMapper_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowMapper rowMapper = new UserNameRowMapper();
            Map<String, Object> args = CollectionUtils.asMap("age", 40);
            List<String> result = jdbcTemplate.queryForList("select * from user_table where age > :age order by id", args, rowMapper);

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void setterArgsRowMapper_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowMapper rowMapper = new UserNameRowMapper();
            PreparedStatementSetter setter = ps -> ps.setInt(1, 40);
            List<String> result = jdbcTemplate.queryForList("select * from user_table where age > ? order by id", setter, rowMapper);

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void sourceArgsRowMapper_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowMapper rowMapper = new UserNameRowMapper();
            SqlArgSource argSource = new MapSqlArgSource(CollectionUtils.asMap("arg", 40));
            List<String> result = jdbcTemplate.queryForList("select * from user_table where age > :arg order by id", argSource, rowMapper);

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void paramArgsRowMapper_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowMapper rowMapper = new UserNameRowMapper();
            Object[] args = new Object[] { SqlArg.valueOf(40) };
            List<String> result = jdbcTemplate.queryForList("select * from user_table where age > ? order by id", args, rowMapper);

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }
}