package net.hasor.dbvisitor.jdbc.core;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import net.hasor.test.utils.UserInfo;
import org.junit.Test;

public class StringUsingNothingTest {
    @Test
    public void noargs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            String string = jdbcTemplate.queryForString("select 'abc'");
            assert string.equals("abc");
        }
    }

    @Test
    public void usingPosArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            String string = jdbcTemplate.queryForString("select ?", new Object[] { "abc" });
            assert string.equals("abc");
        }
    }

    @Test
    public void usingPosArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            String string = jdbcTemplate.queryForString("select ?", "abc");
            assert string.equals("abc");
        }
    }

    @Test
    public void usingPosArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            String string = jdbcTemplate.queryForString("select ?", new String[] { "abc" });
            assert string.equals("abc");
        }
    }

    @Test
    public void usingNamedArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            String string = jdbcTemplate.queryForString("select :userUuid", user);
            assert string.equals(user.getUserUuid());
        }
    }

    @Test
    public void usingNamedArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            String string = jdbcTemplate.queryForString("select &userUuid", user);
            assert string.equals(user.getUserUuid());
        }
    }

    @Test
    public void usingNamedArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            String string = jdbcTemplate.queryForString("select #{userUuid}", user);
            assert string.equals(user.getUserUuid());
        }
    }

    @Test
    public void usingInjectArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            String string = jdbcTemplate.queryForString("select ${\"'\" + userUuid + \"'\"}", user);
            assert string.equals(user.getUserUuid());
        }
    }

    @Test
    public void usingRuleArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            String string = jdbcTemplate.queryForString("select @{arg,true,userUuid}", user);
            assert string.equals(user.getUserUuid());
        }
    }

    @Test
    public void argType_as_map_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", "abc");
            String string = jdbcTemplate.queryForString("select :uuid", mapParams);
            assert string.equals("abc");
        }
    }

    @Test
    public void argtype_as_pos_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            String string = jdbcTemplate.queryForString("select ?", new Object[] { "abc" });
            assert string.equals("abc");
        }
    }

    @Test
    public void argtype_as_source_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", "abc");
            MapSqlArgSource source = new MapSqlArgSource(mapParams);
            String string = jdbcTemplate.queryForString("select :uuid", source);
            assert string.equals("abc");
        }
    }

    @Test
    public void argtype_as_setter_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            String string = jdbcTemplate.queryForString("select ?", ps -> {
                ps.setString(1, "abc");
            });
            assert string.equals("abc");
        }
    }
}
