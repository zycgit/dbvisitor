package net.hasor.dbvisitor.adapter.redis;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.hasor.dbvisitor.adapter.redis.support.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.support.RedisCommandInterceptor;
import org.junit.Test;
import redis.clients.jedis.args.ListDirection;
import redis.clients.jedis.args.ListPosition;
import redis.clients.jedis.commands.ListCommands;
import redis.clients.jedis.params.LPosParams;
import redis.clients.jedis.util.KeyValue;

public class ListCommandTest extends AbstractJdbcTest {
    @Test
    public void lmove_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lmove", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lmove sKey dKey right right")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals(returnValue);
                        assert rs.getString("VALUE").equals(returnValue);
                    }
                }
            }

            assert argList.size() == 4;
            assert argList.equals(Arrays.asList("sKey", "dKey", ListDirection.RIGHT, ListDirection.RIGHT));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void blmove_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("blmove", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("blmove sKey dKey right right 12")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals(returnValue);
                        assert rs.getString("VALUE").equals(returnValue);
                    }
                }
            }

            assert argList.size() == 5;
            assert argList.equals(Arrays.asList("sKey", "dKey", ListDirection.RIGHT, ListDirection.RIGHT, 12d));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lmpop_1() {
        List<Object> argList = new ArrayList<>();
        KeyValue returnValue = new KeyValue("abc", Arrays.asList("a", "b", "c"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lmpop", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lmpop 2 sKey dKey right count 10")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("KEY").equals("abc");

                        assert rs.getString(2).equals("a, b, c");
                        assert rs.getString("VALUE").equals("a, b, c");

                        assert rs.getArray(2).getArray().equals(Arrays.asList("a", "b", "c"));
                        assert rs.getArray("VALUE").getArray().equals(Arrays.asList("a", "b", "c"));
                    }
                }
            }

            assert argList.size() == 3;
            assert Objects.deepEquals(argList.toArray(), new Object[] { ListDirection.RIGHT, 10, new String[] { "sKey", "dKey" } });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lmpop_2() {
        List<Object> argList = new ArrayList<>();
        KeyValue returnValue = new KeyValue("abc", Arrays.asList("a", "b", "c"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lmpop", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lmpop 2 sKey dKey right")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("KEY").equals("abc");

                        assert rs.getString(2).equals("a, b, c");
                        assert rs.getString("VALUE").equals("a, b, c");

                        assert rs.getArray(2).getArray().equals(Arrays.asList("a", "b", "c"));
                        assert rs.getArray("VALUE").getArray().equals(Arrays.asList("a", "b", "c"));
                    }
                }
            }

            assert argList.size() == 2;
            assert Objects.deepEquals(argList.toArray(), new Object[] { ListDirection.RIGHT, new String[] { "sKey", "dKey" } });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void blmpop_1() {
        List<Object> argList = new ArrayList<>();
        KeyValue returnValue = new KeyValue("abc", Arrays.asList("a", "b", "c"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("blmpop", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("blmpop 120 2 sKey dKey right count 10")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("KEY").equals("abc");

                        assert rs.getString(2).equals("a, b, c");
                        assert rs.getString("VALUE").equals("a, b, c");

                        assert rs.getArray(2).getArray().equals(Arrays.asList("a", "b", "c"));
                        assert rs.getArray("VALUE").getArray().equals(Arrays.asList("a", "b", "c"));
                    }
                }
            }

            assert argList.size() == 4;
            assert Objects.deepEquals(argList.toArray(), new Object[] { 120d, ListDirection.RIGHT, 10, new String[] { "sKey", "dKey" } });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void blmpop_2() {
        List<Object> argList = new ArrayList<>();
        KeyValue returnValue = new KeyValue("abc", Arrays.asList("a", "b", "c"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("blmpop", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("blmpop 120 2 sKey dKey right")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("KEY").equals("abc");

                        assert rs.getString(2).equals("a, b, c");
                        assert rs.getString("VALUE").equals("a, b, c");

                        assert rs.getArray(2).getArray().equals(Arrays.asList("a", "b", "c"));
                        assert rs.getArray("VALUE").getArray().equals(Arrays.asList("a", "b", "c"));
                    }
                }
            }

            assert argList.size() == 3;
            assert Objects.deepEquals(argList.toArray(), new Object[] { 120d, ListDirection.RIGHT, new String[] { "sKey", "dKey" } });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lpop_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lpop", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lpop myKey")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("VALUE"));
                    }
                    assert r1.equals(Arrays.asList("abc"));
                    assert r2.equals(Arrays.asList("abc"));
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("myKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lpop_2() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("a", "b", "c");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lpop", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lpop myKey 10")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("VALUE"));
                    }
                    assert r1.equals(Arrays.asList("a", "b", "c"));
                    assert r2.equals(Arrays.asList("a", "b", "c"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myKey");
            assert argList.get(1).equals(10);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void rpop_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("rpop", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("rpop myKey")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("VALUE"));
                    }
                    assert r1.equals(Arrays.asList("abc"));
                    assert r2.equals(Arrays.asList("abc"));
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("myKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void rpop_2() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("a", "b", "c");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("rpop", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("rpop myKey 10")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("VALUE"));
                    }
                    assert r1.equals(Arrays.asList("a", "b", "c"));
                    assert r2.equals(Arrays.asList("a", "b", "c"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myKey");
            assert argList.get(1).equals(10);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void blpop_1() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("a", "b", "c");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("blpop", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("blpop myKey1 myKey2 123")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("VALUE"));
                    }
                    assert r1.equals(Arrays.asList("a", "b", "c"));
                    assert r2.equals(Arrays.asList("a", "b", "c"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(123);
            assert Objects.deepEquals(argList.get(1), new String[] { "myKey1", "myKey2" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void brpop_1() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("a", "b", "c");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("brpop", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("brpop myKey1 myKey2 123")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("VALUE"));
                    }
                    assert r1.equals(Arrays.asList("a", "b", "c"));
                    assert r2.equals(Arrays.asList("a", "b", "c"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(123);
            assert Objects.deepEquals(argList.get(1), new String[] { "myKey1", "myKey2" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void rpoplpush_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("rpoplpush", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("rpoplpush myKey1 myKey2")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    }
                }
            }

            assert argList.size() == 2;
            assert argList.equals(Arrays.asList("myKey1", "myKey2"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void brpoplpush_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("brpoplpush", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("brpoplpush myKey1 myKey2 120")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    }
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("myKey1", "myKey2", 120));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lindex_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lindex", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lindex myKey1 120")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    }
                }
            }

            assert argList.size() == 2;
            assert argList.equals(Arrays.asList("myKey1", 120L));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void linsert_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("linsert", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("linsert myKey1 after Hello Word")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
                }
            }

            assert argList.size() == 4;
            assert argList.equals(Arrays.asList("myKey1", ListPosition.AFTER, "Hello", "Word"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void llen_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("llen", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("llen myKey1")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
                }
            }

            assert argList.size() == 1;
            assert argList.equals(Arrays.asList("myKey1"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lpos_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lpos", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lpos myKey1 ele")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
                }
            }

            assert argList.size() == 2;
            assert argList.equals(Arrays.asList("myKey1", "ele"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lpos_2() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lpos", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lpos myKey1 ele rank 120")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("myKey1", "ele", new LPosParams().rank(120)));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lpos_3() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lpos", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lpos myKey1 ele rank 120 count 5")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("VALUE"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 4;
            assert argList.equals(Arrays.asList("myKey1", "ele", new LPosParams().rank(120), 5L));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lpos_4() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lpos", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lpos myKey1 ele rank 120 count 5 maxlen 111")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("VALUE"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 4;
            assert argList.equals(Arrays.asList("myKey1", "ele", new LPosParams().rank(120).maxlen(111), 5L));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lpush_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lpush", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lpush myKey v1 v2 v3")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "v1", "v2", "v3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lpushx_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lpushx", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lpushx myKey v1 v2 v3")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "v1", "v2", "v3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void rpush_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("rpush", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("rpush myKey v1 v2 v3")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "v1", "v2", "v3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void rpushx_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("rpushx", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("rpushx myKey v1 v2 v3")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "v1", "v2", "v3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lrange_4() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("1", "2", "3");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lrange", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lrange myKey 10 50")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("VALUE"));
                    }
                    assert r1.equals(Arrays.asList("1", "2", "3"));
                    assert r2.equals(Arrays.asList("1", "2", "3"));
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("myKey", 10L, 50L));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lrem_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lrem", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lrem myKey 10 value")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("myKey", 10L, "value"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void lset_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("lset", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("lset myKey 10 value")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    }
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("myKey", 10L, "value"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void ltrim_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(ListCommands.class, createInvocationHandler("ltrim", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("ltrim myKey 10 20")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    }
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("myKey", 10L, 20L));
        } catch (SQLException e) {
            assert false;
        }
    }
}