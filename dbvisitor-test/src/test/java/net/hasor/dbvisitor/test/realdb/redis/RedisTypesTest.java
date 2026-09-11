/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.ref.Tuple;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.realdb.redis.dto1.UserInfo1;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import redis.clients.jedis.Jedis;

public class RedisTypesTest {
    @org.junit.BeforeClass
    public static void assumeDataSource() {
        OneApiDataSourceManager.assumeCurrentDataSource("redis");
    }


    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_TYPE_STRING)
    public void string_1() throws Exception {
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

            // delete
            assertTrue(jdbc.executeUpdate("del myKey1") == 1);
            assertTrue(jdbc.queryForInt("get myKey1") == null);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_TYPE_HASH)
    public void hash_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myKey1");// 预删除避免 test case 相互污染

            // read
            assertTrue(jdbc.executeUpdate("HSET myKey1 field1 value1") == 1);
            assertTrue(jdbc.queryForString("HGET ? ?", new Object[] { "myKey1", "field1" }).equals("value1"));

            // delete
            assertTrue(jdbc.executeUpdate("del myKey1") == 1);
            assertTrue(jdbc.queryForInt("get myKey1") == null);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_TYPE_HASH)
    public void hash_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myKey1");// 预删除避免 test case 相互污染

            assertTrue(jdbc.executeUpdate("HSET myKey1 field1 value1 field2 value2") == 2);

            // read 1
            List<String> keys = jdbc.queryForList("HKEYS myKey1", String.class);
            assertTrue(keys.size() == 2);
            assertTrue(keys.contains("field1"));
            assertTrue(keys.contains("field2"));

            // read 2
            Map<String, String> keyValue = jdbc.queryForPairs("HGETALL myKey1", String.class, String.class);
            assertTrue(keyValue.size() == 2);
            assertTrue(keyValue.get("field1").equals("value1"));
            assertTrue(keyValue.get("field2").equals("value2"));

            // delete
            assertTrue(jdbc.executeUpdate("del myKey1") == 1);
            assertTrue(jdbc.queryForInt("get myKey1") == null);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_TYPE_HASH)
    public void hash_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myKey1");// 预删除避免 test case 相互污染

            Map<String, String> hashData = new HashMap<>();
            hashData.put("field1", "value1");
            hashData.put("field2", "value2");
            jdbc.executeUpdate("HSET myKey1 @{pairs, :arg0, :k :v}", SqlArg.valueOf(hashData));

            // read 1
            List<String> keys = jdbc.queryForList("HKEYS myKey1", String.class);
            assertTrue(keys.size() == 2);
            assertTrue(keys.contains("field1"));
            assertTrue(keys.contains("field2"));

            // read 2
            Map<String, String> keyValue = jdbc.queryForPairs("HGETALL myKey1", String.class, String.class);
            assertTrue(keyValue.size() == 2);
            assertTrue(keyValue.get("field1").equals("value1"));
            assertTrue(keyValue.get("field2").equals("value2"));

            // delete
            assertTrue(jdbc.executeUpdate("del myKey1") == 1);
            assertTrue(jdbc.queryForInt("get myKey1") == null);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_TYPE_LIST)
    public void list_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myListKey");// 预删除避免 test case 相互污染

            jdbc.executeUpdate("LPUSH myListKey value1 value2 value3");

            // read 1
            assertTrue(jdbc.queryForString("LPOP myListKey").equals("value3"));
            assertTrue(jdbc.queryForString("RPOP myListKey").equals("value1"));

            // delete
            assertTrue(jdbc.executeUpdate("del myListKey") == 1);
            assertTrue(jdbc.queryForInt("get myListKey") == null);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_TYPE_LIST)
    public void list_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myListKey");// 预删除避免 test case 相互污染

            List<String> listData = new ArrayList<>();
            listData.add("value1");
            listData.add("value2");
            jdbc.executeUpdate("LPUSH myListKey @{pairs, :arg0, :v}", SqlArg.valueOf(listData));

            // read 1
            int size = jdbc.queryForInt("LLEN myListKey");
            List<String> keys = jdbc.queryForList("LRANGE myListKey 0 " + (size - 1), String.class);
            assertTrue(keys.size() == 2);
            assertTrue(keys.contains("value1"));
            assertTrue(keys.contains("value2"));

            // delete
            assertTrue(jdbc.executeUpdate("del myListKey") == 1);
            assertTrue(jdbc.queryForInt("get myListKey") == null);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_JDBC_DSL_BEAN)
    public void bean_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del user_j1111");// 预删除避免 test case 相互污染

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

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_TYPE_SET)
    public void set_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del mySetKey");// 预删除避免 test case 相互污染

            jdbc.executeUpdate("SADD mySetKey value1 value2 value3");
            List<String> members = jdbc.queryForList("SMEMBERS mySetKey", String.class);
            assertTrue(members.size() == 3);
            assertTrue(members.contains("value1"));
            assertTrue(members.contains("value2"));
            assertTrue(members.contains("value3"));

            // delete
            assertTrue(jdbc.executeUpdate("del mySetKey") == 1);
            assertTrue(jdbc.queryForInt("get mySetKey") == null);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_TYPE_SET)
    public void set_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del article_1");// 预删除避免 test case 相互污染

            jdbc.executeUpdate("SADD article_1 tag1 tag2");
            jdbc.executeUpdate("SADD article_1 tag1 tag3");

            // read
            List<String> members = jdbc.queryForList("SMEMBERS article_1", String.class);
            assertTrue(members.size() == 3);
            assertTrue(members.contains("tag1"));
            assertTrue(members.contains("tag2"));
            assertTrue(members.contains("tag3"));

            // delete
            assertTrue(jdbc.executeUpdate("del article_1") == 1);
            assertTrue(jdbc.queryForInt("get article_1") == null);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_TYPE_SET)
    public void set_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myKey1");// 预删除避免 test case 相互污染

            Map<String, String> hashData = new HashMap<>();
            hashData.put("field1", "value1");
            hashData.put("field2", "value2");
            jdbc.executeUpdate("SADD myKey1 @{pairs, :arg0, :k}", SqlArg.valueOf(hashData));

            // read
            List<String> members = jdbc.queryForList("SMEMBERS myKey1", String.class);
            assertTrue(members.size() == 2);
            assertTrue(members.contains("field1"));
            assertTrue(members.contains("field2"));

            // delete
            assertTrue(jdbc.executeUpdate("del myKey1") == 1);
            assertTrue(jdbc.queryForInt("get myKey1") == null);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_TYPE_ZSET)
    public void zset_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del mySetKey");// 预删除避免 test case 相互污染

            jdbc.execute("ZADD mySetKey 3 value3 2 value2 1 value1");
            List<String> members = jdbc.queryForList("ZRANGEBYSCORE mySetKey -inf +inf ", String.class);
            assertTrue(members.size() == 3);
            assertTrue(members.contains("value1"));
            assertTrue(members.contains("value2"));
            assertTrue(members.contains("value3"));

            // delete
            assertTrue(jdbc.executeUpdate("del mySetKey") == 1);
            assertTrue(jdbc.queryForInt("get mySetKey") == null);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_TYPE_ZSET)
    public void zset_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myKey1");// 预删除避免 test case 相互污染

            Map<String, Double> hashData = new HashMap<>();
            hashData.put("field1", 3.0);
            hashData.put("field2", 2.0);
            hashData.put("field3", 1.0);
            jdbc.queryForString("ZADD myKey1 @{pairs, :arg0, :v :k}", SqlArg.valueOf(hashData));

            // read
            List<String> members = jdbc.queryForList("ZRANGEBYSCORE myKey1 -inf +inf ", String.class);
            assertTrue(members.size() == 3);
            assertTrue(members.get(0).equals("field3"));
            assertTrue(members.get(1).equals("field2"));
            assertTrue(members.get(2).equals("field1"));

            // delete
            assertTrue(jdbc.executeUpdate("del myKey1") == 1);
            assertTrue(jdbc.queryForInt("get myKey1") == null);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_TYPE_ZSET)
    public void zset_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myKey1");// 预删除避免 test case 相互污染

            List<Tuple> hashData = new ArrayList<>();
            hashData.add(Tuple.of("field1", 3.0));
            hashData.add(Tuple.of("field2", 2.0));
            hashData.add(Tuple.of("field3", 1.0));
            jdbc.queryForString("ZADD myKey1 @{pairs, :arg0, :v.arg1 :v.arg0 }", SqlArg.valueOf(hashData));

            // read
            List<String> members = jdbc.queryForList("ZRANGEBYSCORE myKey1 -inf +inf ", String.class);
            assertTrue(members.size() == 3);
            assertTrue(members.get(0).equals("field3"));
            assertTrue(members.get(1).equals("field2"));
            assertTrue(members.get(2).equals("field1"));

            // delete
            assertTrue(jdbc.executeUpdate("del myKey1") == 1);
            assertTrue(jdbc.queryForInt("get myKey1") == null);
        }
    }
}
