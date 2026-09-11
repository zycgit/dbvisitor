package net.hasor.dbvisitor.adapter.milvus.commands;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusCollectionKeysTest {
    private final List<CreateCollectionReq> requests = new ArrayList<>();
    private       boolean                   failCreate;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if (method.getName().equals("getServerVersion")) {
                return "v2.6.2";
            }
            if (method.getName().equals("createCollection")) {
                requests.add((CreateCollectionReq) args[0]);
                if (failCreate) {
                    throw new IllegalStateException("collection rejected by server");
                }
            }
            if (method.getName().equals("describeCollection")) {
                CreateCollectionReq created = requests.get(0);
                return DescribeCollectionResp.builder().collectionSchema(created.getCollectionSchema()).collectionName(created.getCollectionName()).consistencyLevel(created.getConsistencyLevel()).description(created.getDescription()).numOfPartitions(8L).shardsNum(created.getNumShards()).build();
            }
            return null;
        });
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    private Connection connect() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/db1", properties);
    }

    @Test
    public void keysAndCollectionOptionsShouldReachNativeSchemaAndRoundTrip() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE books (id INT64 PRIMARY KEY, tenant VARCHAR(64) PARTITION KEY,
                      age INT32 CLUSTERING KEY, v FLOAT_VECTOR(2))
                    WITH (num_partitions=8, num_shards=2, consistency_level=Strong, description='tenant''s data')
                    """);
            CreateCollectionReq created = requests.get(0);
            assertEquals("db1", created.getDatabaseName());
            assertEquals(Integer.valueOf(8), created.getNumPartitions());
            assertEquals(Integer.valueOf(2), created.getNumShards());
            assertEquals(ConsistencyLevel.STRONG, created.getConsistencyLevel());
            assertEquals("tenant's data", created.getDescription());
            assertTrue(created.getCollectionSchema().getFieldSchemaList().get(1).getIsPartitionKey());
            assertTrue(created.getCollectionSchema().getFieldSchemaList().get(2).getIsClusteringKey());
            try (ResultSet rows = statement.executeQuery("SHOW TABLE books")) {
                assertEquals(12, rows.getMetaData().getColumnCount());
                while (rows.next()) {
                    assertEquals(rows.getString("FIELD").equals("tenant"), rows.getBoolean("PARTITION_KEY"));
                    assertEquals(rows.getString("FIELD").equals("age"), rows.getBoolean("CLUSTERING_KEY"));
                }
            }
            String script;
            try (ResultSet rows = statement.executeQuery("SHOW CREATE TABLE books")) {
                assertTrue(rows.next());
                script = rows.getString("CREATE SCRIPT");
            }
            statement.executeUpdate(script);
            CreateCollectionReq restored = requests.get(1);
            assertEquals(created.getNumPartitions(), restored.getNumPartitions());
            assertEquals(created.getNumShards(), restored.getNumShards());
            assertEquals(created.getDescription(), restored.getDescription());
            assertTrue(restored.getCollectionSchema().getFieldSchemaList().get(1).getIsPartitionKey());
            assertTrue(restored.getCollectionSchema().getFieldSchemaList().get(2).getIsClusteringKey());
        }
    }

    @Test
    public void parametersShouldFollowFieldThenCollectionOrderAndRemainValues() throws SQLException {
        String text = "x'); DROP TABLE books; --";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("""
                CREATE TABLE books (id INT64 PRIMARY KEY, tenant INT64 PARTITION KEY CLUSTERING KEY,
                    body VARCHAR(64) WITH (enable_analyzer=?), v FLOAT_VECTOR(2))
                WITH (num_partitions=?, num_shards=?, description=?, consistency_level=?)
                """)) {
            statement.setBoolean(1, true);
            statement.setInt(2, 8);
            statement.setObject(3, 2);
            statement.setString(4, text);
            statement.setString(5, "Strong");
            assertEquals(0, statement.executeUpdate());
            statement.setObject(4, text, Types.VARCHAR);
            assertEquals(0, statement.executeUpdate());
        }
        assertEquals(2, requests.size());
        for (CreateCollectionReq created : requests) {
            assertTrue(created.getCollectionSchema().getFieldSchemaList().get(2).getEnableAnalyzer());
            assertEquals(text, created.getDescription());
            assertEquals(Integer.valueOf(8), created.getNumPartitions());
            assertEquals(Integer.valueOf(2), created.getNumShards());
            assertEquals(ConsistencyLevel.STRONG, created.getConsistencyLevel());
        }
    }

    @Test
    public void omittedOptionsShouldPreserveSdkDefaultsAndOrdinaryIdentifiers() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE clustering (id INT64 PRIMARY KEY, partition INT64, v FLOAT_VECTOR(2))");
        }
        CreateCollectionReq created = requests.get(0);
        assertNull(created.getNumPartitions());
        assertEquals(Integer.valueOf(1), created.getNumShards());
        assertEquals(ConsistencyLevel.BOUNDED, created.getConsistencyLevel());
        assertFalse(created.getEnableDynamicField());
        for (CreateCollectionReq.FieldSchema field : created.getCollectionSchema().getFieldSchemaList()) {
            assertFalse(field.getIsPartitionKey());
            assertFalse(field.getIsClusteringKey());
        }
    }

    @Test
    public void invalidOptionsAndUnsupportedAddedKeysShouldFailBeforeCreate() throws SQLException {
        String fields = "CREATE TABLE books (id INT64 PRIMARY KEY, tenant INT64 PARTITION KEY, v FLOAT_VECTOR(2)) WITH ";
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String options : Arrays.asList("(num_partitions=0)", "(num_partitions=1.5)", "(num_shards=2147483648)", "(num_shards='2')", "(num_partitions=true)", "(consistency_level='bad')", "(description=42)", "(unknown=1)", "(num_partitions=2,num_partitions=3)", "(num_shards=2,NUM_SHARDS=3)")) {
                assertThrows(options, SQLException.class, () -> statement.executeUpdate(fields + options));
            }
            for (String sql : Arrays.asList("CREATE TABLE t (id INT64 PRIMARY KEY, v FLOAT_VECTOR(2)) WITH (num_partitions=2)", "CREATE TABLE t (id INT64 PRIMARY KEY PARTITION KEY, v FLOAT_VECTOR(2))", "CREATE TABLE t (id INT64 PRIMARY KEY, tenant INT32 PARTITION KEY, v FLOAT_VECTOR(2))", "CREATE TABLE t (id INT64 PRIMARY KEY, tenant INT64 NULL PARTITION KEY, v FLOAT_VECTOR(2))", "CREATE TABLE t (id INT64 PRIMARY KEY, a INT64 PARTITION KEY, b INT64 PARTITION KEY, v FLOAT_VECTOR(2))", "CREATE TABLE t (id INT64 PRIMARY KEY, a INT64 CLUSTERING KEY, b INT64 CLUSTERING KEY, v FLOAT_VECTOR(2))",
                    "ALTER TABLE t ADD COLUMN tenant INT64 NULL PARTITION KEY", "ALTER TABLE t ADD COLUMN rank INT64 NULL CLUSTERING KEY")) {
                assertThrows(sql, SQLException.class, () -> statement.executeUpdate(sql));
            }
            try (PreparedStatement prepared = connection.prepareStatement(fields + "(num_partitions=?)")) {
                prepared.setNull(1, Types.INTEGER);
                assertThrows(SQLException.class, prepared::executeUpdate);
                prepared.setObject(1, Double.NaN);
                assertThrows(SQLException.class, prepared::executeUpdate);
            }
        }
        assertTrue(requests.isEmpty());
    }

    @Test
    public void nativeFailureShouldBeReportedWithoutRetryOrFallback() throws SQLException {
        failCreate = true;
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            SQLException error = assertThrows(SQLException.class, () -> statement.executeUpdate("CREATE TABLE books (id INT64 PRIMARY KEY, tenant INT64 CLUSTERING KEY, v FLOAT_VECTOR(2))"));
            assertTrue(error.getMessage().contains("collection rejected by server"));
        }
        assertEquals(1, requests.size());
    }
}
