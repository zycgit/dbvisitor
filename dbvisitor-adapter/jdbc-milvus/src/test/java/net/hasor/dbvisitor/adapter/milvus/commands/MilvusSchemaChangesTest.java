package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.database.request.AlterDatabasePropertiesReq;
import io.milvus.v2.service.database.request.DropDatabasePropertiesReq;
import io.milvus.v2.service.index.request.AlterIndexPropertiesReq;
import io.milvus.v2.service.index.request.DropIndexPropertiesReq;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusSchemaChangesTest {
    private final List<Object> requests = new ArrayList<>();
    private       Connection   connection;

    @Before
    public void connect() throws SQLException {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("getServerVersion".equals(method.getName())) {
                return "v2.6.2";
            }
            if (args.length > 0) {
                requests.add(args[0]);
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

    private <T> T request(Class<T> type) {
        return this.requests.stream().filter(type::isInstance).map(type::cast).findFirst().orElseThrow(AssertionError::new);
    }

    @Test
    public void setPropertiesUsesNativeRequestsAndCatalog() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("ALTER DATABASE db2 SET PROPERTIES ('database.replica.number'=2)"));
            assertEquals(0, statement.executeUpdate("ALTER TABLE t SET PROPERTIES ('collection.ttl.seconds'=3600)"));
            assertEquals(0, statement.executeUpdate("ALTER TABLE t ALTER COLUMN name SET PROPERTIES (max_length=256)"));
            assertEquals(0, statement.executeUpdate("ALTER INDEX idx ON t SET PROPERTIES (mmap.enabled=true)"));
        }
        assertEquals("db2", request(AlterDatabasePropertiesReq.class).getDatabaseName());
        assertEquals("2", request(AlterDatabasePropertiesReq.class).getProperties().get("database.replica.number"));
        AlterCollectionPropertiesReq collection = request(AlterCollectionPropertiesReq.class);
        assertEquals("db1", collection.getDatabaseName());
        assertEquals("t", collection.getCollectionName());
        assertEquals(Collections.singletonMap("collection.ttl.seconds", "3600"), collection.getProperties());
        AlterCollectionFieldReq field = request(AlterCollectionFieldReq.class);
        assertEquals("db1", field.getDatabaseName());
        assertEquals("t", field.getCollectionName());
        assertEquals("name", field.getFieldName());
        assertEquals(Collections.singletonMap("max_length", "256"), field.getProperties());
        AlterIndexPropertiesReq index = request(AlterIndexPropertiesReq.class);
        assertEquals("db1", index.getDatabaseName());
        assertEquals("t", index.getCollectionName());
        assertEquals("idx", index.getIndexName());
        assertEquals(Collections.singletonMap("mmap.enabled", "true"), index.getProperties());
        assertEquals(4, this.requests.size());
    }

    @Test
    public void dropPropertiesRemovesOnlyNamedKeys() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("ALTER DATABASE db2 DROP PROPERTIES ('database.replica.number')");
            statement.executeUpdate("ALTER TABLE t DROP PROPERTIES ('collection.ttl.seconds', 'mmap.enabled')");
            statement.executeUpdate("ALTER TABLE t ALTER COLUMN name DROP PROPERTIES ('mmap.enabled')");
            statement.executeUpdate("ALTER INDEX idx ON TABLE t DROP PROPERTIES ('mmap.enabled')");
        }
        assertEquals("db2", request(DropDatabasePropertiesReq.class).getDatabaseName());
        assertEquals(Collections.singletonList("database.replica.number"), request(DropDatabasePropertiesReq.class).getPropertyKeys());
        DropCollectionPropertiesReq collection = request(DropCollectionPropertiesReq.class);
        assertEquals("db1", collection.getDatabaseName());
        assertEquals("t", collection.getCollectionName());
        assertEquals(Arrays.asList("collection.ttl.seconds", "mmap.enabled"), collection.getPropertyKeys());
        DropCollectionFieldPropertiesReq field = request(DropCollectionFieldPropertiesReq.class);
        assertEquals("db1", field.getDatabaseName());
        assertEquals("t", field.getCollectionName());
        assertEquals("name", field.getFieldName());
        assertEquals(Collections.singletonList("mmap.enabled"), field.getPropertyKeys());
        DropIndexPropertiesReq index = request(DropIndexPropertiesReq.class);
        assertEquals("db1", index.getDatabaseName());
        assertEquals("t", index.getCollectionName());
        assertEquals("idx", index.getIndexName());
        assertEquals(Collections.singletonList("mmap.enabled"), index.getPropertyKeys());
        assertEquals(4, this.requests.size());
    }

    @Test
    public void addNullableFieldPreservesDefaultAndAnalyzerOptions() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE t ADD COLUMN name VARCHAR(256) NULL DEFAULT 'unknown' WITH (enable_analyzer=true, enable_match=true)");
        }
        AddCollectionFieldReq field = request(AddCollectionFieldReq.class);
        assertEquals("db1", field.getDatabaseName());
        assertEquals("t", field.getCollectionName());
        assertEquals("name", field.getFieldName());
        assertEquals(DataType.VarChar, field.getDataType());
        assertEquals(Integer.valueOf(256), field.getMaxLength());
        assertTrue(field.getIsNullable());
        assertTrue(field.isEnableDefaultValue());
        assertEquals("unknown", field.getDefaultValue());
        assertTrue(field.getEnableAnalyzer());
        assertTrue(field.getEnableMatch());
        assertEquals(1, this.requests.size());
    }

    @Test
    public void addNullableArrayKeepsCapacityWithoutInventingDefault() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE t ADD tags ARRAY<VARCHAR(32)>(8) NULL");
        }
        AddCollectionFieldReq field = request(AddCollectionFieldReq.class);
        assertEquals(DataType.Array, field.getDataType());
        assertEquals(DataType.VarChar, field.getElementType());
        assertEquals(Integer.valueOf(8), field.getMaxCapacity());
        assertEquals(Integer.valueOf(32), field.getMaxLength());
        assertFalse(field.isEnableDefaultValue());
        assertNull(field.getDefaultValue());
    }

    @Test
    public void boundPropertyValueIsNotSqlAndRespectsStatementParameterOrder() throws SQLException {
        String value = "x'); DROP TABLE t; --";
        try (PreparedStatement statement = this.connection.prepareStatement("ALTER TABLE t SET PROPERTIES (description=?); ALTER INDEX idx ON t SET PROPERTIES (description=?)")) {
            statement.setString(1, value);
            statement.setObject(2, "second");
            assertFalse(statement.execute());
            assertEquals(0, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(0, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
        assertEquals(Collections.singletonMap("description", value), request(AlterCollectionPropertiesReq.class).getProperties());
        assertEquals(Collections.singletonMap("description", "second"), request(AlterIndexPropertiesReq.class).getProperties());
        assertEquals(2, this.requests.size());
    }

    @Test
    public void invalidChangesFailBeforeSdkMutation() throws SQLException {
        String[] statements = { "ALTER TABLE t ADD COLUMN name VARCHAR(64) NOT NULL", "ALTER TABLE t ADD COLUMN id INT64 PRIMARY KEY NULL", "ALTER TABLE t DROP PROPERTIES ('')", "ALTER TABLE t DROP PROPERTIES (?)" };
        try (Statement statement = this.connection.createStatement()) {
            for (String sql : statements) {
                assertThrows(sql, SQLException.class, () -> statement.executeUpdate(sql));
            }
        }
        try (PreparedStatement statement = this.connection.prepareStatement("ALTER TABLE t SET PROPERTIES (description=?)")) {
            statement.setObject(1, null);
            assertThrows(SQLException.class, statement::executeUpdate);
        }
        assertTrue(this.requests.isEmpty());
    }
}
