package net.hasor.dbvisitor.adapter.redis.connect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import net.hasor.dbvisitor.adapter.redis.JedisKeys;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import net.hasor.dbvisitor.adapter.redis.RedisCustomJedis;
import net.hasor.dbvisitor.driver.JdbcDriver;

public class TestParamTest {

    public Connection redisConnection() throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(JedisKeys.INTERCEPTOR, RedisCommandInterceptor.class.getName());
        prop.setProperty(JedisKeys.CUSTOM_JEDIS, RedisCustomJedis.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:jedis://xxxxxx", prop);
    }

    // resis
}
