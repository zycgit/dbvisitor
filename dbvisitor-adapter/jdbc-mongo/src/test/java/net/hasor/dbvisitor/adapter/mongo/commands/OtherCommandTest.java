package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.mongodb.client.MongoDatabase;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;

public class OtherCommandTest extends AbstractJdbcTest {
    @Test
    public void runCommand_0() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
            assert doc.containsKey("ping");
            return new Document("ok", 1.0);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            ResultSet res = stmt.executeQuery("db.runCommand({ ping: 1 })");
            assert res.next();
            String json = res.getString("JSON");
            assert json.contains("ok");
            assert json.contains("1");
        }
    }

    @Test
    public void serverStatus_0() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
            assert doc.containsKey("serverStatus");
            return new Document("ok", 1.0).append("version", "4.4.0");
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            ResultSet res = stmt.executeQuery("db.serverStatus()");
            assert res.next();
            String json = res.getString("JSON");
            assert json.contains("4.4.0");
        }
    }

    @Test
    public void version_0() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
            assert doc.containsKey("buildInfo");
            return new Document("ok", 1.0).append("version", "5.0.0");
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            ResultSet res = stmt.executeQuery("db.version()");
            assert res.next();
            String json = res.getString("JSON");
            assert json.contains("5.0.0");
        }
    }

    @Test
    public void stats_0() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Bson command = (Bson) args[0];
            BsonDocument doc = command.toBsonDocument(BsonDocument.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
            assert doc.containsKey("dbStats");
            return new Document("ok", 1.0).append("db", "mydb");
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            ResultSet res = stmt.executeQuery("db.stats()");
            assert res.next();
            String json = res.getString("JSON");
            assert json.contains("mydb");
        }
    }
}
