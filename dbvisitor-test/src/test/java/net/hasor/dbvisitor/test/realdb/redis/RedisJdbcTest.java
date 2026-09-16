/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.Connection;
import java.util.Map;
import java.util.Objects;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.realdb.redis.dto1.UserInfo1;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import static org.junit.Assert.*;

public class RedisJdbcTest {
    @org.junit.BeforeClass
    public static void assumeDataSource() {
        OneApiDataSourceManager.assumeCurrentDataSource("redis");
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_JDBC_DSL_CRUD)
    public void using_jdbc_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myKey1");// 预删除避免 test case 相互污染

            // read
            assertNull(jdbc.queryForInt("get myKey1"));

            // write
            assertEquals(1, jdbc.executeUpdate("set myKey1 123"));

            // read
            assertEquals(123, (int) jdbc.queryForInt("get myKey1"));

            // update the existing value
            assertEquals(1, jdbc.executeUpdate("set myKey1 456"));
            assertEquals(456, (int) jdbc.queryForInt("get myKey1"));

            // delete
            assertEquals(1, jdbc.executeUpdate("del myKey1"));
            assertNull(jdbc.queryForInt("get myKey1"));
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_JDBC_DSL_PAIRS)
    public void using_jdbc_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myKey1 myKey2");// 预删除避免 test case 相互污染

            // read
            assertTrue(jdbc.queryForPairs("mget myKey1 myKey2", String.class, Integer.class).values().stream().noneMatch(Objects::nonNull));

            // write
            assertEquals(2, jdbc.executeUpdate("mset myKey1 123 myKey2 456"));

            // read
            Map<String, Integer> res = jdbc.queryForPairs("mget myKey1 myKey2", String.class, Integer.class);
            assertEquals(2, res.size());
            assertEquals(123, (int) res.get("myKey1"));
            assertEquals(456, (int) res.get("myKey2"));

            // delete
            assertEquals(2, jdbc.executeUpdate("del myKey1 myKey2"));
            assertTrue(jdbc.queryForPairs("mget myKey1 myKey2", String.class, Integer.class).values().stream().noneMatch(Objects::nonNull));
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_JDBC_DSL_BEAN)
    public void using_jdbc_bean_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del user_j1111");

            UserInfo1 user = new UserInfo1();
            user.setUid("j1111");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            assertEquals(1, jdbc.executeUpdate("set #{'user_' + arg0.uid} #{arg0}", user));

            // load
            UserInfo1 info = jdbc.queryForObject("get #{'user_' + arg0}", "j1111", UserInfo1.class);
            assertNotSame(user, info);
            assertEquals("j1111", info.getUid());
            assertEquals("username", info.getName());
            assertEquals("login_123", info.getLoginName());
            assertEquals("password", info.getLoginPassword());

            // delete
            assertNotNull(c.unwrap(Jedis.class).get("user_j1111"));
            assertEquals(1, jdbc.executeUpdate("del user_j1111"));
            assertNull(c.unwrap(Jedis.class).get("user_j1111"));
        }
    }
}
