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
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.realdb.redis.dto1.UserInfo1;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import redis.clients.jedis.Jedis;

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
            assertTrue(jdbc.queryForInt("get myKey1") == null);

            // write
            assertTrue(jdbc.executeUpdate("set myKey1 123") == 1);

            // read
            assertTrue(jdbc.queryForInt("get myKey1") == 123);

            // update the existing value
            assertTrue(jdbc.executeUpdate("set myKey1 456") == 1);
            assertTrue(jdbc.queryForInt("get myKey1") == 456);

            // delete
            assertTrue(jdbc.executeUpdate("del myKey1") == 1);
            assertTrue(jdbc.queryForInt("get myKey1") == null);
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
            assertTrue(jdbc.executeUpdate("mset myKey1 123 myKey2 456") == 2);

            // read
            Map<String, Integer> res = jdbc.queryForPairs("mget myKey1 myKey2", String.class, Integer.class);
            assertTrue(res.size() == 2);
            assertTrue(res.get("myKey1") == 123);
            assertTrue(res.get("myKey2") == 456);

            // delete
            assertTrue(jdbc.executeUpdate("del myKey1 myKey2") == 2);
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

            UserInfo1 user = new UserInfo1();
            user.setUid("j1111");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            assertTrue(jdbc.executeUpdate("set #{'user_' + arg0.uid} #{arg0}", user) == 1);

            // load
            UserInfo1 info = jdbc.queryForObject("get #{'user_' + arg0}", "j1111", UserInfo1.class);
            assertTrue(user != info);
            assertTrue(info.getUid().equals("j1111"));
            assertTrue(info.getName().equals("username"));
            assertTrue(info.getLoginName().equals("login_123"));
            assertTrue(info.getLoginPassword().equals("password"));

            // delete
            assertTrue(c.unwrap(Jedis.class).get("user_j1111") != null);
            assertTrue(jdbc.executeUpdate("del user_j1111") == 1);
            assertTrue(c.unwrap(Jedis.class).get("user_j1111") == null);
        }
    }
}
