/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.jdbc.core;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.args.BeanSqlArgSource;
import net.hasor.dbvisitor.jdbc.PreparedStatementSetter;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo;
import net.hasor.test.dto.user_info;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/***
 * executeUpdate 系列方法测试
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class ExecuteUpdateTest extends AbstractDbTest {
    @Test
    public void noargs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            assert jdbcTemplate.executeUpdate("update user_info set user_name = '123'") == 3;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 1;
            assert collect2.contains("123");
        }
    }

    @Test
    public void usingPosArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            assert jdbcTemplate.executeUpdate("update user_info set user_name = ?", "123") == 3;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 1;
            assert collect2.contains("123");
        }
    }

    @Test
    public void usingPosArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            assert jdbcTemplate.executeUpdate("update user_info set user_name = ?", new Object[] { "123" }) == 3;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 1;
            assert collect2.contains("123");
        }
    }

    @Test
    public void usingPosArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            assert jdbcTemplate.executeUpdate("update user_info set user_name = ?", new String[] { "123" }) == 3;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 1;
            assert collect2.contains("123");
        }
    }

    @Test
    public void usingNamedArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            UserInfo tbUser = TestUtils.beanForData1();
            BeanSqlArgSource beanSqlParameterSource = new BeanSqlArgSource(tbUser);
            assert jdbcTemplate.executeUpdate("update user_info set user_name = '123' where user_uuid != :userUuid", beanSqlParameterSource) == 2;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 2;
            assert collect2.contains("123");
            assert collect2.contains(tbUser.getName());
        }
    }

    @Test
    public void usingNamedArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            UserInfo tbUser = TestUtils.beanForData1();
            BeanSqlArgSource beanSqlParameterSource = new BeanSqlArgSource(tbUser);
            assert jdbcTemplate.executeUpdate("update user_info set user_name = '123' where user_uuid != &userUuid", beanSqlParameterSource) == 2;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 2;
            assert collect2.contains("123");
            assert collect2.contains(tbUser.getName());
        }
    }

    @Test
    public void usingNamedArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            UserInfo tbUser = TestUtils.beanForData1();
            BeanSqlArgSource beanSqlParameterSource = new BeanSqlArgSource(tbUser);
            assert jdbcTemplate.executeUpdate("update user_info set user_name = '123' where user_uuid != #{userUuid}", beanSqlParameterSource) == 2;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 2;
            assert collect2.contains("123");
            assert collect2.contains(tbUser.getName());
        }
    }

    @Test
    public void usingInjectArgs_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            UserInfo tbUser = TestUtils.beanForData1();
            BeanSqlArgSource beanSqlParameterSource = new BeanSqlArgSource(tbUser);
            assert jdbcTemplate.executeUpdate("update user_info set user_name = '123' where user_uuid != ${\"'\" + userUuid + \"'\"}", beanSqlParameterSource) == 2;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 2;
            assert collect2.contains("123");
            assert collect2.contains(tbUser.getName());
        }
    }

    @Test
    public void usingRuleArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            UserInfo tbUser = TestUtils.beanForData1();
            BeanSqlArgSource beanSqlParameterSource = new BeanSqlArgSource(tbUser);
            assert jdbcTemplate.executeUpdate("update user_info set user_name = '123' where user_uuid != @{arg,true,userUuid}", beanSqlParameterSource) == 2;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 2;
            assert collect2.contains("123");
            assert collect2.contains(tbUser.getName());
        }
    }

    @Test
    public void argType_as_map_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            UserInfo tbUser = TestUtils.beanForData1();
            Map<String, Object> map = CollectionUtils.asMap("uuid", tbUser.getUserUuid());
            assert jdbcTemplate.executeUpdate("update user_info set user_name = '123' where user_uuid != :uuid", map) == 2;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 2;
            assert collect2.contains("123");
            assert collect2.contains(tbUser.getName());
        }
    }

    @Test
    public void argtype_as_pos_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            assert jdbcTemplate.executeUpdate("update user_info set user_name = ?", new Object[] { "123" }) == 3;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 1;
            assert collect2.contains("123");
        }
    }

    @Test
    public void argtype_as_source_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            UserInfo tbUser = TestUtils.beanForData1();
            BeanSqlArgSource beanSqlParameterSource = new BeanSqlArgSource(tbUser);
            assert jdbcTemplate.executeUpdate("update user_info set user_name = '123' where user_uuid != :userUuid", beanSqlParameterSource) == 2;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 2;
            assert collect2.contains("123");
            assert collect2.contains(tbUser.getName());
        }
    }

    @Test
    public void argtype_as_setter_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;

            assert jdbcTemplate.executeUpdate("update user_info set user_name = ?", (PreparedStatementSetter) ps -> {
                ps.setString(1, "123");
            }) == 3;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 1;
            assert collect2.contains("123");
        }
    }
}
