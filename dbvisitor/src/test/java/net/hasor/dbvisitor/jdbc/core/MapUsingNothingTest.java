package net.hasor.dbvisitor.jdbc.core;
import net.hasor.dbvisitor.dynamic.args.BeanSqlArgSource;
import net.hasor.test.dto.UserInfo;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MapUsingNothingTest {
    @Test
    public void noargs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = '" + user.getUserUuid() + "'");
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void usingPosArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() });
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void usingPosArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = ?", user.getUserUuid());
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void usingPosArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = ?", new String[] { user.getUserUuid() });
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void usingNamedArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = :userUuid", user);
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void usingNamedArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = &userUuid", user);
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void usingNamedArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = #{userUuid}", user);
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void usingInjectArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = ${\"'\" + userUuid + \"'\"}", user);
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void usingRuleArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = @{arg,true,userUuid}", user);
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void argType_as_map_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = :uuid", mapParams);
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void argtype_as_pos_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() });
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void argtype_as_source_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            BeanSqlArgSource beanSqlParameterSource = new BeanSqlArgSource(user);
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = :userUuid", beanSqlParameterSource);
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void argtype_as_setter_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = ?", ps -> {
                ps.setString(1, user.getUserUuid());
            });
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }
}
