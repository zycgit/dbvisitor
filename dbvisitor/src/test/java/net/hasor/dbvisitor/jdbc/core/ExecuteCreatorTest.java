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
import net.hasor.dbvisitor.jdbc.mapper.MappingRowMapper;
import net.hasor.scene.UserNameResultSetExtractor;
import net.hasor.scene.UserNameRowCallback;
import net.hasor.scene.UserNameRowMapper;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/***
 * executeUpdate 系列方法测试
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class ExecuteCreatorTest extends AbstractDbTest {
    @Test
    public void callBack_0() throws SQLException {
        // PreparedStatementCreator and ResultSetExtractor
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.executeCreator(con -> {
                PreparedStatement ps = con.prepareStatement("select * from user_table where age > ? order by id");
                ps.setInt(1, 40);
                return ps;
            }, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void callBack_1() throws SQLException {
        // PreparedStatementCreator and RowCallbackHandler
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowCallback callback = new UserNameRowCallback();

            jdbcTemplate.executeCreator(con -> {
                PreparedStatement ps = con.prepareStatement("select * from user_table where age > ? order by id");
                ps.setInt(1, 40);
                return ps;
            }, callback);

            assert callback.size() == 2;
            assert callback.getName(0).equals("jon wes");
            assert callback.getName(1).equals("mary");
        }
    }

    @Test
    public void callBack_2() throws SQLException {
        // PreparedStatementCreator and RowMapper
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.executeCreator(con -> {
                PreparedStatement ps = con.prepareStatement("select * from user_table where age > ? order by id");
                ps.setInt(1, 40);
                return ps;
            }, new UserNameRowMapper());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void callBack_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<UserInfo2> users = jdbcTemplate.executeCreator(con -> {
                return con.prepareStatement("select * from user_info");
            }, new MappingRowMapper<>(UserInfo2.class));
            assert users.size() == 3;
            assert TestUtils.beanForData1().getUserUuid().equals(users.get(0).getUid());
            assert TestUtils.beanForData2().getUserUuid().equals(users.get(1).getUid());
            assert TestUtils.beanForData3().getUserUuid().equals(users.get(2).getUid());
        }
    }

    @Test
    public void callBack_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<UserInfo2> users = new ArrayList<>();
            jdbcTemplate.executeCreator(con -> {
                return con.prepareStatement("select * from user_info");
            }, (rs, rowNum) -> {
                users.add(new MappingRowMapper<>(UserInfo2.class).mapRow(rs, rowNum));
            });
            assert users.size() == 3;
            assert TestUtils.beanForData1().getUserUuid().equals(users.get(0).getUid());
            assert TestUtils.beanForData2().getUserUuid().equals(users.get(1).getUid());
            assert TestUtils.beanForData3().getUserUuid().equals(users.get(2).getUid());
        }
    }
}
