package net.hasor.dbvisitor.adapter.redis.connect;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.JedisKeys;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import net.hasor.dbvisitor.adapter.redis.RedisCustomJedis;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.Test;
import redis.clients.jedis.commands.StringCommands;

public class SeparatorCharTest extends AbstractJdbcTest {
    public Connection redisConnection(char separatorChar) throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(JedisKeys.INTERCEPTOR, RedisCommandInterceptor.class.getName());
        prop.setProperty(JedisKeys.CUSTOM_JEDIS, RedisCustomJedis.class.getName());
        prop.setProperty(JedisKeys.SEPARATOR_CHAR, String.valueOf(separatorChar));
        return new JdbcDriver().connect("jdbc:dbvisitor:jedis://xxxxxx", prop);
    }

    @Test
    public void test_1() throws SQLException {
        List<Object> getArgList = new ArrayList<>();
        List<Object> setArgList = new ArrayList<>();

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler(new String[] { "set", "get" }, (name, args) -> {
            if (StringUtils.equalsIgnoreCase(name, "set")) {
                setArgList.addAll(Arrays.asList(args));
                return "ok";
            }
            if (StringUtils.equalsIgnoreCase(name, "get")) {
                getArgList.addAll(Arrays.asList(args));
                return "abc2";
            }

            throw new UnsupportedOperationException();
        }));

        try (Connection conn = redisConnection(';')) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("set aaa 123; get aaa");
                assert stmt.getUpdateCount() == 1;
                assert stmt.getUpdateCount() == 1;

                assert stmt.getMoreResults();

                try (ResultSet rs = stmt.getResultSet()) {
                    rs.next();
                    assert rs.getString(1).equals("abc2");
                }
            }

            assert getArgList.size() == 1;
            assert getArgList.get(0).equals("aaa");
            assert setArgList.size() == 3;
            assert setArgList.get(0).equals("aaa");
            assert setArgList.get(1).equals("123");
        }
    }

    @Test
    public void test_1_error() throws SQLException {
        try (Connection conn = redisConnection(';')) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("set aaa 123\n get aaa");
                assert false;
            } catch (SQLException e) {
                assert e.getMessage().contains("parserFailed. (separatorChar = ';')");
            }
        }
    }

    @Test
    public void test_2() throws SQLException {
        List<Object> getArgList = new ArrayList<>();
        List<Object> setArgList = new ArrayList<>();

        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler(new String[] { "set", "get" }, (name, args) -> {
            if (StringUtils.equalsIgnoreCase(name, "set")) {
                setArgList.addAll(Arrays.asList(args));
                return "abc1";
            }
            if (StringUtils.equalsIgnoreCase(name, "get")) {
                getArgList.addAll(Arrays.asList(args));
                return "abc2";
            }

            throw new UnsupportedOperationException();
        }));

        try (Connection conn = redisConnection('\n')) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("set aaa 123\n get aaa");
                assert stmt.getUpdateCount() == 0;
                assert stmt.getUpdateCount() == 0;

                assert stmt.getMoreResults();

                try (ResultSet rs = stmt.getResultSet()) {
                    rs.next();
                    assert rs.getString(1).equals("abc2");
                }

            }

            assert getArgList.size() == 1;
            assert getArgList.get(0).equals("aaa");
            assert setArgList.size() == 3;
            assert setArgList.get(0).equals("aaa");
            assert setArgList.get(1).equals("123");
        }
    }

    @Test
    public void test_2_error() throws SQLException {
        try (Connection conn = redisConnection('\n')) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("set aaa 123; get aaa");
                assert false;
            } catch (SQLException e) {
                assert e.getMessage().contains("parserFailed. (separatorChar = '\\n')");
            }
        }
    }
}
