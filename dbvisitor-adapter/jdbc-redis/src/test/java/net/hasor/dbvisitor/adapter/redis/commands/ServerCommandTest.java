package net.hasor.dbvisitor.adapter.redis.commands;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
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
        RedisCommandInterceptor.addInterceptor(DatabaseCommands.class, createInvocationHandler("move", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("move mykey 123") == 123L;
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
        RedisCommandInterceptor.addInterceptor(ServerCommands.class, createInvocationHandler("waitReplicas", (name, args) -> {
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
        RedisCommandInterceptor.addInterceptor(ServerCommands.class, createInvocationHandler("waitAOF", (name, args) -> {
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

    @Test
    public void ping_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ServerCommands.class, createInvocationHandler("ping", (name, args) -> {
            if (args != null) {
                argList.addAll(Arrays.asList(args));
            }
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("ping")) {
                    rs.next();
                    assert rs.getString(1).equals("abc");
                    assert rs.getString("RESULT").equals("abc");
                }
            }

            assert argList.isEmpty();
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void ping_2() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ServerCommands.class, createInvocationHandler("ping", (name, args) -> {
            if (args != null) {
                argList.addAll(Arrays.asList(args));
            }
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("ping hello")) {
                    rs.next();
                    assert rs.getString(1).equals("abc");
                    assert rs.getString("RESULT").equals("abc");
                }
            }

            assert argList.get(0).equals("hello");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void echo_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ServerCommands.class, createInvocationHandler("echo", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("echo hello")) {
                    rs.next();
                    assert rs.getString(1).equals("abc");
                    assert rs.getString("RESULT").equals("abc");
                }
            }

            assert argList.get(0).equals("hello");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void select_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "ok";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(DatabaseCommands.class, createInvocationHandler("select", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("select 111") == 1;
            }

            assert argList.get(0).equals(111);
        } catch (SQLException e) {
            assert false;
        }
    }
}