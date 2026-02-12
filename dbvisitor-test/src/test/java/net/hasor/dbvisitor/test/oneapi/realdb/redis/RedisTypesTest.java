package net.hasor.dbvisitor.test.oneapi.realdb.redis;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hasor.cobble.ref.Tuple;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.oneapi.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.oneapi.realdb.redis.dto1.UserInfo1;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;
import redis.clients.jedis.Jedis;

public class RedisTypesTest {

    @Test
    public void string_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myKey1");// 预删除避免 test case 相互污染

            // read
            assert jdbc.queryForInt("get myKey1") == null;

            // write
            assert jdbc.executeUpdate("set myKey1 123") == 1;

            // read
            assert jdbc.queryForInt("get myKey1") == 123;

            // delete
            assert jdbc.executeUpdate("del myKey1") == 1;
            assert jdbc.queryForInt("get myKey1") == null;
        }
    }

    @Test
    public void hash_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myKey1");// 预删除避免 test case 相互污染

            // read
            assert jdbc.executeUpdate("HSET myKey1 field1 value1") == 1;
            assert jdbc.queryForString("HGET ? ?", new Object[] { "myKey1", "field1" }).equals("value1");

            // delete
            assert jdbc.executeUpdate("del myKey1") == 1;
            assert jdbc.queryForInt("get myKey1") == null;
        }
    }

    @Test
    public void hash_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myKey1");// 预删除避免 test case 相互污染

            assert jdbc.executeUpdate("HSET myKey1 field1 value1 field2 value2") == 2;

            // read 1
            List<String> keys = jdbc.queryForList("HKEYS myKey1", String.class);
            assert keys.size() == 2;
            assert keys.contains("field1");
            assert keys.contains("field2");

            // read 2
            Map<String, String> keyValue = jdbc.queryForPairs("HGETALL myKey1", String.class, String.class);
            assert keyValue.size() == 2;
            assert keyValue.get("field1").equals("value1");
            assert keyValue.get("field2").equals("value2");

            // delete
            assert jdbc.executeUpdate("del myKey1") == 1;
            assert jdbc.queryForInt("get myKey1") == null;
        }
    }

    @Test
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
            assert keys.size() == 2;
            assert keys.contains("field1");
            assert keys.contains("field2");

            // read 2
            Map<String, String> keyValue = jdbc.queryForPairs("HGETALL myKey1", String.class, String.class);
            assert keyValue.size() == 2;
            assert keyValue.get("field1").equals("value1");
            assert keyValue.get("field2").equals("value2");

            // delete
            assert jdbc.executeUpdate("del myKey1") == 1;
            assert jdbc.queryForInt("get myKey1") == null;
        }
    }

    @Test
    public void list_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myListKey");// 预删除避免 test case 相互污染

            jdbc.executeUpdate("LPUSH myListKey value1 value2 value3");

            // read 1
            assert jdbc.queryForString("LPOP myListKey").equals("value3");
            assert jdbc.queryForString("RPOP myListKey").equals("value1");

            // delete
            assert jdbc.executeUpdate("del myListKey") == 1;
            assert jdbc.queryForInt("get myListKey") == null;
        }
    }

    @Test
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
            assert keys.size() == 2;
            assert keys.contains("value1");
            assert keys.contains("value2");

            // delete
            assert jdbc.executeUpdate("del myListKey") == 1;
            assert jdbc.queryForInt("get myListKey") == null;
        }
    }

    @Test
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
            assert jdbc.executeUpdate("set #{'user_' + arg0.uid} #{arg0}", user) == 1;

            // load
            UserInfo1 info = jdbc.queryForObject("get #{'user_' + arg0}", "j1111", UserInfo1.class);
            assert user != info;
            assert info.getUid().equals("j1111");
            assert info.getName().equals("username");
            assert info.getLoginName().equals("login_123");
            assert info.getLoginPassword().equals("password");

            // delete
            assert c.unwrap(Jedis.class).get("user_j1111") != null;
            assert jdbc.executeUpdate("del user_j1111") == 1;
            assert c.unwrap(Jedis.class).get("user_j1111") == null;
        }
    }

    @Test
    public void set_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del mySetKey");// 预删除避免 test case 相互污染

            jdbc.executeUpdate("SADD mySetKey value1 value2 value3");
            List<String> members = jdbc.queryForList("SMEMBERS mySetKey", String.class);
            assert members.size() == 3;
            assert members.contains("value1");
            assert members.contains("value2");
            assert members.contains("value3");

            // delete
            assert jdbc.executeUpdate("del mySetKey") == 1;
            assert jdbc.queryForInt("get mySetKey") == null;
        }
    }

    @Test
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
            assert members.size() == 3;
            assert members.contains("tag1");
            assert members.contains("tag2");
            assert members.contains("tag3");

            // delete
            assert jdbc.executeUpdate("del article_1") == 1;
            assert jdbc.queryForInt("get article_1") == null;
        }
    }

    @Test
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
            assert members.size() == 2;
            assert members.contains("field1");
            assert members.contains("field2");

            // delete
            assert jdbc.executeUpdate("del myKey1") == 1;
            assert jdbc.queryForInt("get myKey1") == null;
        }
    }

    @Test
    public void zset_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del mySetKey");// 预删除避免 test case 相互污染

            jdbc.execute("ZADD mySetKey 3 value3 2 value2 1 value1");
            List<String> members = jdbc.queryForList("ZRANGEBYSCORE mySetKey -inf +inf ", String.class);
            assert members.size() == 3;
            assert members.contains("value1");
            assert members.contains("value2");
            assert members.contains("value3");

            // delete
            assert jdbc.executeUpdate("del mySetKey") == 1;
            assert jdbc.queryForInt("get mySetKey") == null;
        }
    }

    @Test
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
            assert members.size() == 3;
            assert members.get(0).equals("field3");
            assert members.get(1).equals("field2");
            assert members.get(2).equals("field1");

            // delete
            assert jdbc.executeUpdate("del myKey1") == 1;
            assert jdbc.queryForInt("get myKey1") == null;
        }
    }

    @Test
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
            assert members.size() == 3;
            assert members.get(0).equals("field3");
            assert members.get(1).equals("field2");
            assert members.get(2).equals("field1");

            // delete
            assert jdbc.executeUpdate("del myKey1") == 1;
            assert jdbc.queryForInt("get myKey1") == null;
        }
    }
}
