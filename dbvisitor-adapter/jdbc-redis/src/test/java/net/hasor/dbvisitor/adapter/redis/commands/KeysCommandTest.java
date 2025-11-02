package net.hasor.dbvisitor.adapter.redis.commands;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import org.junit.Test;
import redis.clients.jedis.args.ExpiryOption;
import redis.clients.jedis.commands.DatabaseCommands;
import redis.clients.jedis.commands.KeyCommands;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

public class KeysCommandTest extends AbstractJdbcTest {
    @Test
    public void copy_1() {
        List<Object> argList = new ArrayList<>();
        boolean returnValue = true;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("copy", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("copy mykey copyMyKey")) {
                    rs.next();
                    assert rs.getBoolean(1);
                    assert rs.getBoolean("RESULT");
                }
            }

            assert argList.equals(Arrays.asList("mykey", "copyMyKey", false));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void copy_2() {
        List<Object> argList = new ArrayList<>();
        boolean returnValue = false;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("copy", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("copy mykey copyMyKey")) {
                    rs.next();
                    assert !rs.getBoolean(1);
                    assert !rs.getBoolean("RESULT");
                }
            }

            assert argList.equals(Arrays.asList("mykey", "copyMyKey", false));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void copy_3() {
        List<Object> argList = new ArrayList<>();
        boolean returnValue = false;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(DatabaseCommands.class, createInvocationHandler("copy", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("copy mykey copyMyKey db 12 replace")) {
                    rs.next();
                    assert !rs.getBoolean(1);
                    assert !rs.getBoolean("RESULT");
                }
            }

            assert argList.equals(Arrays.asList("mykey", "copyMyKey", 12, true));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void copy_4() {
        List<Object> argList = new ArrayList<>();
        boolean returnValue = false;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(DatabaseCommands.class, createInvocationHandler("copy", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement stmt = conn.prepareStatement("copy mykey ? db 12 replace")) {
                stmt.setString(1, "copyMyKey");
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    assert !rs.getBoolean(1);
                    assert !rs.getBoolean("RESULT");
                }
            }

            assert argList.equals(Arrays.asList("mykey", "copyMyKey", 12, true));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void delete_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("del", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("del mykey copyMyKey")) {
                    rs.next();
                    assert rs.getLong(1) == 2;
                    assert rs.getLong("RESULT") == 2;
                }
            }

            assert ((Object[]) argList.get(0))[0].equals("mykey");
            assert ((Object[]) argList.get(0))[1].equals("copyMyKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void delete_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("del", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement stmt = conn.prepareStatement("del ? ?")) {
                stmt.setString(1, "mykey");
                stmt.setString(2, "copyMyKey");
                try (ResultSet rs = stmt.executeQuery("del mykey copyMyKey")) {
                    rs.next();
                    assert rs.getLong(1) == 2;
                    assert rs.getLong("RESULT") == 2;
                }
            }

            assert ((Object[]) argList.get(0))[0].equals("mykey");
            assert ((Object[]) argList.get(0))[1].equals("copyMyKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void unlink_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("unlink", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("unlink mykey copyMyKey")) {
                    rs.next();
                    assert rs.getLong(1) == 2;
                    assert rs.getLong("RESULT") == 2;
                }
            }

            assert ((Object[]) argList.get(0))[0].equals("mykey");
            assert ((Object[]) argList.get(0))[1].equals("copyMyKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void unlink_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("unlink", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement stmt = conn.prepareStatement("unlink ? ?")) {
                stmt.setString(1, "mykey");
                stmt.setString(2, "copyMyKey");
                try (ResultSet rs = stmt.executeQuery("unlink mykey copyMyKey")) {
                    rs.next();
                    assert rs.getLong(1) == 2;
                    assert rs.getLong("RESULT") == 2;
                }
            }

            assert ((Object[]) argList.get(0))[0].equals("mykey");
            assert ((Object[]) argList.get(0))[1].equals("copyMyKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void dump_1() {
        List<Object> argList = new ArrayList<>();
        byte[] returnValue = new byte[] { 1, 2, 3, 4, 5 };

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("dump", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("dump mykey")) {
                    rs.next();
                    assert rs.getBytes(1) == returnValue;
                    assert rs.getBytes("VALUE") == returnValue;
                }
            }

            assert argList.get(0).equals("mykey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void exists_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("exists", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("exists mykey copyMyKey")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert ((Object[]) argList.get(0))[0].equals("mykey");
            assert ((Object[]) argList.get(0))[1].equals("copyMyKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void expire_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("expire", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("expire mykey 123")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", 123L));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void expire_nx_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("expire", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("expire mykey 123 nx")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", 123L, ExpiryOption.NX));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void expire_xx_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("expire", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("expire mykey 123 xx")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", 123L, ExpiryOption.XX));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void expire_gt_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("expire", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("expire mykey 123 gt")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", 123L, ExpiryOption.GT));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void expire_lt_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("expire", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("expire mykey 123 LT")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", 123L, ExpiryOption.LT));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void expireat_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("expireAt", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("expireat mykey 123")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", 123L));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void expireat_lt_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("expireAt", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("expireat mykey 123 LT")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", 123L, ExpiryOption.LT));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void expiretime_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("expireTime", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("expiretime mykey")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void pexpire_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("pexpire", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("pexpire mykey 123")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", 123L));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void pexpirelt_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("pexpire", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("pexpire mykey 123 LT")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", 123L, ExpiryOption.LT));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void pexpireat_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("pexpireAt", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("pexpireat mykey 123")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", 123L));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void pexpireat_lt_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("pexpireAt", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("pexpireat mykey 123 LT")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", 123L, ExpiryOption.LT));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void pexpiretime_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("pexpireTime", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("pexpiretime mykey")) {
                    rs.next();
                    assert rs.getLong(1) == 2L;
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void keys_1() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult returnValue = new ScanResult("0", Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("scan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("keys ?")) {
                ps.setString(1, "*");
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("KEY"));
                    }
                    assert r1.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                    assert r2.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                }
            }
            assert argList.size() == 2;
            assert argList.get(0) == null;
            assert argList.get(1).equals(new ScanParams().match("*"));
        }
    }

    @Test
    public void keys_2() throws SQLException {
        List<Object> argList = new ArrayList<>();
        Map<String, ScanResult> paged = new HashMap<>();
        paged.put(null, new ScanResult("1", Arrays.asList("key1", "key2")));
        paged.put("1", new ScanResult("2", Arrays.asList("key3", "key4")));
        paged.put("2", new ScanResult("3", Arrays.asList("key5")));
        paged.put("3", new ScanResult("0", Arrays.asList("key6")));
        AtomicInteger scanCnt = new AtomicInteger();

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("scan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            scanCnt.incrementAndGet();
            return paged.get(args[0]);
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("keys ?")) {
                ps.setString(1, "*");
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("KEY"));
                    }
                    assert r1.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                    assert r2.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                }
            }

            assert scanCnt.get() == 4;
            assert argList.size() == 8;
            assert argList.get(0) == null;
            assert argList.get(1).equals(new ScanParams().match("*"));
            assert argList.get(2).equals("1");
            assert argList.get(3).equals(new ScanParams().match("*"));
            assert argList.get(4).equals("2");
            assert argList.get(5).equals(new ScanParams().match("*"));
            assert argList.get(6).equals("3");
            assert argList.get(7).equals(new ScanParams().match("*"));
        }
    }

    @Test
    public void keys_3() throws SQLException {
        List<Object> argList = new ArrayList<>();
        Map<String, ScanResult> paged = new HashMap<>();
        paged.put(null, new ScanResult("1", Arrays.asList("key1", "key2")));
        paged.put("1", new ScanResult("2", Arrays.asList("key3", "key4")));
        paged.put("2", new ScanResult("3", Arrays.asList("key5")));
        paged.put("3", new ScanResult("0", Arrays.asList("key6")));
        AtomicInteger scanCnt = new AtomicInteger();

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("scan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            scanCnt.incrementAndGet();
            return paged.get(args[0]);
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("keys ?")) {
                ps.setString(1, "*");
                ps.setMaxRows(4);
                ps.setFetchSize(20);
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("KEY"));
                    }
                    assert r1.equals(Arrays.asList("key1", "key2", "key3", "key4"));
                    assert r2.equals(Arrays.asList("key1", "key2", "key3", "key4"));
                }
            }

            assert scanCnt.get() == 2;
            assert argList.size() == 4;
            assert argList.get(0) == null;
            assert argList.get(1).equals(new ScanParams().match("*").count(20));
            assert argList.get(2).equals("1");
            assert argList.get(3).equals(new ScanParams().match("*").count(20));
        }
    }

    @Test
    public void object_0() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abcdefg";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("objectEncoding", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("object encoding mykey")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assert rs.getString("RESULT").equals("abcdefg");
                }
            }

            assert argList.size() == 1;
            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void object_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("objectFreq", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("object freq mykey")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 1;
            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void object_2() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("objectIdletime", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("object idletime mykey")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 1;
            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void object_3() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("objectRefcount", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("object refcount mykey")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 1;
            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void persist_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("persist", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("persist mykey")) {
                    rs.next();
                    assert rs.getLong("RESULT") == 2L;
                }
            }

            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void ttl_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("ttl", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("ttl mykey")) {
                    rs.next();
                    assert rs.getLong("RESULT") == 2;
                }
            }

            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void pttl_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("pttl", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("pttl mykey")) {
                    rs.next();
                    assert rs.getLong("RESULT") == 2;
                }
            }

            assert argList.equals(Arrays.asList("mykey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void randomkey_0() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "abcdef";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("randomKey", (name, args) -> {
            if (args != null) {
                argList.addAll(Arrays.asList(args));
            }
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("randomkey")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assert rs.getString("KEY").equals("abcdef");
                }
            }

            assert argList.isEmpty();
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void rename_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "ok";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("rename", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("rename oldKey newKey")) {
                    rs.next();
                    assert rs.getString("RESULT").equals("ok");
                }
            }

            assert argList.equals(Arrays.asList("oldKey", "newKey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void renamenx_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 1;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("renamenx", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("renamenx mykey copyMyKey")) {
                    rs.next();
                    assert rs.getLong("RESULT") == 1L;
                }
            }

            assert argList.equals(Arrays.asList("mykey", "copyMyKey"));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void scan_1() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult returnValue = new ScanResult("0", Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("scan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("scan 0")) {
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        assert rs.getString("CURSOR").equals("0");
                        r1.add(rs.getString(2));
                        r2.add(rs.getString("KEY"));
                    }
                    assert r1.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                    assert r2.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                }
            }
            assert argList.size() == 2;
            assert argList.get(0).equals("0");
            assert argList.get(1).equals(new ScanParams());
        }
    }

    @Test
    public void scan_2() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult returnValue = new ScanResult("0", Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("scan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("scan 0 match ?")) {
                ps.setString(1, "*");
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(2));
                        r2.add(rs.getString("KEY"));
                    }
                    assert r1.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                    assert r2.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                }
            }
            assert argList.size() == 2;
            assert argList.get(0).equals("0");
            assert argList.get(1).equals(new ScanParams().match("*"));
        }
    }

    @Test
    public void scan_3() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult returnValue = new ScanResult("0", Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("scan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("scan 0 match ? count 10")) {
                ps.setString(1, "*");
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(2));
                        r2.add(rs.getString("KEY"));
                    }
                    assert r1.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                    assert r2.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                }
            }
            assert argList.size() == 2;
            assert argList.get(0).equals("0");
            assert argList.get(1).equals(new ScanParams().match("*").count(10));
        }
    }

    @Test
    public void scan_4() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult returnValue = new ScanResult("0", Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("scan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("scan 0 match ? count 10 type abc")) {
                ps.setString(1, "*");
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(2));
                        r2.add(rs.getString("KEY"));
                    }
                    assert r1.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                    assert r2.equals(Arrays.asList("key1", "key2", "key3", "key4", "key5", "key6"));
                }
            }
            assert argList.size() == 3;
            assert argList.get(0).equals("0");
            assert argList.get(1).equals(new ScanParams().match("*").count(10));
            assert argList.get(2).equals("abc");
        }
    }

    @Test
    public void touch_0() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 2;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("touch", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("touch mykey copyMyKey")) {
                    rs.next();
                    assert rs.getLong(1) == 2;
                    assert rs.getLong("RESULT") == 2;
                }
            }

            assert ((Object[]) argList.get(0))[0].equals("mykey");
            assert ((Object[]) argList.get(0))[1].equals("copyMyKey");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void type_1() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "int";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(KeyCommands.class, createInvocationHandler("type", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("type mykey")) {
                    while (rs.next()) {
                        assert rs.getString(1).equals("int");
                        assert rs.getString("RESULT").equals("int");
                    }
                }
            }

            assert argList.get(0).equals("mykey");
        } catch (SQLException e) {
            assert false;
        }
    }
}