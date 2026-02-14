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
import org.bson.Document;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.mockito.ArgumentMatchers.any;

public class DatabaseCommandTest extends AbstractJdbcTest {

    @Test
    public void show_dbs_0() {
        List<String> result = Arrays.asList("db1", "db2");
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoCluster.class, createInvocationHandler(new String[] { "listDatabaseNames", "getDatabase" }, (name, args) -> {
            if ("listDatabaseNames".equals(name)) {
                return mockListDatabasesIterable(result);
            }
            if ("getDatabase".equals(name)) {
                MongoDatabase mockDb = PowerMockito.mock(MongoDatabase.class);
                PowerMockito.when(mockDb.runCommand(any())).thenAnswer(inv -> {
                    Document doc = inv.getArgument(0);
                    if (doc.containsKey("buildInfo")) {
                        return new Document("version", "4.0.0");
                    }
                    return null;
                });
                return mockDb;
            }
            return null;
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
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void drop_database_0() {
        List<String> dbs = Collections.singletonList("mydb");
        List<Object> dropped = new ArrayList<>();
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoCluster.class, createInvocationHandler(new String[] { "listDatabaseNames", "getDatabase" }, (name, args) -> {
            if ("listDatabaseNames".equals(name)) {
                return mockListDatabasesIterable(dbs);
            }
            if ("getDatabase".equals(name)) {
                MongoDatabase mockDb = PowerMockito.mock(MongoDatabase.class);
                PowerMockito.when(mockDb.runCommand(any())).thenAnswer(inv -> {
                    Document doc = inv.getArgument(0);
                    if (doc.containsKey("buildInfo")) {
                        return new Document("version", "4.0.0");
                    }
                    return null;
                });
                PowerMockito.doAnswer(inv -> {
                    dropped.add("mydb");
                    return null;
                }).when(mockDb).drop();
                return mockDb;
            }
            return null;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            assert stmt.executeUpdate("mydb.dropDatabase()") == 0;
            assert dropped.size() == 1;
            assert dropped.get(0).equals("mydb");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void drop_database_1() {
        List<String> dbs = Collections.singletonList("mydb");
        List<Object> dropped = new ArrayList<>();
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoCluster.class, createInvocationHandler(new String[] { "listDatabaseNames", "getDatabase" }, (name, args) -> {
            if ("listDatabaseNames".equals(name)) {
                return mockListDatabasesIterable(dbs);
            }
            if ("getDatabase".equals(name)) {
                MongoDatabase mockDb = PowerMockito.mock(MongoDatabase.class);
                PowerMockito.when(mockDb.runCommand(any())).thenAnswer(inv -> {
                    Document doc = inv.getArgument(0);
                    if (doc.containsKey("buildInfo")) {
                        return new Document("version", "4.0.0");
                    }
                    return null;
                });
                PowerMockito.doAnswer(inv -> {
                    dropped.add("mydb");
                    return null;
                }).when(mockDb).drop();
                return mockDb;
            }
            return null;
        }));

        try (Connection conn = redisConnection("mydb"); Statement stmt = conn.createStatement()) {
            assert stmt.executeUpdate("db.dropDatabase()") == 0;
            assert dropped.size() == 1;
            assert dropped.get(0).equals("mydb");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void drop_database_2() {
        List<String> dbs = Collections.singletonList("mydb");
        List<Object> dropped = new ArrayList<>();
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoCluster.class, createInvocationHandler(new String[] { "listDatabaseNames", "getDatabase" }, (name, args) -> {
            if ("listDatabaseNames".equals(name)) {
                return mockListDatabasesIterable(dbs);
            }
            if ("getDatabase".equals(name)) {
                MongoDatabase mockDb = PowerMockito.mock(MongoDatabase.class);
                PowerMockito.when(mockDb.runCommand(any())).thenAnswer(inv -> {
                    Document doc = inv.getArgument(0);
                    if (doc.containsKey("buildInfo")) {
                        return new Document("version", "4.0.0");
                    }
                    return null;
                });
                PowerMockito.doAnswer(inv -> {
                    dropped.add("mydb");
                    return null;
                }).when(mockDb).drop();
                return mockDb;
            }
            return null;
        }));

        try (Connection conn = redisConnection("mydb"); Statement stmt = conn.createStatement()) {
            assert stmt.executeUpdate("db.dropDatabase()") == 0;
            assert dropped.size() == 1;
            assert dropped.get(0).equals("mydb");
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void use_database_0() {
        MongoCommandInterceptor.resetInterceptor();

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            assert conn.getCatalog() == null;
            assert conn.getSchema() == null;

            stmt.execute("use mydb");

            assert "mydb".equals(conn.getCatalog());
            assert "mydb".equals(conn.getSchema());
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }
}
