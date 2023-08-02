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
import net.hasor.dbvisitor.jdbc.paramer.BeanSqlParameterSource;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * queryFor 系列方法测试
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class QueryForTest extends AbstractDbTest {
    @Test
    public void queryForList_1() throws Throwable {
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
    public void queryForList_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() }, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryForList_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() }, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryForList_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            BeanSqlParameterSource beanSqlParameterSource = new BeanSqlParameterSource(user);
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :userUuid ", beanSqlParameterSource, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryForList_5() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :uuid ", mapParams, UserInfo2.class);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).getUid());
        }
    }

    @Test
    public void queryForObject_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            MappingRowMapper<UserInfo2> rowMapper = new MappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = '" + user1.getUserUuid() + "'", rowMapper);
            assert user2 != null;
            assert user2.getUid().equals(user1.getUserUuid());
        }
    }

    @Test
    public void queryForObject_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            MappingRowMapper<UserInfo2> rowMapper = new MappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = ?", new Object[] { user1.getUserUuid() }, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void queryForObject_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            MappingRowMapper<UserInfo2> rowMapper = new MappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = ?", new Object[] { user1.getUserUuid() }, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void queryForObject_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            BeanSqlParameterSource beanSqlParameterSource = new BeanSqlParameterSource(user1);
            MappingRowMapper<UserInfo2> rowMapper = new MappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = :userUuid", beanSqlParameterSource, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void queryForObject_5() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user1.getUserUuid());
            MappingRowMapper<UserInfo2> rowMapper = new MappingRowMapper<>(UserInfo2.class);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = :uuid", mapParams, rowMapper);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void queryForObject_6() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = '" + user1.getUserUuid() + "'", UserInfo2.class);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void queryForObject_7() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = ?", new Object[] { user1.getUserUuid() }, UserInfo2.class);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void queryForObject_8() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = ?", new Object[] { user1.getUserUuid() }, UserInfo2.class);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void queryForObject_9() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            BeanSqlParameterSource beanSqlParameterSource = new BeanSqlParameterSource(user1);
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = :userUuid", beanSqlParameterSource, UserInfo2.class);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void queryForObject_10() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user1 = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user1.getUserUuid());
            UserInfo2 user2 = jdbcTemplate.queryForObject("select * from user_info where user_uuid = :uuid", mapParams, UserInfo2.class);
            assert user2 != null;
            assert user1.getUserUuid().equals(user2.getUid());
        }
    }

    @Test
    public void queryForNumber_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            long userCountLong = jdbcTemplate.queryForLong("select count(*) from user_info");
            assert userCountLong == 3;
            long userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info");
            assert userCountInt == 3;
        }
    }

    @Test
    public void queryForNumber_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            long userCountLong = jdbcTemplate.queryForLong("select count(*) from user_info where user_uuid != ?", new Object[] { user.getUserUuid() });
            assert userCountLong == 2;
            long userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != ?", new Object[] { user.getUserUuid() });
            assert userCountInt == 2;
        }
    }

    @Test
    public void queryForNumber_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            BeanSqlParameterSource beanSqlParameterSource = new BeanSqlParameterSource(user);
            long userCountLong = jdbcTemplate.queryForLong("select count(*) from user_info where user_uuid != :userUuid", beanSqlParameterSource);
            assert userCountLong == 2;
            long userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != :userUuid", beanSqlParameterSource);
            assert userCountInt == 2;
        }
    }

    @Test
    public void queryForNumber_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            long userCountLong = jdbcTemplate.queryForLong("select count(*) from user_info where user_uuid != :uuid", mapParams);
            assert userCountLong == 2;
            long userCountInt = jdbcTemplate.queryForInt("select count(*) from user_info where user_uuid != :uuid", mapParams);
            assert userCountInt == 2;
        }
    }

    @Test
    public void queryForMap_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = '" + user.getUserUuid() + "'");
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void queryForMap_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() });
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void queryForMap_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            BeanSqlParameterSource beanSqlParameterSource = new BeanSqlParameterSource(user);
            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where user_uuid = :userUuid", beanSqlParameterSource);
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }

    @Test
    public void queryForMap_4() throws Throwable {
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
    public void queryForListMap_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info");
            assert users.size() == 3;
            assert TestUtils.beanForData1().getUserUuid().equals(users.get(0).get("user_UUID"));
            assert TestUtils.beanForData2().getUserUuid().equals(users.get(1).get("user_UUID"));
            assert TestUtils.beanForData3().getUserUuid().equals(users.get(2).get("user_UUID"));
        }
    }

    @Test
    public void queryForListMap_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ?", new Object[] { user.getUserUuid() });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void queryForListMap_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            BeanSqlParameterSource beanSqlParameterSource = new BeanSqlParameterSource(user);
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :userUuid ", beanSqlParameterSource);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void queryForListMap_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("uuid", user.getUserUuid());
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = :uuid ", mapParams);
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }

    @Test
    public void queryForListMap_5() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserInfo user = TestUtils.beanForData1();
            List<Map<String, Object>> users = jdbcTemplate.queryForList("select * from user_info where user_uuid = ? ", ps -> {
                ps.setString(1, user.getUserUuid());
            });
            assert users.size() == 1;
            assert user.getUserUuid().equals(users.get(0).get("user_UUID"));
        }
    }
}
