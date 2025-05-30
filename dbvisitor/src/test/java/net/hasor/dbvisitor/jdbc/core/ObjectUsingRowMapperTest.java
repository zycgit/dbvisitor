package net.hasor.dbvisitor.jdbc.core;
import net.hasor.dbvisitor.dynamic.args.BeanSqlArgSource;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import net.hasor.test.utils.UserInfo;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectUsingRowMapperTest {
    @Test
    public void noargs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = '" + user1.getUserUuid() + "'", rowMapper);
            assert user2 != null;
            assert user2.getUid().equals(user1.getUserUuid());
        }
    }

    @Test
    public void usingPosArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = ?", new Object[] { user1.getUserUuid() }, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void usingPosArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = ?", user1.getUserUuid(), rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void usingPosArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = ?", new String[] { user1.getUserUuid() }, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void usingNamedArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user1.getUserUuid());
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = :uuid", mapParams, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void usingNamedArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user1.getUserUuid());
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = &uuid", mapParams, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void usingNamedArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user1.getUserUuid());
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = #{uuid}", mapParams, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void usingInjectArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user1.getUserUuid());
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = ${\"'\" + uuid + \"'\"}", mapParams, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void usingRuleArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user1.getUserUuid());
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = @{arg,true,uuid}", mapParams, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void argType_as_map_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :uuid", mapParams, rowMapper);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void argtype_as_pos_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = ?", new Object[] { user1.getUserUuid() }, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void argtype_as_source_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            BeanSqlArgSource source = new BeanSqlArgSource(user1);
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = :userUuid", source, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void argtype_as_setter_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            BeanMappingRowMapper<UserInfo2> rowMapper = new BeanMappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = ?", ps -> {
                ps.setString(1, user1.getUserUuid());
            }, rowMapper);

            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }
}
