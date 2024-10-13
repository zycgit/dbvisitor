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
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.StatementCallback;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.user_info;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.hasor.test.utils.TestUtils.beanForData1;

/***
 * execute 系列方法测试
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class ExecuteTest extends AbstractDbTest {
    @Test
    public void execute_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.execute((ConnectionCallback<Boolean>) con -> {
                return con.createStatement().execute("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = '" + beanForData1().getUserUuid() + "'");
            });

            List<user_info> tbUsers = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", new Object[] { beanForData1().getUserUuid() }, user_info.class);
            Set<String> collect = tbUsers.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect.size() == 1;
            assert collect.contains(beanForData1().getName() + "~");
        }
    }

    @Test
    public void execute_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.execute((StatementCallback<Boolean>) s -> {
                return s.execute("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = '" + beanForData1().getUserUuid() + "'");
            });

            List<user_info> tbUsers = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", new Object[] { beanForData1().getUserUuid() }, user_info.class);
            Set<String> collect = tbUsers.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect.size() == 1;
            assert collect.contains(beanForData1().getName() + "~");
        }
    }

    @Test
    public void execute_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.execute("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = '" + beanForData1().getUserUuid() + "'");

            List<user_info> tbUsers = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", new Object[] { beanForData1().getUserUuid() }, user_info.class);
            Set<String> collect = tbUsers.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect.size() == 1;
            assert collect.contains(beanForData1().getName() + "~");
        }
    }
}
