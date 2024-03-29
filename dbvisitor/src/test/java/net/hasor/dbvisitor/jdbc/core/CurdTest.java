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
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.user_info;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

import static net.hasor.test.utils.TestUtils.*;

/***
 * CURD 基准测试
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class CurdTest extends AbstractDbTest {
    @Test
    public void insertTest_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData4());

            List<user_info> tbUsers = jdbcTemplate.queryForList("select * from user_info where user_uuid =?", //
                    new Object[] { beanForData4().getUserUuid() }, user_info.class);
            List<String> collect = tbUsers.stream().map(user_info::getUser_name).collect(Collectors.toList());
            assert collect.contains("赵子龙");
        }
    }

    @Test
    public void insertTest_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate(INSERT_MAP, mapForData4());

            List<user_info> tbUsers = jdbcTemplate.queryForList("select * from user_info where user_uuid =?", //
                    new Object[] { beanForData4().getUserUuid() }, user_info.class);
            List<String> collect = tbUsers.stream().map(user_info::getUser_name).collect(Collectors.toList());
            assert collect.contains("赵子龙");
        }
    }

    @Test
    public void insertTest_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            assert jdbcTemplate.queryForInt("select count(1) from user_info") == 3;

            int count = 10;
            Object[][] batchValues = new Object[count][];
            for (int i = 0; i < count; i++) {
                batchValues[i] = new Object[7];
                batchValues[i][0] = UUID.randomUUID().toString();
                batchValues[i][1] = String.format("默认用户_%s", i);
                batchValues[i][2] = String.format("acc_%s", i);
                batchValues[i][3] = String.format("pwd_%s", i);
                batchValues[i][4] = String.format("autoUser_%s@hasor.net", i);
                batchValues[i][5] = i;
                batchValues[i][6] = new Date();
            }
            jdbcTemplate.executeBatch(INSERT_ARRAY, batchValues);//批量执行执行插入语句
            printMapList(jdbcTemplate.queryForList("select * from user_info"));
            assert jdbcTemplate.queryForInt("select count(1) from user_info") == 13;
        }
    }

    @Test
    public void insertTest_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            assert jdbcTemplate.queryForInt("select count(1) from user_info") == 3;

            int count = 10;
            Map<String, Object>[] batchValues = new Map[count];
            for (int i = 0; i < count; i++) {
                batchValues[i] = new HashMap<>();
                batchValues[i].put("userUUID", UUID.randomUUID().toString());
                batchValues[i].put("name", String.format("默认用户_%s", i));
                batchValues[i].put("loginName", String.format("acc_%s", i));
                batchValues[i].put("loginPassword", String.format("pwd_%s", i));
                batchValues[i].put("email", String.format("autoUser_%s@hasor.net", i));
                batchValues[i].put("index", i);
                batchValues[i].put("registerTime", new Date());
            }
            jdbcTemplate.executeBatch(INSERT_MAP, batchValues);//批量执行执行插入语句
            printMapList(jdbcTemplate.queryForList("select * from user_info"));
            assert jdbcTemplate.queryForInt("select count(1) from user_info") == 13;
        }
    }

    @Test
    public void updateTest_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            assert jdbcTemplate.executeUpdate("update user_info set user_name = '123'") == 3;

            printMapList(jdbcTemplate.queryForList("select * from user_info"));
            List<user_info> tbUsers = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect = tbUsers.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect.contains("123");
            assert collect.size() == 1;
        }
    }

    @Test
    public void deleteTest_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            assert jdbcTemplate.executeUpdate("delete user_info where login_name = 'muhammad'") == 1;

            printMapList(jdbcTemplate.queryForList("select * from user_info"));
            List<user_info> tbUsers = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect = tbUsers.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert !collect.contains("muhammad");
            assert collect.size() == 2;
        }
    }
}