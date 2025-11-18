package net.hasor.dbvisitor.adapter.redis.commands;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import org.junit.Test;
import redis.clients.jedis.commands.StringCommands;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.SetParams;

public class StringCommandTest extends AbstractJdbcTest {
    @Test
    public void set_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "ok";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("set", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("set mykey value") == 1;
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", "value", new SetParams()));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void set_2() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "ok";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("set", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("set mykey value nx") == 1;
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", "value", new SetParams().nx()));
        } catch (SQLException e) {
            assert false;
        } finally {
            argList.clear();
        }

        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("set mykey value xx") == 1;
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", "value", new SetParams().xx()));
        } catch (SQLException e) {
            assert false;
        } finally {
            argList.clear();
        }
    }

    @Test
    public void set_3() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("set", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("set mykey value xx ex 123") == 0;
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", "value", new SetParams().xx().ex(123)));
        } catch (SQLException e) {
            assert false;
        } finally {
            argList.clear();
        }
    }

    @Test
    public void set_4() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "ok";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("set", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("set mykey value xx keepttl") == 1;
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", "value", new SetParams().xx().keepTtl()));
        } catch (SQLException e) {
            assert false;
        } finally {
            argList.clear();
        }
    }

    @Test
    public void set_and_get_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("setGet", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("set mykey value get")) {
                    rs.next();
                    assert rs.getString(1).equals(returnValue);
                    assert rs.getString("VALUE").equals(returnValue);
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", "value", new SetParams()));
        } catch (SQLException e) {
            assert false;
        } finally {
            argList.clear();
        }

        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("set mykey value xx get")) {
                    rs.next();
                    assert rs.getString(1).equals(returnValue);
                    assert rs.getString("VALUE").equals(returnValue);
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", "value", new SetParams().xx()));
        } catch (SQLException e) {
            assert false;
        } finally {
            argList.clear();
        }
    }

    @Test
    public void set_and_get_2() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("setGet", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("set mykey value xx get")) {
                    rs.next();
                    assert rs.getString(1).equals(returnValue);
                    assert rs.getString("VALUE").equals(returnValue);
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", "value", new SetParams().xx()));
        } catch (SQLException e) {
            assert false;
        } finally {
            argList.clear();
        }
    }

    @Test
    public void set_and_get_3() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("setGet", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("set mykey value xx get keepttl")) {
                    rs.next();
                    assert rs.getString(1).equals(returnValue);
                    assert rs.getString("VALUE").equals(returnValue);
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", "value", new SetParams().xx().keepTtl()));
        } catch (SQLException e) {
            assert false;
        } finally {
            argList.clear();
        }
    }

    @Test
    public void get_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("get", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("get mykey")) {
                    rs.next();
                    assert rs.getString(1).equals(returnValue);
                    assert rs.getString("VALUE").equals(returnValue);
                }
            }

            assert argList.size() == 1;
            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void incr_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("incr", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("incr mykey")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("VALUE") == 123L;
                }
            }

            assert argList.size() == 1;
            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void incrby_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("incrBy", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("incrby mykey 111")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("VALUE") == 123L;
                }
            }

            assert argList.size() == 2;
            assert argList.equals(Arrays.asList("mykey", 111L));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void decr_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("decr", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("decr mykey")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("VALUE") == 123L;
                }
            }

            assert argList.size() == 1;
            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void decrby_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("decrBy", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("decrby mykey 111")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("VALUE") == 123L;
                }
            }

            assert argList.size() == 2;
            assert argList.equals(Arrays.asList("mykey", 111L));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void append_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("append", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("append mykey value")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 2;
            assert argList.equals(Arrays.asList("mykey", "value"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void getdel_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getDel", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("getdel mykey")) {
                    rs.next();
                    assert rs.getString(1).equals("abc");
                    assert rs.getString("VALUE").equals("abc");
                }
            }

            assert argList.size() == 1;
            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void getex_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getEx", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("getex mykey")) {
                    rs.next();
                    assert rs.getString(1).equals("abc");
                    assert rs.getString("VALUE").equals("abc");
                }
            }

            assert argList.size() == 2;
            assert argList.equals(Arrays.asList("mykey", new GetExParams()));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void getex_2() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getEx", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("getex mykey persist")) {
                    rs.next();
                    assert rs.getString(1).equals("abc");
                    assert rs.getString("VALUE").equals("abc");
                }
            }

            assert argList.size() == 2;
            assert argList.equals(Arrays.asList("mykey", new GetExParams().persist()));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void getex_3() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getEx", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("getex mykey ex 100")) {
                    rs.next();
                    assert rs.getString(1).equals("abc");
                    assert rs.getString("VALUE").equals("abc");
                }
            }

            assert argList.size() == 2;
            assert argList.equals(Arrays.asList("mykey", new GetExParams().ex(100)));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void getrange_3() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getrange", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("getrange mykey 1 20")) {
                    rs.next();
                    assert rs.getString(1).equals("abc");
                    assert rs.getString("VALUE").equals("abc");
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", 1L, 20L));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void getset_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getSet", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("getset mykey value")) {
                    rs.next();
                    assert rs.getString(1).equals("abc");
                    assert rs.getString("VALUE").equals("abc");
                }
            }

            assert argList.size() == 2;
            assert argList.equals(Arrays.asList("mykey", "value"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void mget_1() throws SQLException {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("mget", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement ps = conn.createStatement()) {
                try (ResultSet rs = ps.executeQuery("mget key1 key2 key3 key4 key5 key6")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("VALUE"));
                    }
                    assert r1.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                    assert r2.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                }
            }
            assert argList.size() == 1;
            assert Objects.deepEquals(argList.get(0), new String[] { "key1", "key2", "key3", "key4", "key5", "key6" });
        }
    }

    @Test
    public void mset_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "ok";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("mset", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("mset ket1 k1value ket2 k2value") == 2L;
            }

            assert argList.size() == 1;
            assert Objects.deepEquals(argList.get(0), new String[] { "ket1", "k1value", "ket2", "k2value" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void msetnx_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 1;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("msetnx", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("msetnx ket1 k1value ket2 k2value") == 2L;
            }

            assert argList.size() == 1;
            assert Objects.deepEquals(argList.get(0), new String[] { "ket1", "k1value", "ket2", "k2value" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void psetex_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "ok";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("psetex", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("psetex ket1 100 value") == 1L;
            }
            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("ket1", 100L, "value"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void setex_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "ok";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("setex", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("setex ket1 100 value") == 1L;
            }
            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("ket1", 100L, "value"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void setnx_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 1;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("setnx", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("setnx ket1 value") == 1L;
            }
            assert argList.size() == 2;
            assert argList.equals(Arrays.asList("ket1", "value"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void setrange_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 1;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("setrange", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("setrange ket1 100 value") == 1L;
            }
            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("ket1", 100L, "value"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void strlen_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 10;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("strlen", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("strlen ket1")) {
                    rs.next();
                    assert rs.getLong(1) == 10L;
                    assert rs.getLong("RESULT") == 10L;
                }
            }
            assert argList.size() == 1;
            assert argList.equals(Arrays.asList("ket1"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void substr_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("substr", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("substr mykey 12 22")) {
                    rs.next();
                    assert rs.getString(1).equals("abc");
                    assert rs.getString("VALUE").equals("abc");
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", 12, 22));
        } catch (SQLException e) {
            assert false;
        }
    }
}