package net.hasor.dbvisitor.adapter.milvus.commands;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.milvus.grpc.LoadState;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.GetLoadStateResp;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import io.milvus.v2.service.database.request.AlterDatabasePropertiesReq;
import io.milvus.v2.service.database.request.CreateDatabaseReq;
import io.milvus.v2.service.database.response.ListDatabasesResp;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.index.request.DescribeIndexReq;
import io.milvus.v2.service.index.response.DescribeIndexResp;
import io.milvus.v2.service.partition.request.CreatePartitionReq;
import io.milvus.v2.service.partition.request.DropPartitionReq;
import io.milvus.v2.service.partition.request.LoadPartitionsReq;
import io.milvus.v2.service.rbac.request.*;
import io.milvus.v2.service.rbac.response.DescribeRoleResp;
import io.milvus.v2.service.utility.request.AlterAliasReq;
import io.milvus.v2.service.utility.request.CreateAliasReq;
import io.milvus.v2.service.utility.request.DropAliasReq;
import io.milvus.v2.service.utility.request.FlushReq;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;

public class MilvusV2AdminTest {
    private final List<Object> requests = new ArrayList<>();
    private final List<String> methods  = new ArrayList<>();

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            methods.add(method.getName());
            if (args.length > 0) {
                requests.add(args[0]);
            }
            switch (method.getName()) {
                case "getServerVersion":
                    return "v2.6.2";
                case "listDatabases":
                    return ListDatabasesResp.builder().databaseNames(Arrays.asList("default", "db1")).build();
                case "hasCollection":
                case "hasPartition":
                    return true;
                case "listCollectionsV2":
                    return ListCollectionsResp.builder().collectionNames(Arrays.asList("t", "other")).build();
                case "listPartitions":
                    return Arrays.asList("p", "other");
                case "listUsers":
                    return Arrays.asList("root", "u");
                case "listRoles":
                    return Arrays.asList("admin", "r");
                case "getLoadStateV2":
                    return GetLoadStateResp.builder().state(LoadState.LoadStateLoaded).progress(100L).build();
                case "describeIndex":
                    return DescribeIndexResp.builder().indexDescriptions(Collections.singletonList(DescribeIndexResp.IndexDesc.builder().fieldName("v").indexName("idx").id(12).indexType(IndexParam.IndexType.HNSW).metricType(IndexParam.MetricType.L2).extraParams(Collections.singletonMap("M", "16")).indexedRows(8).totalRows(10).build())).build();
                case "describeRole":
                    return DescribeRoleResp.builder().roleName("r").grantInfos(Collections.singletonList(DescribeRoleResp.GrantInfo.builder().roleName("r").dbName("db1").objectType("Collection").objectName("t").privilege("Search").build())).build();
                default:
                    return null;
            }
        });
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    private Connection connect() throws SQLException {
        Properties props = new Properties();
        props.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        props.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/db1", props);
    }

    private <T> T request(Class<T> type) {
        return requests.stream().filter(type::isInstance).map(type::cast).findFirst().orElseThrow(AssertionError::new);
    }

    @Test
    public void databaseAndCollectionCommandsUseCurrentCatalog() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE db2");
            statement.executeUpdate("ALTER DATABASE db1 SET PROPERTIES (replica_number=2)");
            statement.executeUpdate("DROP DATABASE db1");
            statement.executeUpdate("CREATE TABLE t (id INT64 PRIMARY KEY, v FLOAT_VECTOR(2))");
            statement.executeUpdate("ALTER TABLE t RENAME TO t2");
            statement.executeUpdate("DROP TABLE t2");
            try (ResultSet result = statement.executeQuery("SHOW DATABASES")) {
                assertTrue(result.next());
                assertEquals("default", result.getString("DATABASE"));
            }
            try (ResultSet result = statement.executeQuery("SHOW TABLES")) {
                assertTrue(result.next());
                assertEquals("t", result.getString("TABLE"));
            }
        }
        assertEquals("db2", request(CreateDatabaseReq.class).getDatabaseName());
        assertEquals("2", request(AlterDatabasePropertiesReq.class).getProperties().get("replica_number"));
        assertEquals("db1", request(CreateCollectionReq.class).getDatabaseName());
        assertEquals(Boolean.FALSE, request(CreateCollectionReq.class).getEnableDynamicField());
        assertEquals("t", request(RenameCollectionReq.class).getCollectionName());
        assertEquals("t2", request(RenameCollectionReq.class).getNewCollectionName());
        assertEquals("db1", request(DropCollectionReq.class).getDatabaseName());
        assertEquals("db1", request(ListCollectionsReq.class).getDatabaseName());
    }

    @Test
    public void partitionAndAliasCommandsKeepTheirResultShape() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE PARTITION p ON t");
            statement.executeUpdate("DROP PARTITION p ON t");
            statement.executeUpdate("CREATE ALIAS a FOR t");
            statement.executeUpdate("ALTER ALIAS a FOR t2");
            statement.executeUpdate("DROP ALIAS a");
            statement.executeUpdate("FLUSH t");
            try (ResultSet result = statement.executeQuery("SHOW PARTITION p ON t")) {
                assertTrue(result.next());
                assertEquals("p", result.getString("PARTITION"));
                assertFalse(result.next());
            }
            try (ResultSet result = statement.executeQuery("SHOW PARTITIONS FROM t")) {
                assertTrue(result.next());
                assertTrue(result.next());
                assertFalse(result.next());
            }
        }
        assertEquals("db1", request(CreatePartitionReq.class).getDatabaseName());
        assertEquals("p", request(DropPartitionReq.class).getPartitionName());
        assertEquals("db1", request(CreateAliasReq.class).getDatabaseName());
        assertEquals("t2", request(AlterAliasReq.class).getCollectionName());
        assertEquals("a", request(DropAliasReq.class).getAlias());
        assertEquals(Collections.singletonList("t"), request(FlushReq.class).getCollectionNames());
        assertEquals(Long.valueOf(60_000), request(FlushReq.class).getWaitFlushedTimeoutMs());
    }

    @Test
    public void indexOptionsAndProgressUseNativeV2Requests() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE INDEX idx ON t(v) USING HNSW WITH (metric_type=L2, M=16)");
            statement.executeUpdate("DROP INDEX idx ON t");
            try (ResultSet result = statement.executeQuery("SHOW INDEX idx ON t")) {
                assertTrue(result.next());
                assertEquals("idx", result.getString("INDEX"));
                assertEquals("v", result.getString("FIELD"));
                assertEquals(12L, result.getLong("ID"));
                assertTrue(result.getString("PARAMS").contains("M=16"));
            }
            try (ResultSet result = statement.executeQuery("SHOW PROGRESS OF INDEX idx ON t")) {
                assertTrue(result.next());
                assertEquals(10L, result.getLong("TOTAL"));
                assertEquals(8L, result.getLong("INDEXED"));
            }
        }
        assertEquals(Long.valueOf(600_000), request(CreateIndexReq.class).getTimeout());
        IndexParam index = request(CreateIndexReq.class).getIndexParams().get(0);
        assertEquals(IndexParam.IndexType.HNSW, index.getIndexType());
        assertEquals(IndexParam.MetricType.L2, index.getMetricType());
        assertEquals(16L, index.getExtraParams().get("M"));
        assertEquals("idx", request(DescribeIndexReq.class).getIndexName());
        assertEquals("db1", request(DescribeIndexReq.class).getDatabaseName());
    }

    @Test
    public void roleAndPrivilegeCommandsStayOnJdbc() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE USER u PASSWORD 'secret'");
            statement.executeUpdate("CREATE ROLE r");
            statement.executeUpdate("GRANT ROLE r TO u");
            statement.executeUpdate("REVOKE ROLE r FROM u");
            statement.executeUpdate("GRANT Search ON Collection t TO ROLE r");
            statement.executeUpdate("REVOKE Search ON Collection t FROM ROLE r");
            try (ResultSet result = statement.executeQuery("SHOW ROLES")) {
                assertTrue(result.next());
                assertEquals("admin", result.getString("ROLE"));
            }
            try (ResultSet result = statement.executeQuery("SHOW USERS")) {
                assertTrue(result.next());
                assertEquals("root", result.getString("USER"));
            }
            try (ResultSet result = statement.executeQuery("SHOW GRANTS FOR ROLE r")) {
                assertTrue(result.next());
                assertEquals("db1", result.getString("DATABASE"));
                assertEquals("Collection", result.getString("OBJECT"));
                assertEquals("Search", result.getString("PRIVILEGE"));
            }
            statement.executeUpdate("DROP USER u");
            statement.executeUpdate("DROP ROLE r");
        }
        assertEquals("u", request(CreateUserReq.class).getUserName());
        assertEquals("r", request(GrantRoleReq.class).getRoleName());
        assertEquals("u", request(RevokeRoleReq.class).getUserName());
        assertEquals("Collection", request(GrantPrivilegeReq.class).getObjectType());
        assertEquals("t", request(RevokePrivilegeReq.class).getObjectName());
    }

    @Test
    public void existingObjectsSkipConditionalCreateRequests() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS db1");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS t (id INT64 PRIMARY KEY, v FLOAT_VECTOR(2))");
            statement.executeUpdate("CREATE PARTITION IF NOT EXISTS p ON t");
        }
        assertTrue(methods.contains("listDatabases"));
        assertTrue(methods.contains("hasCollection"));
        assertTrue(methods.contains("hasPartition"));
        assertFalse(methods.contains("createDatabase"));
        assertFalse(methods.contains("createCollection"));
        assertFalse(methods.contains("createPartition"));
    }

    @Test
    public void missingDatabaseHonorsIfExistsAndNeverSendsDropRequest() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("DROP DATABASE IF EXISTS missing"));
            SQLException error = assertThrows(SQLException.class, () -> statement.executeUpdate("DROP DATABASE missing"));
            assertTrue(error.getMessage().contains("database not exists"));
        }
        assertFalse(methods.contains("dropDatabase"));
    }

    @Test
    public void administrativeListsHonorJdbcMaxRows() throws Exception {
        // @formatter:off
        String[][] commands = {
            { "SHOW DATABASES", "DATABASE", "default" },
            { "SHOW TABLES", "TABLE", "t" },
            { "SHOW PARTITIONS FROM t", "PARTITION", "p" },
            { "SHOW USERS", "USER", "root" }
        };
        // @formatter:on
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.setMaxRows(1);
            for (String[] command : commands) {
                try (ResultSet result = statement.executeQuery(command[0])) {
                    assertTrue(result.next());
                    assertEquals(command[2], result.getString(command[1]));
                    assertFalse(result.next());
                }
            }
        }
    }

    @Test
    public void administrativeFailureStopsLaterCommandsWithoutDisablingStatement() throws Exception {
        java.lang.reflect.InvocationHandler original = MilvusCommandInterceptor.getInterceptor(MilvusClientV2.class);
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("createAlias".equals(method.getName())) {
                methods.add(method.getName());
                throw new IllegalStateException("simulated alias failure");
            }
            return original.invoke(proxy, method, args);
        });
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            SQLException error = assertThrows(SQLException.class, () -> statement.execute("CREATE ALIAS a FOR t; DROP ALIAS a"));
            assertTrue(error.getMessage().contains("simulated alias failure"));
            assertTrue(methods.contains("createAlias"));
            assertFalse(methods.contains("dropAlias"));
            try (ResultSet result = statement.executeQuery("SHOW TABLES")) {
                assertTrue(result.next());
                assertEquals("t", result.getString("TABLE"));
            }
        }
    }

    @Test
    public void loadingHintsControlPollingRatherThanSdkBlocking() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("LOAD TABLE t PARTITION p");
            statement.executeUpdate("/*+ sync=false */ LOAD TABLE t");
            statement.executeUpdate("/*+ sync=false */ RELEASE TABLE t PARTITION p");
            statement.executeUpdate("/*+ sync=false */ RELEASE TABLE t");
            try (ResultSet result = statement.executeQuery("SHOW PROGRESS OF LOADING ON t PARTITION p")) {
                assertTrue(result.next());
                assertEquals(100L, result.getLong("PROGRESS"));
            }
        }
        assertEquals(Boolean.FALSE, request(LoadPartitionsReq.class).getSync());
        assertEquals(Boolean.FALSE, request(LoadCollectionReq.class).getSync());
        assertEquals("p", request(GetLoadStateReq.class).getPartitionName());
        assertEquals("db1", request(GetLoadStateReq.class).getDatabaseName());
        assertEquals(2L, methods.stream().filter("getLoadStateV2"::equals).count());
    }
}
