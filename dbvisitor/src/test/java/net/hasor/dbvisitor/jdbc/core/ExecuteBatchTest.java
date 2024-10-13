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
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.args.BeanSqlArgSource;
import net.hasor.dbvisitor.jdbc.BatchPreparedStatementSetter;
import net.hasor.dbvisitor.jdbc.PreparedStatementSetter;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.user_info;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/***
 * executeBatch 系列方法测试
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class ExecuteBatchTest extends AbstractDbTest {
    @Test
    public void noargs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            String[] updateSql = new String[] {//
                    "update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = '" + TestUtils.beanForData1().getUserUuid() + "'",//
                    "update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = '" + TestUtils.beanForData2().getUserUuid() + "'",//
                    "update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = '" + TestUtils.beanForData3().getUserUuid() + "'",//
            };
            int[] ins = jdbcTemplate.executeBatch(updateSql);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void usingPosArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            Object[][] ids = new Object[][] {//
                    new Object[] { TestUtils.beanForData1().getUserUuid() },//
                    new Object[] { TestUtils.beanForData2().getUserUuid() },//
                    new Object[] { TestUtils.beanForData3().getUserUuid() } //
            };
            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = ?", ids);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void usingPosArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            Object[] ids = new Object[] {//
                    TestUtils.beanForData1().getUserUuid(),//
                    TestUtils.beanForData2().getUserUuid(),//
                    TestUtils.beanForData3().getUserUuid()//
            };
            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = ?", ids);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void usingPosArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            String[][] ids = new String[][] {//
                    new String[] { TestUtils.beanForData1().getUserUuid() },//
                    new String[] { TestUtils.beanForData2().getUserUuid() },//
                    new String[] { TestUtils.beanForData3().getUserUuid() } //
            };
            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = ?", ids);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void usingNamedArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            SqlArgSource[] ids = new SqlArgSource[] {//
                    new BeanSqlArgSource(TestUtils.beanForData1()),//
                    new BeanSqlArgSource(TestUtils.beanForData2()),//
                    new BeanSqlArgSource(TestUtils.beanForData3()) //
            };
            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = :userUuid", ids);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void usingNamedArgs_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            SqlArgSource[] ids = new SqlArgSource[] {//
                    new BeanSqlArgSource(TestUtils.beanForData1()),//
                    new BeanSqlArgSource(TestUtils.beanForData2()),//
                    new BeanSqlArgSource(TestUtils.beanForData3()) //
            };
            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = &userUuid", ids);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void usingNamedArgs_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            SqlArgSource[] ids = new SqlArgSource[] {//
                    new BeanSqlArgSource(TestUtils.beanForData1()),//
                    new BeanSqlArgSource(TestUtils.beanForData2()),//
                    new BeanSqlArgSource(TestUtils.beanForData3()) //
            };
            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = #{userUuid}", ids);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void usingInjectArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            SqlArgSource[] ids = new SqlArgSource[] {//
                    new BeanSqlArgSource(TestUtils.beanForData1()),//
                    new BeanSqlArgSource(TestUtils.beanForData2()),//
                    new BeanSqlArgSource(TestUtils.beanForData3()) //
            };

            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = :userUuid and 1 = ${1}", ids);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void usingRuleArgs_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            SqlArgSource[] ids = new SqlArgSource[] {//
                    new BeanSqlArgSource(TestUtils.beanForData1()),//
                    new BeanSqlArgSource(TestUtils.beanForData2()),//
                    new BeanSqlArgSource(TestUtils.beanForData3()) //
            };
            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = @{arg,true,userUuid}", ids);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void argType_as_map_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            Map[] ids = new Map[] {//
                    CollectionUtils.asMap("uuid", TestUtils.beanForData1().getUserUuid()),//
                    CollectionUtils.asMap("uuid", TestUtils.beanForData2().getUserUuid()),//
                    CollectionUtils.asMap("uuid", TestUtils.beanForData3().getUserUuid())//
            };
            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = :uuid", ids);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void argtype_as_pos_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            Object[][] ids = new Object[][] {//
                    new Object[] { TestUtils.beanForData1().getUserUuid() },//
                    new Object[] { TestUtils.beanForData2().getUserUuid() },//
                    new Object[] { TestUtils.beanForData3().getUserUuid() } //
            };
            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = ?", ids);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void argtype_as_source_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            SqlArgSource[] ids = new SqlArgSource[] {//
                    new BeanSqlArgSource(TestUtils.beanForData1()),//
                    new BeanSqlArgSource(TestUtils.beanForData2()),//
                    new BeanSqlArgSource(TestUtils.beanForData3()) //
            };
            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = #{userUuid}", ids);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void argtype_as_setter_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            PreparedStatementSetter[] ids = new PreparedStatementSetter[] {//
                    ps -> ps.setString(1, TestUtils.beanForData1().getUserUuid()),//
                    ps -> ps.setString(1, TestUtils.beanForData2().getUserUuid()),//
                    ps -> ps.setString(1, TestUtils.beanForData3().getUserUuid()),//
            };
            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = ?", ids);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }

    @Test
    public void argtype_as_batch_setter_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            SqlArgSource[] ids = new SqlArgSource[] {//
                    new BeanSqlArgSource(TestUtils.beanForData1()),//
                    new BeanSqlArgSource(TestUtils.beanForData2()),//
                    new BeanSqlArgSource(TestUtils.beanForData3()) //
            };
            BatchPreparedStatementSetter bps = new BatchPreparedStatementSetter() {
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, ids[i].getValue("userUuid").toString());
                }

                public int getBatchSize() {
                    return 3;
                }
            };
            int[] ins = jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = ?", bps);
            assert ins[0] == 1;
            assert ins[1] == 1;
            assert ins[2] == 1;

            List<user_info> tbUsers2 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect2 = tbUsers2.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect2.size() == 3;
            assert !collect2.contains(TestUtils.beanForData1().getName());
            assert !collect2.contains(TestUtils.beanForData2().getName());
            assert !collect2.contains(TestUtils.beanForData3().getName());
            assert collect2.contains(TestUtils.beanForData1().getName() + "~");
            assert collect2.contains(TestUtils.beanForData2().getName() + "~");
            assert collect2.contains(TestUtils.beanForData3().getName() + "~");
        }
    }
}
