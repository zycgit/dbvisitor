package net.hasor.dbvisitor.jdbc.core;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.dynamic.args.BeanSqlArgSource;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import net.hasor.test.utils.UserInfo;
import org.junit.Test;

public class IntUsingNothingTest {
    @Test
    public void noargs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info");
            assert userCountInt == 3;
        }
    }

    @Test
    public void usingPosArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != ?", new Object[] { user.getUserUuid() });
            assert userCountInt == 2;
        }
    }

    @Test
    public void usingPosArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != ?", user.getUserUuid());
            assert userCountInt == 2;
        }
    }

    @Test
    public void usingPosArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != ?", new String[] { user.getUserUuid() });
            assert userCountInt == 2;
        }
    }

    @Test
    public void usingNamedArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != :userUuid", user);
            assert userCountInt == 2;
        }
    }

    @Test
    public void usingNamedArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != &userUuid", user);
            assert userCountInt == 2;
        }
    }

    @Test
    public void usingNamedArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != #{userUuid}", user);
            assert userCountInt == 2;
        }
    }

    @Test
    public void usingInjectArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != ${\"'\" + userUuid + \"'\"}", user);
            assert userCountInt == 2;
        }
    }

    @Test
    public void usingRuleArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != @{arg,true,userUuid}", user);
            assert userCountInt == 2;
        }
    }

    @Test
    public void argType_as_map_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != :uuid", mapParams);
            assert userCountInt == 2;
        }
    }

    @Test
    public void argtype_as_pos_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != ?", new Object[] { user.getUserUuid() });
            assert userCountInt == 2;
        }
    }

    @Test
    public void argtype_as_source_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            BeanSqlArgSource source = new BeanSqlArgSource(user);
            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != :userUuid", source);
            assert userCountInt == 2;
        }
    }

    @Test
    public void argtype_as_setter_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            int userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != ?", ps -> {
                ps.setString(1, user.getUserUuid());
            });
            assert userCountInt == 2;
        }
    }
}
