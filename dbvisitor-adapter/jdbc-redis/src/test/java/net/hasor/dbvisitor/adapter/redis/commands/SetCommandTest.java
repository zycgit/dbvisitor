package net.hasor.dbvisitor.adapter.redis.commands;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import org.junit.Test;
import redis.clients.jedis.commands.SetCommands;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

public class SetCommandTest extends AbstractJdbcTest {
    @Test
    public void sadd_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sadd", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("sadd myKey v1 v2 v3") == 123L;
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "v1", "v2", "v3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void scard_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("scard", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("scard myKey")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("myKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void sdiff_1() {
        List<Object> argList = new ArrayList<>();
        Set<String> returnValue = new HashSet<>(Arrays.asList("1", "2", "3"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sdiff", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("sdiff myKey1 myKey2 myKey3")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("1", "2", "3"));
                    assert r2.equals(Arrays.asList("1", "2", "3"));
                }
            }

            assert argList.size() == 1;
            assert Objects.deepEquals(argList.get(0), new String[] { "myKey1", "myKey2", "myKey3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void sdiffstore_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sdiffstore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("sdiffstore dst myKey1 myKey2") == 123L;
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("dst");
            assert Objects.deepEquals(argList.get(1), new String[] { "myKey1", "myKey2" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void sinter_1() {
        List<Object> argList = new ArrayList<>();
        Set<String> returnValue = new HashSet<>(Arrays.asList("1", "2", "3"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sinter", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("sinter myKey1 myKey2")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("1", "2", "3"));
                    assert r2.equals(Arrays.asList("1", "2", "3"));
                }
            }

            assert argList.size() == 1;
            assert Objects.deepEquals(argList.get(0), new String[] { "myKey1", "myKey2" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void sintercard_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sintercard", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("sintercard 2 myKey1 myKey2")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 1;
            assert Objects.deepEquals(argList.get(0), new String[] { "myKey1", "myKey2" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void sintercard_2() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sintercard", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("sintercard 2 myKey1 myKey2 limit 10")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(10);
            assert Objects.deepEquals(argList.get(1), new String[] { "myKey1", "myKey2" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void sinterstore_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sinterstore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("sinterstore dstKey myKey1 myKey2") == 123L;
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("dstKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "myKey1", "myKey2" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void sismember_1() {
        List<Object> argList = new ArrayList<>();
        boolean returnValue = true;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sismember", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("sismember key member")) {
                    rs.next();
                    assert rs.getLong(1) == 1L;
                    assert rs.getLong("RESULT") == 1L;
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals("member");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void sismember_2() {
        List<Object> argList = new ArrayList<>();
        boolean returnValue = false;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sismember", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("sismember key member")) {
                    rs.next();
                    assert rs.getLong(1) == 0L;
                    assert rs.getLong("RESULT") == 0L;
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals("member");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void smismember_1() {
        List<Object> argList = new ArrayList<>();
        List<Boolean> returnValue = Arrays.asList(true, false, true);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("smismember", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("smismember myKey member1 member2 member3")) {
                    ArrayList<Boolean> r1 = new ArrayList<>();
                    ArrayList<Boolean> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getBoolean(1));
                        r2.add(rs.getBoolean("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(true, false, true));
                    assert r2.equals(Arrays.asList(true, false, true));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "member1", "member2", "member3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void smembers_1() {
        List<Object> argList = new ArrayList<>();
        Set<String> returnValue = new HashSet<>(Arrays.asList("1", "2", "3"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("smembers", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("smembers myKey")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("1", "2", "3"));
                    assert r2.equals(Arrays.asList("1", "2", "3"));
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("myKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void smove_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("smove", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("smove srcKey dstKey member") == 123L;
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("srcKey");
            assert argList.get(1).equals("dstKey");
            assert argList.get(2).equals("member");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void spop_1() {
        List<Object> argList = new ArrayList<>();
        Set<String> returnValue = new HashSet<>(Arrays.asList("1", "2", "3"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("spop", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("spop myKey 123")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("1", "2", "3"));
                    assert r2.equals(Arrays.asList("1", "2", "3"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myKey");
            assert argList.get(1).equals(123L);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void spop_2() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "1";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("spop", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("spop myKey")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("1"));
                    assert r2.equals(Arrays.asList("1"));
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("myKey");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void srandmember_1() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("1", "2", "3");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("srandmember", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("srandmember myKey 123")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("1", "2", "3"));
                    assert r2.equals(Arrays.asList("1", "2", "3"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myKey");
            assert argList.get(1).equals(123);
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void srandmember_2() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "1";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("srandmember", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("srandmember myKey")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("1"));
                    assert r2.equals(Arrays.asList("1"));
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("myKey");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void srem_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("srem", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("srem myKey member1 member2 member3") == 123L;
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "member1", "member2", "member3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void sscan_1() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult returnValue = new ScanResult("0", Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sscan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("sscan key 0")) {
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        assert rs.getString(1).equals("0");
                        assert rs.getString("CURSOR").equals("0");
                        r1.add(rs.getString(2));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                    assert r2.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                }
            }
            assert argList.size() == 3;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals("0");
            assert argList.get(2).equals(new ScanParams());
        }
    }

    @Test
    public void sscan_2() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult returnValue = new ScanResult("0", Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sscan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("sscan key 0 match *a*")) {
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(2));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                    assert r2.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                }
            }
            assert argList.size() == 3;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals("0");
            assert argList.get(2).equals(new ScanParams().match("*a*"));
        }
    }

    @Test
    public void sscan_3() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult returnValue = new ScanResult("0", Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sscan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("sscan key 0 match *a* count 10")) {
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(2));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                    assert r2.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                }
            }
            assert argList.size() == 3;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals("0");
            assert argList.get(2).equals(new ScanParams().match("*a*").count(10));
        }
    }

    @Test
    public void sunion_1() {
        List<Object> argList = new ArrayList<>();
        Set<String> returnValue = new LinkedHashSet<>(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sunion", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("sunion myKey1 myKey2 myKey3")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                    assert r2.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                }
            }

            assert argList.size() == 1;
            assert Objects.deepEquals(argList.get(0), new String[] { "myKey1", "myKey2", "myKey3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void sunionstore_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SetCommands.class, createInvocationHandler("sunionstore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("sunionstore dstKey myKey1 myKey2 myKey3") == 123L;
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("dstKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "myKey1", "myKey2", "myKey3" });
        } catch (SQLException e) {
            assert false;
        }
    }
}