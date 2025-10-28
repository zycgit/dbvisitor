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

public class ServerCommandTest extends AbstractJdbcTest {

    @Test
    public void move_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(DatabaseCommands.class, createInvocationHandler("move", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("move mykey 123") == 2;
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
                    while (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
                }
            }

            assert argList.get(0).equals(10);
            assert argList.get(1).equals(10L);
        } catch (SQLException e) {
            assert false;
        }
    }
}