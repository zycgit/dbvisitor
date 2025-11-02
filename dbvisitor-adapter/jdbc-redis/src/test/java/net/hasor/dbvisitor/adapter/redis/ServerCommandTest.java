package net.hasor.dbvisitor.adapter.redis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.hasor.dbvisitor.adapter.redis.support.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.support.RedisCommandInterceptor;
import org.junit.Test;
import redis.clients.jedis.commands.DatabaseCommands;
import redis.clients.jedis.commands.ServerCommands;
import redis.clients.jedis.util.KeyValue;

public class ServerCommandTest extends AbstractJdbcTest {

    @Test
    public void move_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(DatabaseCommands.class, createInvocationHandler("move", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("move mykey 123")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", 123));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void wait_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ServerCommands.class, createInvocationHandler("waitReplicas", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("wait 10 10")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("REPLICAS") == 123L;
                }
            }

            assert argList.get(0).equals(10);
            assert argList.get(1).equals(10L);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void waitaof_1() {
        List<Object> argList = new ArrayList<>();
        KeyValue<Long, Long> returnValue = new KeyValue<>(123L, 321L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ServerCommands.class, createInvocationHandler("waitAOF", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("waitaof 1 10 10")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("LOCAL") == 123L;
                    assert rs.getLong(2) == 321L;
                    assert rs.getLong("REPLICAS") == 321L;
                }
            }

            assert argList.get(0).equals(1L);
            assert argList.get(1).equals(10L);
            assert argList.get(2).equals(10L);
        } catch (SQLException e) {
            assert false;
        }
    }
}