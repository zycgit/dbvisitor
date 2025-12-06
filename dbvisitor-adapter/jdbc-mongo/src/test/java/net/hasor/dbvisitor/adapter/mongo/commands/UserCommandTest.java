package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import com.mongodb.client.MongoDatabase;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;

public class UserCommandTest extends AbstractJdbcTest {
    @Test
    public void create_user_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());

            assert doc.getString("createUser").getValue().equals("myUser");
            assert doc.getString("pwd").getValue().equals("password");
            assert doc.getArray("roles").get(0).asString().getValue().equals("read");

            return new Document("ok", 1.0);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.createUser('myUser', 'password', ['read'])");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void create_user_1() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());

            assert doc.getString("createUser").getValue().equals("myUser");
            assert doc.getString("pwd").getValue().equals("password");

            BsonDocument role = doc.getArray("roles").get(0).asDocument();
            assert role.getString("role").getValue().equals("read");
            assert role.getString("db").getValue().equals("otherDb");

            return new Document("ok", 1.0);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.createUser('myUser', 'password', [{role: 'read', db: 'otherDb'}])");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void drop_user_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());

            assert doc.getString("dropUser").getValue().equals("myUser");

            return new Document("ok", 1.0);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.dropUser('myUser')");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void update_user_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());

            assert doc.getString("updateUser").getValue().equals("myUser");
            assert doc.getString("pwd").getValue().equals("newPassword");

            return new Document("ok", 1.0);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.updateUser('myUser', {pwd: 'newPassword'})");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void change_user_password_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());

            assert doc.getString("updateUser").getValue().equals("myUser");
            assert doc.getString("pwd").getValue().equals("newPassword");

            return new Document("ok", 1.0);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.changeUserPassword('myUser', 'newPassword')");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void show_users_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
            assert doc.getInt32("usersInfo").getValue() == 1;

            Document userDoc = new Document("_id", "mydb.myUser").append("user", "myUser").append("db", "mydb").append("roles", Arrays.asList(new Document("role", "read").append("db", "mydb")));

            return new Document("users", Arrays.asList(userDoc)).append("ok", 1.0);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            try (ResultSet rs = stmt.executeQuery("show users")) {
                assert rs.next();
                assert "myUser".equals(rs.getString("USER"));
                assert "mydb".equals(rs.getString("DB"));
                assert rs.getString("ROLES").contains("read");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void show_roles_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
            assert doc.getInt32("rolesInfo").getValue() == 1;

            Document roleDoc = new Document("role", "myRole").append("db", "mydb").append("isBuiltin", false).append("roles", Arrays.asList(new Document("role", "read").append("db", "mydb")));

            return new Document("roles", Arrays.asList(roleDoc)).append("ok", 1.0);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            try (ResultSet rs = stmt.executeQuery("show roles")) {
                assert rs.next();
                assert "myRole".equals(rs.getString("ROLE"));
                assert "mydb".equals(rs.getString("DB"));
                assert !rs.getBoolean("IS_BUILTIN");
                assert rs.getString("INHERITED_ROLES").contains("read");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void grant_roles_to_user_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());

            assert doc.getString("grantRolesToUser").getValue().equals("myUser");
            assert doc.getArray("roles").get(0).asString().getValue().equals("readWrite");

            return new Document("ok", 1.0);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.grantRolesToUser('myUser', ['readWrite'])");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void revoke_roles_from_user_0() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());

            assert doc.getString("revokeRolesFromUser").getValue().equals("myUser");
            assert doc.getArray("roles").get(0).asString().getValue().equals("readWrite");

            return new Document("ok", 1.0);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            int res = stmt.executeUpdate("db.revokeRolesFromUser('myUser', ['readWrite'])");
            assert res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }
}
