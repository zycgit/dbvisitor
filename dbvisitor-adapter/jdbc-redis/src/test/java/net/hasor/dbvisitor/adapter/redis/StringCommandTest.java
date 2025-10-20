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
import redis.clients.jedis.commands.StringCommands;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.SetParams;

public class StringCommandTest extends AbstractJdbcTest {
    @Test
    public void set_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("set", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("set mykey value")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals(returnValue);
                        assert rs.getString("VALUE").equals(returnValue);
                    }
                }
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
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("set", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("set mykey value nx")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals(returnValue);
                        assert rs.getString("VALUE").equals(returnValue);
                    }
                }
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
                try (ResultSet rs = stmt.executeQuery("set mykey value xx")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals(returnValue);
                        assert rs.getString("VALUE").equals(returnValue);
                    }
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
    public void set_3() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("set", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("set mykey value xx ex 123")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals(returnValue);
                        assert rs.getString("VALUE").equals(returnValue);
                    }
                }
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
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("set", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("set mykey value xx keepttl")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals(returnValue);
                        assert rs.getString("VALUE").equals(returnValue);
                    }
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", "value", new SetParams().xx().keepTtl()));
        } catch (SQLException e) {
            e.printStackTrace();
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("setGet", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("set mykey value get")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals(returnValue);
                        assert rs.getString("VALUE").equals(returnValue);
                    }
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
                    while (rs.next()) {
                        assert rs.getString(1).equals(returnValue);
                        assert rs.getString("VALUE").equals(returnValue);
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("setGet", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("set mykey value xx get")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals(returnValue);
                        assert rs.getString("VALUE").equals(returnValue);
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("setGet", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("set mykey value xx get keepttl")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals(returnValue);
                        assert rs.getString("VALUE").equals(returnValue);
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("get", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("get mykey")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals(returnValue);
                        assert rs.getString("VALUE").equals(returnValue);
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("incr", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("incr mykey")) {
                    while (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("incrBy", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("incrby mykey 111")) {
                    while (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("decr", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("decr mykey")) {
                    while (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("decrBy", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("decrby mykey 111")) {
                    while (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("append", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("append mykey value")) {
                    while (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getDel", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("getdel mykey")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getEx", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("getex mykey")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getEx", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("getex mykey persist")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getEx", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("getex mykey ex 100")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getrange", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("getrange mykey 1 20")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getSet", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("getset mykey value")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    }
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("mget", args -> {
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("mset", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("mset ket1 k1value ket2 k2value") == 1;
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("msetnx", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("msetnx ket1 k1value ket2 k2value") == 1;
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("psetex", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("psetex ket1 100 value") == 1;
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("setex", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("setex ket1 100 value") == 1;
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("setnx", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("setnx ket1 value") == 1;
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("setrange", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("setrange ket1 100 value") == 1;
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("strlen", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("strlen ket1") == 10L;
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
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("substr", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("substr mykey 12 22")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    }
                }
            }

            assert argList.size() == 3;
            assert argList.equals(Arrays.asList("mykey", 12, 22));
        } catch (SQLException e) {
            assert false;
        }
    }
}