package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.Connection;
import java.util.Map;
import java.util.Objects;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.realdb.redis.dto1.UserInfo1;
import org.junit.Test;
import redis.clients.jedis.Jedis;

public class RedisJdbcTest {

    @Test
    public void using_jdbc_1() throws Exception {
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
    public void using_jdbc_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.executeUpdate("del myKey1 myKey2");// 预删除避免 test case 相互污染

            // read
            assert jdbc.queryForPairs("mget myKey1 myKey2", String.class, Integer.class).values().stream().noneMatch(Objects::nonNull);

            // write
            assert jdbc.executeUpdate("mset myKey1 123 myKey2 456") == 2;

            // read
            Map<String, Integer> res = jdbc.queryForPairs("mget myKey1 myKey2", String.class, Integer.class);
            assert res.size() == 2;
            assert res.get("myKey1") == 123;
            assert res.get("myKey2") == 456;

            // delete
            assert jdbc.executeUpdate("del myKey1 myKey2") == 2;
            assert jdbc.queryForPairs("mget myKey1 myKey2", String.class, Integer.class).values().stream().noneMatch(Objects::nonNull);
        }
    }

    @Test
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
}
