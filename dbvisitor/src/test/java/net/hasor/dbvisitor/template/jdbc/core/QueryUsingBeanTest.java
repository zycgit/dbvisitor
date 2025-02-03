package net.hasor.dbvisitor.template.jdbc.core;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.dbvisitor.template.jdbc.PreparedStatementSetter;
import net.hasor.dbvisitor.template.jdbc.UserDTO;
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

public class QueryUsingBeanTest {
    @Test
    public void noargs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info", UserInfo2.class);
            assert users.size() == 3;
            assert TestUtils.beanForData1().getUserUuid().equals(users.get(0).getUid());
            assert TestUtils.beanForData2().getUserUuid().equals(users.get(1).getUid());
            assert TestUtils.beanForData3().getUserUuid().equals(users.get(2).getUid());
        }
    }

    @Test
    public void usingPosArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() }, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void usingPosArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", user.getUserUuid(), UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void usingPosArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", new String[] { user.getUserUuid() }, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void usingNamedArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :uuid", mapParams, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void usingNamedArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = &uuid", mapParams, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void usingNamedArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = #{uuid}", mapParams, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void usingInjectArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ${\"'\" + uuid + \"'\"}", mapParams, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void usingRuleArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = @{arg,true,uuid}", mapParams, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void argType_as_map_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :uuid", mapParams, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void argtype_as_pos_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            String[] args = new String[] { user.getUserUuid() };
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", args, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
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
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :uuid", source, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void argtype_as_setter_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            PreparedStatementSetter setter = ps -> ps.setInt(1, 40);
            List<UserDTO> result = jdbcTemplate.queryForList("select * from user_table where age > ? order by id", setter, UserDTO.class);

            assert result.size() == 2;
            assert result.get(0).getName().equals("jon wes");
            assert result.get(1).getName().equals("mary");
        }
    }
}
