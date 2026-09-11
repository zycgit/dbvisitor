package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import io.milvus.common.clientenum.FunctionType;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.utils.SchemaUtils;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusFunctionManagementTest {
    private final List<String>                         methods  = new ArrayList<>();
    private final List<Object>                         requests = new ArrayList<>();
    private       Connection                           connection;
    private       CreateCollectionReq.CollectionSchema schema;
    private       String                               failingMethod;

    @Before
    public void connect() throws SQLException {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if (method.getName().equals("getServerVersion")) {
                return "v2.6.2";
            }
            this.methods.add(method.getName());
            this.requests.add(args.length == 0 ? null : args[0]);
            if (method.getName().equals(this.failingMethod)) {
                throw new IllegalStateException("native function failure");
            }
            if (method.getName().equals("createCollection")) {
                this.schema = ((CreateCollectionReq) args[0]).getCollectionSchema();
            } else if (method.getName().equals("describeCollection")) {
                return DescribeCollectionResp.builder().collectionName("docs").collectionSchema(this.schema).build();
            }
            return null;
        });
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        this.connection = new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/db1", properties);
    }

    @After
    public void close() throws SQLException {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } finally {
            MilvusCommandInterceptor.resetInterceptor();
        }
    }

    @Test
    public void nativeMutationsBindDescriptionsAndParamsInSqlOrder() throws SQLException {
        String payload = "说明'); DROP TABLE docs; --";
        try (PreparedStatement statement = this.connection.prepareStatement("""
                /*+ trace=? */ ALTER TABLE docs ADD FUNCTION f USING BM25 (body) INTO (sparse) DESCRIPTION ?;
                ALTER TABLE docs ALTER FUNCTION f USING TEXTEMBEDDING (body) INTO (dense)
                    DESCRIPTION ? WITH (provider=?,description=?);
                ALTER TABLE docs DROP FUNCTION f;
                ALTER TABLE docs SET PROPERTIES (description=?)
                """)) {
            statement.setString(1, "hint");
            statement.setString(2, payload);
            statement.setString(3, "replacement");
            statement.setString(4, "openai");
            statement.setString(5, "function parameter, not schema description");
            statement.setString(6, "collection description");
            assertFalse(statement.execute());
            for (int i = 0; i < 4; i++) {
                assertEquals(0, statement.getUpdateCount());
                assertNull(statement.getResultSet());
                assertFalse(statement.getMoreResults());
            }
            assertEquals(-1, statement.getUpdateCount());
        }
        assertEquals(Arrays.asList("addCollectionFunction", "alterCollectionFunction", "dropCollectionFunction", "alterCollectionProperties"), this.methods);
        AddCollectionFunctionReq add = (AddCollectionFunctionReq) this.requests.get(0);
        assertEquals("db1", add.getDatabaseName());
        assertEquals("docs", add.getCollectionName());
        assertEquals("f", add.getFunction().getName());
        assertEquals(FunctionType.BM25, add.getFunction().getFunctionType());
        assertEquals(Collections.singletonList("body"), add.getFunction().getInputFieldNames());
        assertEquals(Collections.singletonList("sparse"), add.getFunction().getOutputFieldNames());
        assertEquals(payload, add.getFunction().getDescription());
        assertTrue(add.getFunction().getParams().isEmpty());
        AlterCollectionFunctionReq alter = (AlterCollectionFunctionReq) this.requests.get(1);
        assertEquals("db1", alter.getDatabaseName());
        assertEquals("docs", alter.getCollectionName());
        assertEquals("f", alter.getFunction().getName());
        assertEquals(FunctionType.TEXTEMBEDDING, alter.getFunction().getFunctionType());
        assertEquals(Collections.singletonList("dense"), alter.getFunction().getOutputFieldNames());
        assertEquals("replacement", alter.getFunction().getDescription());
        assertEquals("openai", alter.getFunction().getParams().get("provider"));
        assertEquals("function parameter, not schema description", alter.getFunction().getParams().get("description"));
        DropCollectionFunctionReq drop = (DropCollectionFunctionReq) this.requests.get(2);
        assertEquals("db1", drop.getDatabaseName());
        assertEquals("docs", drop.getCollectionName());
        assertEquals("f", drop.getFunctionName());
        assertEquals("collection description", ((AlterCollectionPropertiesReq) this.requests.get(3)).getProperties().get("description"));
    }

    @Test
    public void createAndShowCreateRoundTripFunctionDescriptionSeparatelyFromParams() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE docs (id INT64 PRIMARY KEY, body VARCHAR(100) WITH (enable_analyzer=true),
                    sparse SPARSE_FLOAT_VECTOR, FUNCTION description USING BM25 (body) INTO (sparse)
                    DESCRIPTION 'author''s 中文说明' WITH (description='opaque parameter'))
                    """);
            CreateCollectionReq.Function original = this.schema.getFunctionList().get(0);
            assertEquals("author's 中文说明", original.getDescription());
            assertEquals("opaque parameter", original.getParams().get("description"));
            String script;
            try (ResultSet rows = statement.executeQuery("SHOW CREATE TABLE docs")) {
                assertTrue(rows.next());
                script = rows.getString("CREATE SCRIPT");
            }
            assertTrue(script, script.contains("DESCRIPTION 'author''s 中文说明'"));
            statement.executeUpdate(script);
            assertEquals(SchemaUtils.convertToGrpcFunction(original), SchemaUtils.convertToGrpcFunction(this.schema.getFunctionList().get(0)));
        }
    }

    @Test
    public void boundControlCharactersAndBackslashesSurviveShowCreate() throws SQLException {
        String description = "line1\nline2\r\n\tpath\\'quoted'\\n末尾\\";
        try (PreparedStatement statement = this.connection.prepareStatement("""
                CREATE TABLE docs (id INT64 PRIMARY KEY, body VARCHAR(100) WITH (enable_analyzer=true),
                sparse SPARSE_FLOAT_VECTOR, FUNCTION f USING BM25 (body) INTO (sparse) DESCRIPTION ?)
                """)) {
            statement.setString(1, description);
            statement.executeUpdate();
        }
        try (Statement statement = this.connection.createStatement()) {
            String script;
            try (ResultSet rows = statement.executeQuery("SHOW CREATE TABLE docs")) {
                assertTrue(rows.next());
                script = rows.getString("CREATE SCRIPT");
            }
            statement.executeUpdate(script);
            assertEquals(description, this.schema.getFunctionList().get(0).getDescription());
        }
    }

    @Test
    public void invalidDefinitionsFailBeforeSdk() throws SQLException {
        String[] invalid = { "ALTER TABLE ? DROP FUNCTION f", "ALTER TABLE docs DROP FUNCTION ?", "ALTER TABLE docs ADD FUNCTION ? USING BM25 (body) INTO (sparse)", "ALTER TABLE docs ADD FUNCTION f USING ? (body) INTO (sparse)", "ALTER TABLE docs ADD FUNCTION f USING BM25 (?) INTO (sparse)", "ALTER TABLE docs ADD FUNCTION f USING BM25 (body) INTO (?)", "ALTER TABLE docs ADD FUNCTION f USING BM25 (body,other) INTO (sparse)", "ALTER TABLE docs ALTER FUNCTION f USING unknown (body) INTO (sparse)", "ALTER TABLE docs DROP FUNCTION f WITH (force=true)" };
        for (String sql : invalid) {
            try (Statement statement = this.connection.createStatement()) {
                assertThrows(sql, SQLException.class, () -> statement.execute(sql));
            }
        }
        for (Object description : Arrays.asList(null, 42)) {
            try (PreparedStatement statement = this.connection.prepareStatement("ALTER TABLE docs ADD FUNCTION f USING BM25 (body) INTO (sparse) DESCRIPTION ?")) {
                statement.setObject(1, description);
                assertThrows(SQLException.class, statement::execute);
            }
        }
        assertTrue(this.methods.isEmpty());
    }

    @Test
    public void nativeFailureStopsFollowingSqlWithoutFallback() throws SQLException {
        Map<String, String> commands = new LinkedHashMap<>();
        commands.put("addCollectionFunction", "ALTER TABLE docs ADD FUNCTION f USING BM25 (body) INTO (sparse)");
        commands.put("alterCollectionFunction", "ALTER TABLE docs ALTER FUNCTION f USING BM25 (body) INTO (sparse)");
        commands.put("dropCollectionFunction", "ALTER TABLE docs DROP FUNCTION f");
        try (Statement statement = this.connection.createStatement()) {
            for (Map.Entry<String, String> command : commands.entrySet()) {
                this.methods.clear();
                this.failingMethod = command.getKey();
                SQLException failure = assertThrows(SQLException.class, () -> statement.execute(command.getValue() + "; DROP TABLE docs"));
                assertTrue(failure.getMessage().contains("native function failure"));
                assertEquals(Collections.singletonList(command.getKey()), this.methods);
            }
            this.failingMethod = null;
            assertEquals(0, statement.executeUpdate("ALTER TABLE docs DROP FUNCTION recovered"));
        }
    }
}
