package net.hasor.dbvisitor.adapter.redis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.adapter.redis.support.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.support.RedisCommandInterceptor;
import org.junit.Test;
import redis.clients.jedis.args.ExpiryOption;
import redis.clients.jedis.commands.HashCommands;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

public class HashCommandTest extends AbstractJdbcTest {
    @Test
    public void hdel_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hdel", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("hdel theKey mykey1 mykey2") == 123L;
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "mykey1", "mykey2" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hexists_1() {
        List<Object> argList = new ArrayList<>();
        boolean returnValue = true;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hexists", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hexists theKey mykey")) {
                    if (rs.next()) {
                        assert rs.getBoolean(1);
                        assert rs.getBoolean("RESULT");
                    } else {
                        assert false;
                    }
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("mykey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hexpire_1() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hexpire", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hexpire mykey 10 fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("mykey");
            assert argList.get(1).equals(10L);
            assert Objects.deepEquals(argList.get(2), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hexpire_2() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hexpire", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.setMaxRows(2);
                try (ResultSet rs = stmt.executeQuery("hexpire mykey 10 fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L));
                    assert r2.equals(Arrays.asList(1L, 2L));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("mykey");
            assert argList.get(1).equals(10L);
            assert Objects.deepEquals(argList.get(2), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hexpire_3() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hexpire", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hexpire mykey 10 nx fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 4;
            assert argList.get(0).equals("mykey");
            assert argList.get(1).equals(10L);
            assert argList.get(2).equals(ExpiryOption.NX);
            assert Objects.deepEquals(argList.get(3), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hexpireat_1() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hexpireAt", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hexpireat mykey 10 fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("mykey");
            assert argList.get(1).equals(10L);
            assert Objects.deepEquals(argList.get(2), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hexpireat_2() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hexpireAt", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.setMaxRows(2);
                try (ResultSet rs = stmt.executeQuery("hexpireat mykey 10 fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L));
                    assert r2.equals(Arrays.asList(1L, 2L));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("mykey");
            assert argList.get(1).equals(10L);
            assert Objects.deepEquals(argList.get(2), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hexpireat_3() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hexpireAt", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hexpireat mykey 10 nx fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 4;
            assert argList.get(0).equals("mykey");
            assert argList.get(1).equals(10L);
            assert argList.get(2).equals(ExpiryOption.NX);
            assert Objects.deepEquals(argList.get(3), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hexpiretime_1() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hexpireTime", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hexpireTime mykey fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("mykey");
            assert Objects.deepEquals(argList.get(1), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hpexpire_1() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hpexpire", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hpexpire mykey 10 fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("mykey");
            assert argList.get(1).equals(10L);
            assert Objects.deepEquals(argList.get(2), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hpexpire_2() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hpexpire", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hpexpire mykey 10 nx fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 4;
            assert argList.get(0).equals("mykey");
            assert argList.get(1).equals(10L);
            assert argList.get(2).equals(ExpiryOption.NX);
            assert Objects.deepEquals(argList.get(3), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hpexpireat_1() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hpexpireAt", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hpexpireat mykey 10 fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("mykey");
            assert argList.get(1).equals(10L);
            assert Objects.deepEquals(argList.get(2), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hpexpireat_2() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hpexpireAt", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hpexpireat mykey 10 nx fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 4;
            assert argList.get(0).equals("mykey");
            assert argList.get(1).equals(10L);
            assert argList.get(2).equals(ExpiryOption.NX);
            assert Objects.deepEquals(argList.get(3), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hpexpiretime_1() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hpexpireTime", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hpexpiretime mykey fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("mykey");
            assert Objects.deepEquals(argList.get(1), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hget_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abc";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hget", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hget theKey field")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals("abc");
                        assert rs.getString("VALUE").equals("abc");
                    } else {
                        assert false;
                    }
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("field");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hgetall_1() {
        List<Object> argList = new ArrayList<>();
        Map<String, String> returnValue = new LinkedHashMap<>();
        returnValue.put("field1", "abc");
        returnValue.put("field2", "def");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hgetAll", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hgetall theKey")) {
                    Map<String, String> r1 = new LinkedHashMap<>();
                    Map<String, String> r2 = new LinkedHashMap<>();
                    while (rs.next()) {
                        r1.put(rs.getString("FIELD"), rs.getString("VALUE"));
                        r2.put(rs.getString(1), rs.getString(2));
                    }
                    assert r1.equals(returnValue);
                    assert r2.equals(returnValue);
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("theKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hincrby_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hincrBy", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hincrby myhash field -1")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("VALUE") == 123L;
                    } else {
                        assert false;
                    }
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("myhash");
            assert argList.get(1).equals("field");
            assert argList.get(2).equals(-1L);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hkeys_1() {
        List<Object> argList = new ArrayList<>();
        Set<String> returnValue = new LinkedHashSet<>(Arrays.asList("field1", "field2"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hkeys", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hkeys theKey")) {
                    List<String> r1 = new ArrayList<>();
                    List<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("KEY"));
                        r2.add(rs.getString(1));
                    }
                    assert r1.equals(Arrays.asList("field1", "field2"));
                    assert r2.equals(Arrays.asList("field1", "field2"));
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("theKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hlen_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hlen", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hlen myhash")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("RESULT") == 123L;
                    } else {
                        assert false;
                    }
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("myhash");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hmget_1() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("a", "b");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hmget", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hmget theKey mykey1 mykey2")) {
                    List<String> r1 = new ArrayList<>();
                    List<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("VALUE"));
                        r2.add(rs.getString(1));
                    }
                    assert r1.equals(Arrays.asList("a", "b"));
                    assert r2.equals(Arrays.asList("a", "b"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "mykey1", "mykey2" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hset_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hset", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                assert stmt.executeUpdate("hset myhash field1 hello field2 word") == 123L;
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myhash");
            assert Objects.deepEquals(argList.get(1), CollectionUtils.asMap("field1", "hello", "field2", "word"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hmset_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "OK";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hmset", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hmset myhash field1 hello field2 word")) {
                    if (rs.next()) {
                        assert rs.getString(1).equals("OK");
                        assert rs.getString("RESULT").equals("OK");
                    } else {
                        assert false;
                    }
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("myhash");
            assert Objects.deepEquals(argList.get(1), CollectionUtils.asMap("field1", "hello", "field2", "word"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hsetnx_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hsetnx", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hsetnx myhash field1 hello")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("RESULT") == 123L;
                    } else {
                        assert false;
                    }
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("myhash");
            assert argList.get(1).equals("field1");
            assert argList.get(2).equals("hello");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hpersist_1() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hpersist", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hpersist mykey fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("mykey");
            assert Objects.deepEquals(argList.get(1), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void httl_1() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("httl", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("httl mykey fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("mykey");
            assert Objects.deepEquals(argList.get(1), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hpttl_1() {
        List<Object> argList = new ArrayList<>();
        List<Long> returnValue = Arrays.asList(1L, 2L, 3L);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hpttl", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hpttl mykey fields 3 field1 field2 field3")) {
                    ArrayList<Long> r1 = new ArrayList<>();
                    ArrayList<Long> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getLong(1));
                        r2.add(rs.getLong("RESULT"));
                    }
                    assert r1.equals(Arrays.asList(1L, 2L, 3L));
                    assert r2.equals(Arrays.asList(1L, 2L, 3L));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("mykey");
            assert Objects.deepEquals(argList.get(1), new String[] { "field1", "field2", "field3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hrandfield_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "v1";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hrandfield", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hrandfield theKey")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("FIELD"));
                    }
                    assert r1.equals(Arrays.asList("v1"));
                    assert r2.equals(Arrays.asList("v1"));
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("theKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hrandfield_2() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hrandfield", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hrandfield theKey 10")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("FIELD"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals(10L);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hrandfield_3() {
        List<Object> argList = new ArrayList<>();
        List<Map.Entry<String, String>> returnValue = Arrays.asList(//
                new AbstractMap.SimpleEntry<>("v1k", "v1v"),//
                new AbstractMap.SimpleEntry<>("v2k", "v2v"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hrandfieldWithValues", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hrandfield theKey 10 withvalues")) {
                    Map<String, String> r1 = new LinkedHashMap<>();
                    Map<String, String> r2 = new LinkedHashMap<>();
                    while (rs.next()) {
                        r1.put(rs.getString("FIELD"), rs.getString("VALUE"));
                        r2.put(rs.getString(1), rs.getString(2));
                    }
                    assert r1.equals(CollectionUtils.asMap("v1k", "v1v", "v2k", "v2v"));
                    assert r2.equals(CollectionUtils.asMap("v1k", "v1v", "v2k", "v2v"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals(10L);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hscan_1() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult<Map.Entry<String, String>> returnValue = new ScanResult("0", Arrays.asList(//
                new AbstractMap.SimpleEntry<>("v1k", "v1v"),//
                new AbstractMap.SimpleEntry<>("v2k", "v2v")));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hscan", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hscan key 0")) {
                    Map<String, String> r1 = new LinkedHashMap<>();
                    Map<String, String> r2 = new LinkedHashMap<>();
                    while (rs.next()) {
                        assert rs.getString(1).equals("0");
                        assert rs.getString("CURSOR").equals("0");
                        r1.put(rs.getString("FIELD"), rs.getString("VALUE"));
                        r2.put(rs.getString(2), rs.getString(3));
                    }
                    assert r1.equals(CollectionUtils.asMap("v1k", "v1v", "v2k", "v2v"));
                    assert r2.equals(CollectionUtils.asMap("v1k", "v1v", "v2k", "v2v"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals("0");
            assert argList.get(2).equals(new ScanParams());
        }
    }

    @Test
    public void hscan_2() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult<Map.Entry<String, String>> returnValue = new ScanResult("0", Arrays.asList(//
                new AbstractMap.SimpleEntry<>("v1k", "v1v"),//
                new AbstractMap.SimpleEntry<>("v2k", "v2v")));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hscan", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hscan key 0 match a*a")) {
                    Map<String, String> r1 = new LinkedHashMap<>();
                    Map<String, String> r2 = new LinkedHashMap<>();
                    while (rs.next()) {
                        assert rs.getString(1).equals("0");
                        assert rs.getString("CURSOR").equals("0");
                        r1.put(rs.getString("FIELD"), rs.getString("VALUE"));
                        r2.put(rs.getString(2), rs.getString(3));
                    }
                    assert r1.equals(CollectionUtils.asMap("v1k", "v1v", "v2k", "v2v"));
                    assert r2.equals(CollectionUtils.asMap("v1k", "v1v", "v2k", "v2v"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals("0");
            assert argList.get(2).equals(new ScanParams().match("a*a"));
        }
    }

    @Test
    public void hscan_3() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult<Map.Entry<String, String>> returnValue = new ScanResult("0", Arrays.asList(//
                new AbstractMap.SimpleEntry<>("v1k", "v1v"),//
                new AbstractMap.SimpleEntry<>("v2k", "v2v")));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hscan", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hscan key 0 match a*a count 123")) {
                    Map<String, String> r1 = new LinkedHashMap<>();
                    Map<String, String> r2 = new LinkedHashMap<>();
                    while (rs.next()) {
                        assert rs.getString(1).equals("0");
                        assert rs.getString("CURSOR").equals("0");
                        r1.put(rs.getString("FIELD"), rs.getString("VALUE"));
                        r2.put(rs.getString(2), rs.getString(3));
                    }
                    assert r1.equals(CollectionUtils.asMap("v1k", "v1v", "v2k", "v2v"));
                    assert r2.equals(CollectionUtils.asMap("v1k", "v1v", "v2k", "v2v"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals("0");
            assert argList.get(2).equals(new ScanParams().match("a*a").count(123));
        }
    }

    @Test
    public void hscan_4() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult returnValue = new ScanResult("0", Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hscanNoValues", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("hscan key 0 novalues")) {
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        assert rs.getString(1).equals("0");
                        assert rs.getString("CURSOR").equals("0");
                        r1.add(rs.getString(2));
                        r2.add(rs.getString("FIELD"));
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
    public void hstrlen_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hstrlen", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));

        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hstrlen theKey field")) {
                    if (rs.next()) {
                        assert rs.getLong(1) == 123L;
                        assert rs.getLong("RESULT") == 123L;
                    } else {
                        assert false;
                    }
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("field");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void hvals_1() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(HashCommands.class, createInvocationHandler("hvals", args -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));

        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("hvals theKey")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("VALUE"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("theKey");
        } catch (SQLException e) {
            assert false;
        }
    }
}