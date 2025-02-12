package net.hasor.dbvisitor.jdbc.core;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.dbvisitor.jdbc.core.test.UserNameResultSetExtractor;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class QueryUsingResultSetExtractorTest {
    @Test
    public void noargs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.query("select * from user_table where age > 40 order by id", new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void usingPosArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.query("select * from user_table where age > ? order by id", 40, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void usingPosArgs_2() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.query("select * from user_table where age > ? order by id", new int[] { 40 }, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void usingPosArgs_3() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.query("select * from user_table where age > ? order by id", new Object[] { 40 }, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void usingNamedArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("age", 40);
            List<String> result = jdbcTemplate.query("select * from user_table where age > :age order by id", args, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void usingNamedArgs_2() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("age", 40);
            List<String> result = jdbcTemplate.query("select * from user_table where age > &age order by id", args, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void usingNamedArgs_3() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("age", 40);
            List<String> result = jdbcTemplate.query("select * from user_table where age > #{age} order by id", args, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void usingInjectArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("age", 40);
            List<String> result = jdbcTemplate.query("select * from user_table where age > ${age} order by id", args, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void usingRuleArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("age", 40);
            List<String> result = jdbcTemplate.query("select * from user_table where age > @{arg,true, age} order by id", args, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void argType_as_map_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("age", 40);
            List<String> result = jdbcTemplate.query("select * from user_table where age > :age order by id", args, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void argtype_as_pos_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.query("select * from user_table where age > ? order by id", new Object[] { 40 }, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void argtype_as_source_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("age", 40);
            MapSqlArgSource source = new MapSqlArgSource(args);
            List<String> result = jdbcTemplate.query("select * from user_table where age > :age order by id", source, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void argtype_as_setter_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.query("select * from user_table where age > ? order by id", ps -> {
                ps.setInt(1, 40);
            }, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }
}
