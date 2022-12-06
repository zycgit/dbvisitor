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
import net.hasor.dbvisitor.jdbc.paramer.BeanSqlParameterSource;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.TB_User;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.HashMap;
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
    public void executeUpdate_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            //
            List<TB_User> tbUsers1 = jdbcTemplate.queryForList("select * from tb_user", TB_User.class);
            Set<String> collect1 = tbUsers1.stream().map(TB_User::getName).collect(Collectors.toSet());
            assert collect1.size() == 3;
            //
            assert jdbcTemplate.executeUpdate("update tb_user set name = '123'") == 3;
            //
            List<TB_User> tbUsers2 = jdbcTemplate.queryForList("select * from tb_user", TB_User.class);
            Set<String> collect2 = tbUsers2.stream().map(TB_User::getName).collect(Collectors.toSet());
            assert collect2.size() == 1;
            assert collect2.contains("123");
        }
    }

    @Test
    public void executeUpdate_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            //
            List<TB_User> tbUsers1 = jdbcTemplate.queryForList("select * from tb_user", TB_User.class);
            Set<String> collect1 = tbUsers1.stream().map(TB_User::getName).collect(Collectors.toSet());
            assert collect1.size() == 3;
            //
            assert jdbcTemplate.executeUpdate("update tb_user set name = ?", ps -> {
                ps.setString(1, "123");
            }) == 3;
            //
            List<TB_User> tbUsers2 = jdbcTemplate.queryForList("select * from tb_user", TB_User.class);
            Set<String> collect2 = tbUsers2.stream().map(TB_User::getName).collect(Collectors.toSet());
            assert collect2.size() == 1;
            assert collect2.contains("123");
        }
    }

    @Test
    public void executeUpdate_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            //
            List<TB_User> tbUsers1 = jdbcTemplate.queryForList("select * from tb_user", TB_User.class);
            Set<String> collect1 = tbUsers1.stream().map(TB_User::getName).collect(Collectors.toSet());
            assert collect1.size() == 3;
            //
            assert jdbcTemplate.executeUpdate("update tb_user set name = ?", new Object[] { "123" }) == 3;
            //
            List<TB_User> tbUsers2 = jdbcTemplate.queryForList("select * from tb_user", TB_User.class);
            Set<String> collect2 = tbUsers2.stream().map(TB_User::getName).collect(Collectors.toSet());
            assert collect2.size() == 1;
            assert collect2.contains("123");
        }
    }

    @Test
    public void executeUpdate_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            //
            List<TB_User> tbUsers1 = jdbcTemplate.queryForList("select * from tb_user", TB_User.class);
            Set<String> collect1 = tbUsers1.stream().map(TB_User::getName).collect(Collectors.toSet());
            assert collect1.size() == 3;
            //
            TB_User tbUser = TestUtils.beanForData1();
            BeanSqlParameterSource beanSqlParameterSource = new BeanSqlParameterSource(tbUser);
            assert jdbcTemplate.executeUpdate("update tb_user set name = '123' where userUUID != :userUUID", beanSqlParameterSource) == 2;
            //
            List<TB_User> tbUsers2 = jdbcTemplate.queryForList("select * from tb_user", TB_User.class);
            Set<String> collect2 = tbUsers2.stream().map(TB_User::getName).collect(Collectors.toSet());
            assert collect2.size() == 2;
            assert collect2.contains("123");
            assert collect2.contains(tbUser.getName());
        }
    }

    @Test
    public void executeUpdate_5() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            //
            List<TB_User> tbUsers1 = jdbcTemplate.queryForList("select * from tb_user", TB_User.class);
            Set<String> collect1 = tbUsers1.stream().map(TB_User::getName).collect(Collectors.toSet());
            assert collect1.size() == 3;
            //
            TB_User tbUser = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", tbUser.getUserUUID());
            assert jdbcTemplate.executeUpdate("update tb_user set name = '123' where userUUID != :uuid", mapParams) == 2;
            //
            List<TB_User> tbUsers2 = jdbcTemplate.queryForList("select * from tb_user", TB_User.class);
            Set<String> collect2 = tbUsers2.stream().map(TB_User::getName).collect(Collectors.toSet());
            assert collect2.size() == 2;
            assert collect2.contains("123");
            assert collect2.contains(tbUser.getName());
        }
    }
}
