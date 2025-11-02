package net.hasor.dbvisitor.adapter.redis.commands;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import org.junit.Test;
import redis.clients.jedis.args.SortedSetOption;
import redis.clients.jedis.commands.SortedSetCommands;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZParams;
import redis.clients.jedis.params.ZRangeParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.Tuple;
import redis.clients.jedis.util.KeyValue;

public class StoreSetCommandTest extends AbstractJdbcTest {
    @Test
    public void zmpop_1() {
        List<Object> argList = new ArrayList<>();
        KeyValue<String, List<Tuple>> returnValue = new KeyValue<>("key", Arrays.asList(//
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        ));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zmpop", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zmpop 3 v1 v2 v3 min count 5")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    ArrayList<String> r3 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString(2));
                        r3.add(rs.getString(3));
                    }
                    assert r1.equals(Arrays.asList("key", "key"));
                    assert r2.equals(Arrays.asList("123.0", "456.0"));
                    assert r3.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0) == SortedSetOption.MIN;
            assert argList.get(1).equals(5);
            assert Objects.deepEquals(argList.get(2), new String[] { "v1", "v2", "v3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zmpop_2() {
        List<Object> argList = new ArrayList<>();
        KeyValue<String, List<Tuple>> returnValue = new KeyValue<>("key", Arrays.asList(//
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        ));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zmpop", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zmpop 3 v1 v2 v3 min count 5")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    ArrayList<String> r3 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("KEY"));
                        r2.add(rs.getString("SCORE"));
                        r3.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("key", "key"));
                    assert r2.equals(Arrays.asList("123.0", "456.0"));
                    assert r3.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0) == SortedSetOption.MIN;
            assert argList.get(1).equals(5);
            assert Objects.deepEquals(argList.get(2), new String[] { "v1", "v2", "v3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zmpop_3() {
        List<Object> argList = new ArrayList<>();
        KeyValue<String, List<Tuple>> returnValue = new KeyValue<>("key", Arrays.asList(//
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        ));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zmpop", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zmpop 3 v1 v2 v3 min")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    ArrayList<String> r3 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString(2));
                        r3.add(rs.getString(3));
                    }
                    assert r1.equals(Arrays.asList("key", "key"));
                    assert r2.equals(Arrays.asList("123.0", "456.0"));
                    assert r3.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0) == SortedSetOption.MIN;
            assert Objects.deepEquals(argList.get(1), new String[] { "v1", "v2", "v3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zmpop_4() {
        List<Object> argList = new ArrayList<>();
        KeyValue<String, List<Tuple>> returnValue = new KeyValue<>("key", Arrays.asList(//
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        ));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zmpop", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zmpop 3 v1 v2 v3 min")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    ArrayList<String> r3 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("KEY"));
                        r2.add(rs.getString("SCORE"));
                        r3.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("key", "key"));
                    assert r2.equals(Arrays.asList("123.0", "456.0"));
                    assert r3.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0) == SortedSetOption.MIN;
            assert Objects.deepEquals(argList.get(1), new String[] { "v1", "v2", "v3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void bzmpop_1() {
        List<Object> argList = new ArrayList<>();
        KeyValue<String, List<Tuple>> returnValue = new KeyValue<>("key", Arrays.asList(//
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        ));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("bzmpop", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("bzmpop 120 3 v1 v2 v3 min count 5")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    ArrayList<String> r3 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString(2));
                        r3.add(rs.getString(3));
                    }
                    assert r1.equals(Arrays.asList("key", "key"));
                    assert r2.equals(Arrays.asList("123.0", "456.0"));
                    assert r3.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 4;
            assert argList.get(0).equals(120.0d);
            assert argList.get(1) == SortedSetOption.MIN;
            assert argList.get(2).equals(5);
            assert Objects.deepEquals(argList.get(3), new String[] { "v1", "v2", "v3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void bzmpop_2() {
        List<Object> argList = new ArrayList<>();
        KeyValue<String, List<Tuple>> returnValue = new KeyValue<>("key", Arrays.asList(//
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        ));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("bzmpop", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("bzmpop 120 3 v1 v2 v3 min")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    ArrayList<String> r3 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString(2));
                        r3.add(rs.getString(3));
                    }
                    assert r1.equals(Arrays.asList("key", "key"));
                    assert r2.equals(Arrays.asList("123.0", "456.0"));
                    assert r3.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals(120.0d);
            assert argList.get(1) == SortedSetOption.MIN;
            assert Objects.deepEquals(argList.get(2), new String[] { "v1", "v2", "v3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zpopmax_1() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(      //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zpopmax", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zpopmax key1 23")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString(2));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("key1");
            assert argList.get(1).equals(23);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zpopmax_2() {
        List<Object> argList = new ArrayList<>();
        Tuple returnValue = new Tuple("v1", 123.0);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zpopmax", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zpopmax key1")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString(2));
                    }
                    assert r1.equals(Arrays.asList("123.0"));
                    assert r2.equals(Arrays.asList("v1"));
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("key1");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zpopmin_1() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(      //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zpopmin", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zpopmin key1 23")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString(2));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("key1");
            assert argList.get(1).equals(23);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zpopmin_2() {
        List<Object> argList = new ArrayList<>();
        Tuple returnValue = new Tuple("v1", 123.0);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zpopmin", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zpopmin key1")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString(2));
                    }
                    assert r1.equals(Arrays.asList("123.0"));
                    assert r2.equals(Arrays.asList("v1"));
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("key1");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void bzpopmax_1() {
        List<Object> argList = new ArrayList<>();
        KeyValue<String, Tuple> returnValue = new KeyValue<>("key", new Tuple("v1", 123.0));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("bzpopmax", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("bzpopmax myKey1 myKey2 myKey3 123")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    ArrayList<String> r3 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString(2));
                        r3.add(rs.getString(3));
                    }
                    assert r1.equals(Arrays.asList("key"));
                    assert r2.equals(Arrays.asList("123.0"));
                    assert r3.equals(Arrays.asList("v1"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(123d);
            assert Objects.deepEquals(argList.get(1), new String[] { "myKey1", "myKey2", "myKey3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void bzpopmin_1() {
        List<Object> argList = new ArrayList<>();
        KeyValue<String, Tuple> returnValue = new KeyValue<>("key", new Tuple("v1", 123.0));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("bzpopmin", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("bzpopmin myKey1 myKey2 myKey3 123")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    ArrayList<String> r3 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString(2));
                        r3.add(rs.getString(3));
                    }
                    assert r1.equals(Arrays.asList("key"));
                    assert r2.equals(Arrays.asList("123.0"));
                    assert r3.equals(Arrays.asList("v1"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(123d);
            assert Objects.deepEquals(argList.get(1), new String[] { "myKey1", "myKey2", "myKey3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zadd_1() {
        List<Object> argList = new ArrayList<>();
        double returnValue = 1.1d;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zaddIncr", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zadd key nx gt ch incr 123.123 element")) {
                    rs.next();
                    assert rs.getDouble(1) == 1.1d;
                    assert rs.getDouble("RESULT") == 1.1d;
                }
            }

            assert argList.size() == 4;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals(123.123d);
            assert argList.get(2).equals("element");
            assert argList.get(3).equals(new ZAddParams().nx().gt().ch());
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zadd_2() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zadd", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zadd key nx gt ch 123.123 element 333.333 e2")) {
                    rs.next();
                    assert rs.getLong(1) == 123;
                    assert rs.getLong("RESULT") == 123;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals(CollectionUtils.asMap("element", 123.123d, "e2", 333.333d));
            assert argList.get(2).equals(new ZAddParams().nx().gt().ch());
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zadd_3() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zadd", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zadd key nx 123.123 element 333.333 e2")) {
                    rs.next();
                    assert rs.getLong(1) == 123;
                    assert rs.getLong("RESULT") == 123;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals(CollectionUtils.asMap("element", 123.123d, "e2", 333.333d));
            assert argList.get(2).equals(new ZAddParams().nx());
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zadd_4() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zadd", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zadd key 123.123 element 333.333 e2")) {
                    rs.next();
                    assert rs.getLong(1) == 123;
                    assert rs.getLong("RESULT") == 123;
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals(CollectionUtils.asMap("element", 123.123d, "e2", 333.333d));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zcard_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zcard", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zcard key")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 1;
            assert argList.get(0).equals("key");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zcount_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zcount", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zcount key 123 321")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals("123");
            assert argList.get(2).equals("321");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zdiff_1() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("1", "2", "3");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zdiff", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zdiff 2 mykey1 mykey2")) {
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
            assert Objects.deepEquals(argList.get(0), new String[] { "mykey1", "mykey2" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zdiff_2() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(      //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zdiffWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zdiff 2 mykey1 mykey2 withscores")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 1;
            assert Objects.deepEquals(argList.get(0), new String[] { "mykey1", "mykey2" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zdiffstore_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zdiffstore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zdiffstore destKey 2 mykey1 mykey2")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("destKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "mykey1", "mykey2" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zincrby_1() {
        List<Object> argList = new ArrayList<>();
        double returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zincrby", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zincrby theKey 1 element")) {
                    rs.next();
                    assert rs.getDouble(1) == 123d;
                    assert rs.getDouble("SCORE") == 123d;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals(1d);
            assert argList.get(2).equals("element");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zinter_1() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(       //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zinterWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zinter 3 key1 key2 key3 weights 1 1 1 aggregate sum withscores")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(new ZParams().weights(1, 1, 1).aggregate(ZParams.Aggregate.SUM));
            assert Objects.deepEquals(argList.get(1), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zinter_2() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(       //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zinterWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zinter 3 key1 key2 key3 weights 1 1 1 withscores")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(new ZParams().weights(1, 1, 1));
            assert Objects.deepEquals(argList.get(1), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zinter_3() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(       //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zinterWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zinter 3 key1 key2 key3 withscores")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(new ZParams());
            assert Objects.deepEquals(argList.get(1), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zinter_4() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zinter", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zinter 3 key1 key2 key3 weights 1 1 1 aggregate sum")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(new ZParams().weights(1, 1, 1).aggregate(ZParams.Aggregate.SUM));
            assert Objects.deepEquals(argList.get(1), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zinter_5() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zinter", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zinter 3 key1 key2 key3 weights 1 1 1")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(new ZParams().weights(1, 1, 1));
            assert Objects.deepEquals(argList.get(1), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zintercard_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zintercard", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zintercard 3 key1 key2 key3 limit 10")) {
                    rs.next();
                    assert rs.getLong(1) == 123d;
                    assert rs.getLong("RESULT") == 123d;
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(10L);
            assert Objects.deepEquals(argList.get(1), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zintercard_2() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zintercard", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zintercard 3 key1 key2 key3")) {
                    rs.next();
                    assert rs.getLong(1) == 123d;
                    assert rs.getLong("RESULT") == 123d;
                }
            }

            assert argList.size() == 1;
            assert Objects.deepEquals(argList.get(0), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zinterstore_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zinterstore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zinterstore dstKey 3 key1 key2 key3 weights 1 1 1 aggregate sum")) {
                    rs.next();
                    assert rs.getLong(1) == 123d;
                    assert rs.getLong("RESULT") == 123d;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("dstKey");
            assert argList.get(1).equals(new ZParams().weights(1, 1, 1).aggregate(ZParams.Aggregate.SUM));
            assert Objects.deepEquals(argList.get(2), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zlexcount_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zlexcount", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zlexcount theKey 111 222")) {
                    rs.next();
                    assert rs.getDouble(1) == 123d;
                    assert rs.getDouble("RESULT") == 123d;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("111");
            assert argList.get(2).equals("222");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zscore_1() {
        List<Object> argList = new ArrayList<>();
        Double returnValue = 123d;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zscore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zscore theKey element")) {
                    rs.next();
                    assert rs.getDouble(1) == 123d;
                    assert rs.getDouble("SCORE") == 123d;
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("element");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zmscore_1() {
        List<Object> argList = new ArrayList<>();
        List<Double> returnValue = Arrays.asList(1.1, 1.2, 1.3);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zmscore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zmscore theKey element1 element2 element3")) {
                    ArrayList<Double> r1 = new ArrayList<>();
                    ArrayList<Double> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getDouble(1));
                        r2.add(rs.getDouble("SCORE"));
                    }
                    assert r1.equals(Arrays.asList(1.1d, 1.2d, 1.3d));
                    assert r2.equals(Arrays.asList(1.1d, 1.2d, 1.3d));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "element1", "element2", "element3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrandmember_1() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(       //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrandmemberWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrandmember theKey 10 withscores")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString(2));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
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
    public void zrandmember_2() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrandmember", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrandmember theKey 10")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
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
    public void zrandmember_3() {
        List<Object> argList = new ArrayList<>();
        String returnValue = "v1";

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrandmember", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrandmember theKey")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
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
    public void zrange_1() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(       //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrangeWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrange theKey 10 20 byscore rev limit 11 22 withscores")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals(ZRangeParams.zrangeByScoreParams(10, 20).limit(11, 22).rev());
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrange_2() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(       //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrangeWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrange theKey 10 20 limit 11 22 withscores")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals(ZRangeParams.zrangeParams(10, 20).limit(11, 22));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrange_3() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(       //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrangeWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrange theKey 10 20 withscores")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals(ZRangeParams.zrangeParams(10, 20));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrange_4() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrange", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrange theKey 10 20")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals(ZRangeParams.zrangeParams(10, 20));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrange_5() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrange", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrange theKey 10 20 byscore rev limit 11 22")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals(ZRangeParams.zrangeByScoreParams(10, 20).limit(11, 22).rev());
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrangebylex_1() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrangeByLex", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrangebylex theKey 10 20 limit 11 22")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 5;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("10");
            assert argList.get(2).equals("20");
            assert argList.get(3).equals(11);
            assert argList.get(4).equals(22);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrangebylex_2() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrangeByLex", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrangebylex theKey 10 20")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("10");
            assert argList.get(2).equals("20");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrangebyscore_1() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(       //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrangeByScoreWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrangebyscore theKey 10 20 withscores limit 11 22")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 5;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("10");
            assert argList.get(2).equals("20");
            assert argList.get(3).equals(11);
            assert argList.get(4).equals(22);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrangebyscore_2() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(       //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrangeByScoreWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrangebyscore theKey 10 20 withscores")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("10");
            assert argList.get(2).equals("20");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrangebyscore_3() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrangeByScore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrangebyscore theKey 10 20 limit 11 22")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 5;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("10");
            assert argList.get(2).equals("20");
            assert argList.get(3).equals(11);
            assert argList.get(4).equals(22);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrangebyscore_4() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrangeByScore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrangebyscore theKey 10 20")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("10");
            assert argList.get(2).equals("20");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrangestore_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrangestore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrangestore dstKey srcKey 10 20 byscore rev limit 11 22")) {
                    rs.next();
                    assert rs.getLong(1) == 123d;
                    assert rs.getLong("RESULT") == 123d;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("dstKey");
            assert argList.get(1).equals("srcKey");
            assert argList.get(2).equals(ZRangeParams.zrangeByScoreParams(10, 20).limit(11, 22).rev());
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrangestore_2() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrangestore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrangestore dstKey srcKey 10 20 rev limit 11 22")) {
                    rs.next();
                    assert rs.getLong(1) == 123d;
                    assert rs.getLong("RESULT") == 123d;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("dstKey");
            assert argList.get(1).equals("srcKey");
            assert argList.get(2).equals(ZRangeParams.zrangeParams(10, 20).limit(11, 22).rev());
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrangestore_3() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrangestore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrangestore dstKey srcKey 10 20")) {
                    rs.next();
                    assert rs.getLong(1) == 123d;
                    assert rs.getLong("RESULT") == 123d;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("dstKey");
            assert argList.get(1).equals("srcKey");
            assert argList.get(2).equals(ZRangeParams.zrangeParams(10, 20));
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrank_1() {
        List<Object> argList = new ArrayList<>();
        KeyValue<Long, Double> returnValue = new KeyValue<>(123L, 123.0);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrankWithScore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrank theKey element withscore")) {
                    rs.next();
                    assert rs.getDouble(1) == 123.0d;
                    assert rs.getDouble("SCORE") == 123.0d;
                    assert rs.getLong(2) == 123L;
                    assert rs.getLong("RANK") == 123L;
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("element");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrank_2() {
        List<Object> argList = new ArrayList<>();
        Long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrank", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrank theKey element")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RANK") == 123L;
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("element");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrevrank_1() {
        List<Object> argList = new ArrayList<>();
        KeyValue<Long, Double> returnValue = new KeyValue<>(123L, 123.0);

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrevrankWithScore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrevrank theKey element withscore")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RANK") == 123L;
                    assert rs.getDouble(2) == 123.0d;
                    assert rs.getDouble("SCORE") == 123.0d;
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("element");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrevrank_2() {
        List<Object> argList = new ArrayList<>();
        Long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrevrank", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrevrank theKey element")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RANK") == 123L;
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("element");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrem_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrem", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrem theKey v1 v2 v3")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals("theKey");
            assert Objects.deepEquals(argList.get(1), new String[] { "v1", "v2", "v3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zremrangebylex_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zremrangeByLex", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zremrangebylex theKey 11 20")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("11");
            assert argList.get(2).equals("20");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zremrangebyrank_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zremrangeByRank", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zremrangebyrank theKey 11 20")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals(11L);
            assert argList.get(2).equals(20L);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zremrangebyscore_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123L;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zremrangeByScore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zremrangebyscore theKey 11 20")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("11");
            assert argList.get(2).equals("20");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrevrange_1() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(      //
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrevrangeWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrevrange theKey 11 20 withscores")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString(2));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals(11L);
            assert argList.get(2).equals(20L);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrevrange_2() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrevrange", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrevrange theKey 11 20")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals(11L);
            assert argList.get(2).equals(20L);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrevrangebylex_1() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrevrangeByLex", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrevrangebylex theKey 11 20 limit 0 2")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 5;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("11");
            assert argList.get(2).equals("20");
            assert argList.get(3).equals(0);
            assert argList.get(4).equals(2);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrevrangebylex_2() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrevrangeByLex", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrevrangebylex theKey 11 20")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString(1));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("11");
            assert argList.get(2).equals("20");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrevrangebyscore_1() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(//
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrevrangeByScoreWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrevrangebyscore theKey 11 20 withscores limit 0 2")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 5;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("11");
            assert argList.get(2).equals("20");
            assert argList.get(3).equals(0);
            assert argList.get(4).equals(2);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrevrangebyscore_2() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(//
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrevrangeByScoreWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrevrangebyscore theKey 11 20 withscores")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("11");
            assert argList.get(2).equals("20");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrevrangebyscore_3() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrevrangeByScore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrevrangebyscore theKey 11 20 limit 0 2")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 5;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("11");
            assert argList.get(2).equals("20");
            assert argList.get(3).equals(0);
            assert argList.get(4).equals(2);
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zrevrangebyscore_4() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zrevrangeByScore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zrevrangebyscore theKey 11 20")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("theKey");
            assert argList.get(1).equals("11");
            assert argList.get(2).equals("20");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zscan_1() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult<Tuple> returnValue = new ScanResult("0", Arrays.asList(//
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        ));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zscan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("zscan key 0")) {
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }
            assert argList.size() == 3;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals("0");
            assert argList.get(2).equals(new ScanParams());
        }
    }

    @Test
    public void zscan_2() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult<Tuple> returnValue = new ScanResult("0", Arrays.asList(//
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        ));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zscan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("zscan key 0 match v*")) {
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }
            assert argList.size() == 3;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals("0");
            assert argList.get(2).equals(new ScanParams().match("v*"));
        }
    }

    @Test
    public void zscan_3() throws SQLException {
        List<Object> argList = new ArrayList<>();
        ScanResult<Tuple> returnValue = new ScanResult("0", Arrays.asList(//
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        ));

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zscan", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("zscan key 0 match v* count 10")) {
                try (ResultSet rs = ps.executeQuery()) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }
            assert argList.size() == 3;
            assert argList.get(0).equals("key");
            assert argList.get(1).equals("0");
            assert argList.get(2).equals(new ScanParams().match("v*").count(10));
        }
    }

    @Test
    public void zunion_1() {
        List<Object> argList = new ArrayList<>();
        List<Tuple> returnValue = Arrays.asList(//
                new Tuple("v1", 123.0), //
                new Tuple("v2", 456.0)  //
        );

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zunionWithScores", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zunion 3 key1 key2 key3 weights 1 1 1 aggregate sum withscores")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    ArrayList<String> r2 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("SCORE"));
                        r2.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("123.0", "456.0"));
                    assert r2.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(new ZParams().weights(1, 1, 1).aggregate(ZParams.Aggregate.SUM));
            assert Objects.deepEquals(argList.get(1), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zunion_2() {
        List<Object> argList = new ArrayList<>();
        List<String> returnValue = Arrays.asList("v1", "v2");

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zunion", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zunion 3 key1 key2 key3 weights 1 1 1 aggregate sum")) {
                    ArrayList<String> r1 = new ArrayList<>();
                    while (rs.next()) {
                        r1.add(rs.getString("ELEMENT"));
                    }
                    assert r1.equals(Arrays.asList("v1", "v2"));
                }
            }

            assert argList.size() == 2;
            assert argList.get(0).equals(new ZParams().weights(1, 1, 1).aggregate(ZParams.Aggregate.SUM));
            assert Objects.deepEquals(argList.get(1), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zunionstore_1() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zunionstore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zunionstore dstKey 3 key1 key2 key3 weights 1 1 1 aggregate sum")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("dstKey");
            assert argList.get(1).equals(new ZParams().weights(1, 1, 1).aggregate(ZParams.Aggregate.SUM));
            assert Objects.deepEquals(argList.get(2), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zunionstore_2() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zunionstore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zunionstore dstKey 3 key1 key2 key3 weights 1 1 1")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("dstKey");
            assert argList.get(1).equals(new ZParams().weights(1, 1, 1));
            assert Objects.deepEquals(argList.get(2), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void zunionstore_3() {
        List<Object> argList = new ArrayList<>();
        long returnValue = 123;

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(SortedSetCommands.class, createInvocationHandler("zunionstore", (name, args) -> {
            argList.addAll(Arrays.asList(args));
            return returnValue;
        }));
        try (Connection conn = redisConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("zunionstore dstKey 3 key1 key2 key3")) {
                    rs.next();
                    assert rs.getLong(1) == 123L;
                    assert rs.getLong("RESULT") == 123L;
                }
            }

            assert argList.size() == 3;
            assert argList.get(0).equals("dstKey");
            assert argList.get(1).equals(new ZParams());
            assert Objects.deepEquals(argList.get(2), new String[] { "key1", "key2", "key3" });
        } catch (SQLException e) {
            assert false;
        }
    }
}