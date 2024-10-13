package net.hasor.dbvisitor.jdbc.core;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.test.dto.UserInfo;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryUsingNothingTest {
    @Test
    public void noargs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info");
            assert users.size() == 3;
            assert TestUtils.beanForData1().getUserUuid().equals(users.get(0).get("user_UUID"));
            assert TestUtils.beanForData2().getUserUuid().equals(users.get(1).get("user_UUID"));
            assert TestUtils.beanForData3().getUserUuid().equals(users.get(2).get("user_UUID"));
        }
    }

    @Test
    public void usingPosArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void usingPosArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", user.getUserUuid());
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void usingPosArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", new String[] { user.getUserUuid() });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void usingNamedArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :userUuid", user);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void usingNamedArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = &userUuid", user);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void usingNamedArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid =#{userUuid}", user);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void usingInjectArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid =${\"'\" + userUuid + \"'\"}", user);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void usingRuleArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid =@{arg,true,userUuid}", user);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void argType_as_map_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :uuid", mapParams);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void argtype_as_pos_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            String[] args = new String[] { user.getUserUuid() };
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", args);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void argtype_as_source_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            MapSqlArgSource source = new MapSqlArgSource(mapParams);
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :uuid", source);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void argtype_as_setter_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", ps -> {
                ps.setString(1, user.getUserUuid());
            });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }
}
