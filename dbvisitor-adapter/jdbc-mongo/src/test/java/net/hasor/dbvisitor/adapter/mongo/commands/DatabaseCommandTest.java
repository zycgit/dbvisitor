package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.mongodb.client.MongoCluster;
import com.mongodb.client.MongoDatabase;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

public class DatabaseCommandTest extends AbstractJdbcTest {

    @Test
    public void show_dbs_0() throws SQLException {
        List<String> result = Arrays.asList("db1", "db2");
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoCluster.class, createInvocationHandler("listDatabaseNames", (name, args) -> {
            return mockListDatabasesIterable(result);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("show dbs")) {
                List<String> r = new ArrayList<>();
                while (rs.next()) {
                    r.add(rs.getString("DATABASE"));
                }
                assert result.equals(r);
            }
        } catch (SQLException e) {
            assert false;
        }

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("show databases")) {
                List<String> r = new ArrayList<>();
                while (rs.next()) {
                    r.add(rs.getString("DATABASE"));
                }
                assert result.equals(r);
            }
        }
    }

    @Test
    public void drop_database_0() throws SQLException {
        List<String> dbs = Collections.singletonList("mydb");
        List<Object> dropped = new ArrayList<>();
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoCluster.class, createInvocationHandler(new String[] { "listDatabaseNames", "getDatabase" }, (name, args) -> {
            if ("listDatabaseNames".equals(name)) {
                return mockListDatabasesIterable(dbs);
            }
            if ("getDatabase".equals(name)) {
                return PowerMockito.mock(MongoDatabase.class);
            }
            return null;
        }));
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("drop", (name, args) -> {
            dropped.add("mydb");
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            assert stmt.executeUpdate("mydb.dropDatabase()") == 0;
            assert dropped.size() == 1;
            assert dropped.get(0).equals("mydb");
        }
    }

    @Test
    public void drop_database_1() throws SQLException {
        List<String> dbs = Collections.singletonList("mydb");
        List<Object> dropped = new ArrayList<>();
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoCluster.class, createInvocationHandler(new String[] { "listDatabaseNames", "getDatabase" }, (name, args) -> {
            if ("listDatabaseNames".equals(name)) {
                return mockListDatabasesIterable(dbs);
            }
            if ("getDatabase".equals(name)) {
                return PowerMockito.mock(MongoDatabase.class);
            }
            return null;
        }));
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("drop", (name, args) -> {
            dropped.add("mydb");
            return null;
        }));

        try (Connection conn = redisConnection("mydb"); Statement stmt = conn.createStatement()) {
            assert stmt.executeUpdate("db.dropDatabase()") == 0;
            assert dropped.size() == 1;
            assert dropped.get(0).equals("mydb");
        }
    }

    @Test
    public void drop_database_2() throws SQLException {
        List<String> dbs = Collections.singletonList("mydb");
        List<Object> dropped = new ArrayList<>();
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoCluster.class, createInvocationHandler(new String[] { "listDatabaseNames", "getDatabase" }, (name, args) -> {
            if ("listDatabaseNames".equals(name)) {
                return mockListDatabasesIterable(dbs);
            }
            if ("getDatabase".equals(name)) {
                return PowerMockito.mock(MongoDatabase.class);
            }
            return null;
        }));
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("drop", (name, args) -> {
            dropped.add("mydb");
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            assert stmt.executeUpdate("use mydb") == 0;
            assert stmt.executeUpdate("db.dropDatabase()") == 0;
            assert dropped.size() == 1;
            assert dropped.get(0).equals("mydb");
        }
    }

    @Test
    public void use_database_0() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            assert conn.getCatalog() == null;
            assert conn.getSchema() == null;

            stmt.execute("use mydb");

            assert "mydb".equals(conn.getCatalog());
            assert "mydb".equals(conn.getSchema());
        }
    }
}
