package net.hasor.dbvisitor.adapter.redis.support;

import java.lang.reflect.InvocationHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import net.hasor.dbvisitor.adapter.redis.JedisKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;

public class AbstractJdbcTest {

    public Connection redisConnection() throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(JedisKeys.INTERCEPTOR, RedisCommandInterceptor.class.getName());
        prop.setProperty(JedisKeys.CUSTOM_JEDIS, RedisCustomJedis.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:jedis://xxxxxx", prop);
    }

    public InvocationHandler createInvocationHandler(String methodName, TestInvocationHandler handler) {
        return (proxy, method, args) -> {
            if (method.getName().equals(methodName)) {
                return handler.invoke(args);
            }
            return null;
        };
    }
}
