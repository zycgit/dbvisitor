 package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class MongoCommandParamsTest extends AbstractJdbcTest {

    @Test
    public void create_index_with_json_string() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            PowerMockito.when(mockColl.createIndex(any(Bson.class), any(IndexOptions.class))).thenAnswer(inv -> {
                Bson keys = inv.getArgument(0);
                IndexOptions opts = inv.getArgument(1);
                
                // Verify keys parsed correctly
                Document keyDoc = (Document) keys;
                assert keyDoc.getInteger("name") == 1;

                // Verify options parsed correctly
                assert "idx_json".equals(opts.getName());
                assert opts.isUnique();
                
                return "idx_json";
            });
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            // Pass JSON strings as arguments
            // Note: In SQL, strings are quoted with single quotes.
            // We need to escape the JSON string properly.
            // The parser handles string literals.
            // "db.mycol.createIndex('{ \"name\": 1 }', '{ \"name\": \"idx_json\", \"unique\": true }')"
            
            int res = stmt.executeUpdate("db.mycol.createIndex('{ \"name\": 1 }', '{ \"name\": \"idx_json\", \"unique\": true }')");
            assert res == 0;
        }
    }

    @Test
    public void drop_index_with_json_string() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, args) -> {
            MongoCollection mockColl = PowerMockito.mock(MongoCollection.class);
            // Mock dropIndex(Bson)
            PowerMockito.doAnswer(inv -> {
                Bson keys = inv.getArgument(0);
                Document keyDoc = (Document) keys;
                assert keyDoc.getInteger("name") == 1;
                return null;
            }).when(mockColl).dropIndex(any(Bson.class));
            
            return mockColl;
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            // Pass JSON string for key pattern
            int res = stmt.executeUpdate("db.mycol.dropIndex('{ \"name\": 1 }')");
            assert res == 0;
        }
    }

    @Test
    public void create_user_with_json_string() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Document command = (Document) args[0];
            if ("createUser".equals(command.getString("createUser"))) {
                assert "testUser".equals(command.getString("createUser"));
                assert "pwd123".equals(command.getString("pwd"));
                
                List<Document> roles = (List<Document>) command.get("roles");
                assert roles.size() == 1;
                assert "readWrite".equals(roles.get(0).getString("role"));
                assert "mydb".equals(roles.get(0).getString("db"));
                
                Map<String, Object> customData = (Map<String, Object>) command.get("customData");
                assert customData != null;
                assert "value".equals(customData.get("key"));
                
                return new Document("ok", 1.0);
            }
            return new Document("ok", 0.0);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            // db.createUser("testUser", "pwd123", '[{ "role": "readWrite", "db": "mydb" }]', '{ "customData": { "key": "value" } }')
            int res = stmt.executeUpdate("db.createUser('testUser', 'pwd123', '[{ \"role\": \"readWrite\", \"db\": \"mydb\" }]', '{ \"customData\": { \"key\": \"value\" } }')");
            assert res == 0;
        }
    }

    @Test
    public void run_command_with_json_string() throws SQLException {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("runCommand", (name, args) -> {
            Document command = (Document) args[0];
            if (command.containsKey("buildInfo")) {
                return new Document("ok", 1.0).append("version", "4.4.0");
            }
            return new Document("ok", 0.0);
        }));

        try (Connection conn = redisConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("use mydb");
            // db.runCommand('{ "buildInfo": 1 }')
            boolean res = stmt.execute("db.runCommand('{ \"buildInfo\": 1 }')");
            assert res;
        }
    }
}
