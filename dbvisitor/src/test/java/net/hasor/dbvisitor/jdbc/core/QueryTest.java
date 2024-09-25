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
import net.hasor.dbvisitor.dynamic.args.BeanSqlArgSource;
import net.hasor.dbvisitor.jdbc.extractor.RowMapperResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.MappingRowMapper;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * query 系列方法测试
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class QueryTest extends AbstractDbTest {

    @Test
    public void query_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<UserInfo2> users = jdbcTemplate.query("select * from user_info", rs -> {
                return new RowMapperResultSetExtractor<>(new MappingRowMapper<>(UserInfo2.class)).extractData(rs);
            });
            assert users.size() == 3;
            assert TestUtils.beanForData1().getUserUuid().equals(users.get(0).getUid());
            assert TestUtils.beanForData2().getUserUuid().equals(users.get(1).getUid());
            assert TestUtils.beanForData3().getUserUuid().equals(users.get(2).getUid());
        }
    }

    @Test
    public void query_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = jdbcTemplate.query("select * from user_info where user_uuid = ?", ps -> {
                ps.setString(1, user.getUserUuid());
            }, rs -> {
                return new RowMapperResultSetExtractor<>(new MappingRowMapper<>(UserInfo2.class)).extractData(rs);
            });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void query_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = jdbcTemplate.query("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() }, rs -> {
                return new RowMapperResultSetExtractor<>(new MappingRowMapper<>(UserInfo2.class)).extractData(rs);
            });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void query_5() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = jdbcTemplate.query("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() }, rs -> {
                return new RowMapperResultSetExtractor<>(new MappingRowMapper<>(UserInfo2.class)).extractData(rs);
            });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void query_6() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            List<UserInfo2> users = jdbcTemplate.query("select * from user_info where user_uuid = :uuid", mapParams, rs -> {
                return new RowMapperResultSetExtractor<>(new MappingRowMapper<>(UserInfo2.class)).extractData(rs);
            });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void query_7() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            BeanSqlArgSource beanSqlParameterSource = new BeanSqlArgSource(user);
            List<UserInfo2> users = jdbcTemplate.query("select * from user_info where user_uuid = :userUuid", beanSqlParameterSource, rs -> {
                return new RowMapperResultSetExtractor<>(new MappingRowMapper<>(UserInfo2.class)).extractData(rs);
            });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryVoid_1() throws Throwable {
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

    @Test
    public void queryVoid2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<UserInfo2> users = new ArrayList<>();
            jdbcTemplate.query("select * from user_info", (rs, rowNum) -> {
                users.add(new MappingRowMapper<>(UserInfo2.class).mapRow(rs, rowNum));
            });
            assert users.size() == 3;
            assert TestUtils.beanForData1().getUserUuid().equals(users.get(0).getUid());
            assert TestUtils.beanForData2().getUserUuid().equals(users.get(1).getUid());
            assert TestUtils.beanForData3().getUserUuid().equals(users.get(2).getUid());
        }
    }

    @Test
    public void queryVoid3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = new ArrayList<>();
            jdbcTemplate.query("select * from user_info where user_uuid = ?", ps -> {
                ps.setString(1, user.getUserUuid());
            }, (rs, rowNum) -> {
                users.add(new MappingRowMapper<>(UserInfo2.class).mapRow(rs, rowNum));
            });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryVoid4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = new ArrayList<>();
            jdbcTemplate.query("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() }, (rs, rowNum) -> {
                users.add(new MappingRowMapper<>(UserInfo2.class).mapRow(rs, rowNum));
            });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryVoid5() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = new ArrayList<>();
            jdbcTemplate.query("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() }, (rs, rowNum) -> {
                users.add(new MappingRowMapper<>(UserInfo2.class).mapRow(rs, rowNum));
            });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryVoid6() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            List<UserInfo2> users = new ArrayList<>();
            jdbcTemplate.query("select * from user_info where user_uuid = :uuid", mapParams, (rs, rowNum) -> {
                users.add(new MappingRowMapper<>(UserInfo2.class).mapRow(rs, rowNum));
            });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryVoid7() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            BeanSqlArgSource beanSqlParameterSource = new BeanSqlArgSource(user);
            List<UserInfo2> users = new ArrayList<>();
            jdbcTemplate.query("select * from user_info where user_uuid = :userUuid", beanSqlParameterSource, (rs, rowNum) -> {
                users.add(new MappingRowMapper<>(UserInfo2.class).mapRow(rs, rowNum));
            });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryList_1() throws Throwable {
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
    public void queryList_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info", new MappingRowMapper<>(UserInfo2.class));
            assert users.size() == 3;
            assert TestUtils.beanForData1().getUserUuid().equals(users.get(0).getUid());
            assert TestUtils.beanForData2().getUserUuid().equals(users.get(1).getUid());
            assert TestUtils.beanForData3().getUserUuid().equals(users.get(2).getUid());
        }
    }

    @Test
    public void queryList_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", ps -> {
                ps.setString(1, user.getUserUuid());
            }, new MappingRowMapper<>(UserInfo2.class));
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryList_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() }, new MappingRowMapper<>(UserInfo2.class));
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryList_5() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() }, new MappingRowMapper<>(UserInfo2.class));
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryList_6() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :uuid", mapParams, new MappingRowMapper<>(UserInfo2.class));
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryList_7() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            BeanSqlArgSource beanSqlParameterSource = new BeanSqlArgSource(user);
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :userUuid", beanSqlParameterSource, new MappingRowMapper<>(UserInfo2.class));
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }
}
