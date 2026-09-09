package net.hasor.dbvisitor.adapter.redis.connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.JedisKeys;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import net.hasor.dbvisitor.adapter.redis.RedisCustomJedis;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Test;
import redis.clients.jedis.commands.StringCommands;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RedisMultiStatementTest extends AbstractJdbcTest {
    @After
    public void cleanup() {
        RedisCommandInterceptor.resetInterceptor();
    }

    @Test
    public void allCommandsExecuteWithIndependentParameterOffsets() throws Exception {
        for (boolean literalFirst : new boolean[] { false, true }) {
            List<String> calls = new ArrayList<>();
            RedisCommandInterceptor.resetInterceptor();
            RedisCommandInterceptor.addInterceptor(StringCommands.class, (proxy, method, args) -> {
                if ("get".equals(method.getName())) {
                    calls.add("get:" + args[0]);
                    return "value";
                }
                if ("set".equals(method.getName())) {
                    calls.add("set:" + args[0] + ":" + args[1]);
                    return "OK";
                }
                return null;
            });
            Properties props = new Properties();
            props.setProperty(JedisKeys.CUSTOM_JEDIS, RedisCustomJedis.class.getName());
            props.setProperty(JedisKeys.INTERCEPTOR, RedisCommandInterceptor.class.getName());
            props.setProperty(JedisKeys.SEPARATOR_CHAR, ";");
            try (Connection conn = new JdbcDriver().connect("jdbc:dbvisitor:jedis://test", props); PreparedStatement ps = conn.prepareStatement("get " + (literalFirst ? "first" : "?") + "; set ? ?; get last")) {
                for (int repeat = 0; repeat < 2; repeat++) {
                    calls.clear();
                    int index = 1;
                    if (!literalFirst) {
                        ps.setString(index++, "first");
                    }
                    ps.setString(index++, "key" + repeat);
                    ps.setString(index, "value" + repeat);
                    assertTrue(ps.execute());
                    assertEquals(Arrays.asList("get:first", "set:key" + repeat + ":value" + repeat, "get:last"), calls);
                }
            }
        }
    }
}