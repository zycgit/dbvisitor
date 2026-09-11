/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import io.milvus.grpc.LoadState;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.CompactionPlan;
import io.milvus.v2.common.CompactionState;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.GetCollectionStatsResp;
import io.milvus.v2.service.collection.response.GetLoadStateResp;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import io.milvus.v2.service.database.request.AlterDatabasePropertiesReq;
import io.milvus.v2.service.database.request.CreateDatabaseReq;
import io.milvus.v2.service.database.request.DescribeDatabaseReq;
import io.milvus.v2.service.database.response.DescribeDatabaseResp;
import io.milvus.v2.service.database.response.ListDatabasesResp;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.index.request.DescribeIndexReq;
import io.milvus.v2.service.index.response.DescribeIndexResp;
import io.milvus.v2.service.partition.request.CreatePartitionReq;
import io.milvus.v2.service.partition.request.DropPartitionReq;
import io.milvus.v2.service.partition.request.GetPartitionStatsReq;
import io.milvus.v2.service.partition.request.LoadPartitionsReq;
import io.milvus.v2.service.partition.response.GetPartitionStatsResp;
import io.milvus.v2.service.rbac.request.*;
import io.milvus.v2.service.rbac.response.DescribeRoleResp;
import io.milvus.v2.service.rbac.response.DescribeUserResp;
import io.milvus.v2.service.resourcegroup.request.*;
import io.milvus.v2.service.resourcegroup.response.DescribeResourceGroupResp;
import io.milvus.v2.service.resourcegroup.response.ListResourceGroupsResp;
import io.milvus.v2.service.utility.request.*;
import io.milvus.v2.service.utility.response.*;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusV2AdminTest {
    private final List<Object> requests = new ArrayList<>();
    private final List<String> methods  = new ArrayList<>();
    private       boolean      failTransfers;
    private       boolean      failDescriptions;
    private       boolean      failScopedPrivileges;
    private       boolean      failPrincipalWrites;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            methods.add(method.getName());
            if (args.length > 0) {
                requests.add(args[0]);
            }
            switch (method.getName()) {
                case "createUser":
                case "createRole":
                case "dropRole":
                    if (failPrincipalWrites) {
                        throw new IllegalStateException("principal write rejected");
                    }
                    return null;
                case "grantPrivilegeV2":
                case "revokePrivilegeV2":
                    if (failScopedPrivileges) {
                        throw new IllegalStateException("scoped privilege rejected");
                    }
                    return null;
                case "updateUser":
                case "alterRole":
                    if (failDescriptions) {
                        throw new IllegalStateException("description update unsupported by server");
                    }
                    return null;
                case "transferNode":
                case "transferReplica":
                    if (failTransfers) {
                        throw new IllegalStateException("migration rejected");
                    }
                    return null;
                case "listResourceGroups":
                    return ListResourceGroupsResp.builder().groupNames(Arrays.asList("rg1", "rg2")).build();
                case "describeResourceGroup":
                    return DescribeResourceGroupResp.builder().groupName("rg1").capacity(8).numberOfAvailableNode(5).config(requests.stream().filter(CreateResourceGroupReq.class::isInstance).map(CreateResourceGroupReq.class::cast).map(CreateResourceGroupReq::getConfig).filter(Objects::nonNull).findFirst().orElse(null)).numberOfLoadedReplica(Collections.singletonMap("books", 2)).numberOfIncomingNode(Collections.singletonMap("rg2", 1)).numberOfOutgoingNode(Collections.singletonMap("rg2", 3)).build();
                case "describeUser":
                    return DescribeUserResp.builder().userName("u").roles(Arrays.asList("reader", "writer")).description("test user").build();
                case "compact":
                    return CompactResp.builder().compactionID(3_000_000_000L).build();
                case "getCompactionState":
                    return GetCompactionStateResp.builder().state(CompactionState.Executing).executingPlanNo(3L).completedPlanNo(2L).timeoutPlanNo(1L).build();
                case "getCompactionPlans":
                    return GetCompactionPlansResp.builder().state(CompactionState.Completed).plans(((GetCompactionPlansReq) args[0]).getCompactionID() == 0L ? Collections.emptyList() : Collections.singletonList(CompactionPlan.builder().target(8L).sources(Arrays.asList(6L, 7L)).build())).build();
                case "getServerVersion":
                    return "v2.6.2";
                case "listDatabases":
                    return ListDatabasesResp.builder().databaseNames(Arrays.asList("default", "db1")).build();
                case "describeDatabase":
                    return DescribeDatabaseResp.builder().databaseName(((DescribeDatabaseReq) args[0]).getDatabaseName()).properties(Collections.singletonMap("description", "quote\" and slash\\")).build();
                case "hasCollection":
                case "hasPartition":
                    return true;
                case "listCollectionsV2":
                    return ListCollectionsResp.builder().collectionNames(Arrays.asList("t", "other")).build();
                case "listPartitions":
                    return Arrays.asList("p", "other");
                case "listAliases":
                    String collection = ((ListAliasesReq) args[0]).getCollectionName();
                    return ListAliasResp.builder().collectionName(collection).alias("empty".equals(collection) ? Collections.emptyList() : Arrays.asList("a", "b")).build();
                case "describeAlias":
                    return DescribeAliasResp.builder().databaseName("db1").collectionName("t").alias("a").build();
                case "listUsers":
                    return Arrays.asList("root", "u");
                case "listRoles":
                    return Arrays.asList("admin", "r");
                case "getLoadStateV2":
                    return GetLoadStateResp.builder().state(LoadState.LoadStateLoaded).progress(100L).build();
                case "getCollectionStats":
                    return GetCollectionStatsResp.builder().numOfEntities(3_000_000_000L).stats(Collections.singletonMap("row_count", "3000000000")).build();
                case "getPartitionStats":
                    return GetPartitionStatsResp.builder().numOfEntities(12L).stats(Collections.singletonMap("row_count", "12")).build();
                case "describeIndex":
                    return DescribeIndexResp.builder().indexDescriptions(Collections.singletonList(DescribeIndexResp.IndexDesc.builder().fieldName("v").indexName("idx").id(12).indexType(IndexParam.IndexType.HNSW).metricType(IndexParam.MetricType.L2).extraParams(Collections.singletonMap("M", "16")).indexedRows(8).totalRows(10).build())).build();
                case "describeRole":
                    if ("empty".equals(((DescribeRoleReq) args[0]).getRoleName())) {
                        return DescribeRoleResp.builder().roleName("empty").description(null).build();
                    }
                    return DescribeRoleResp.builder().roleName("r").grantInfos(Collections.singletonList(DescribeRoleResp.GrantInfo.builder().dbName("db1").objectType("Collection").objectName("t").privilege("Search").build())).build();
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
    public void scopedPrivilegesPreserveExplicitDatabaseInsteadOfConnectionCatalog() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("GRANT PRIVILEGE Search ON DATABASE other_db TABLE books TO ROLE reader"));
            assertEquals(0, statement.executeUpdate("REVOKE PRIVILEGE Search ON DATABASE other_db TABLE books FROM ROLE reader"));
        }
        GrantPrivilegeReqV2 grant = request(GrantPrivilegeReqV2.class);
        assertEquals("other_db", grant.getDbName());
        assertEquals("books", grant.getCollectionName());
        assertEquals("reader", grant.getRoleName());
        assertEquals("Search", grant.getPrivilege());
        RevokePrivilegeReqV2 revoke = request(RevokePrivilegeReqV2.class);
        assertEquals(grant.getDbName(), revoke.getDbName());
        assertEquals(grant.getCollectionName(), revoke.getCollectionName());
        assertEquals(grant.getRoleName(), revoke.getRoleName());
        assertEquals(grant.getPrivilege(), revoke.getPrivilege());
        assertFalse(methods.contains("grantPrivilege"));
        assertFalse(methods.contains("revokePrivilege"));
    }

    @Test
    public void scopedPrivilegesRequireExplicitWildcards() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("GRANT PRIVILEGE CollectionReadOnly ON DATABASE * TABLE * TO ROLE reader"));
            assertEquals(0, statement.executeUpdate("REVOKE PRIVILEGE CollectionReadOnly ON DATABASE default TABLE * FROM ROLE reader"));
        }
        assertEquals("*", request(GrantPrivilegeReqV2.class).getDbName());
        assertEquals("*", request(GrantPrivilegeReqV2.class).getCollectionName());
        assertEquals("default", request(RevokePrivilegeReqV2.class).getDbName());
        assertEquals("*", request(RevokePrivilegeReqV2.class).getCollectionName());
    }

    @Test
    public void roleDetailWithExplicitDatabaseStaysOnRoleCommand() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String database : Arrays.asList("other_db", "*")) {
                try (ResultSet result = statement.executeQuery("SHOW ROLE r ON DATABASE " + database)) {
                    assertTrue(result.next());
                    assertEquals("r", result.getString("ROLE"));
                    assertNotNull(result.getString("GRANTS"));
                    assertFalse(result.next());
                }
            }
        }
        List<DescribeRoleReq> descriptions = requests.stream().filter(DescribeRoleReq.class::isInstance).map(DescribeRoleReq.class::cast).toList();
        assertEquals("other_db", descriptions.get(0).getDbName());
        assertEquals("*", descriptions.get(1).getDbName());
        assertFalse(methods.contains("describeDatabase"));
    }

    @Test
    public void incompleteScopedPrivilegesDoNotReachSdk() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeUpdate("GRANT PRIVILEGE Search ON DATABASE db1 TO ROLE reader"));
            assertThrows(SQLException.class, () -> statement.executeUpdate("REVOKE PRIVILEGE Search ON DATABASE db1 FROM ROLE reader"));
        }
        assertFalse(methods.contains("grantPrivilegeV2"));
        assertFalse(methods.contains("revokePrivilegeV2"));
    }

    @Test
    public void scopedPrivilegeFailuresDoNotFallBackToLegacyGrants() throws Exception {
        failScopedPrivileges = true;
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeUpdate("GRANT PRIVILEGE Search ON DATABASE db1 TABLE books TO ROLE reader"));
            assertThrows(SQLException.class, () -> statement.executeUpdate("REVOKE PRIVILEGE Search ON DATABASE db1 TABLE books FROM ROLE reader"));
        }
        assertEquals(1, Collections.frequency(methods, "grantPrivilegeV2"));
        assertEquals(1, Collections.frequency(methods, "revokePrivilegeV2"));
        assertFalse(methods.contains("grantPrivilege"));
        assertFalse(methods.contains("revokePrivilege"));
    }

    @Test
    public void roleDetailPreservesMetadataAndGrantScope() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SHOW ROLE r")) {
            assertEquals(3, result.getMetaData().getColumnCount());
            assertTrue(result.next());
            assertEquals("r", result.getString("ROLE"));
            assertEquals("", result.getString("DESCRIPTION"));
            com.google.gson.JsonObject grant = com.google.gson.JsonParser.parseString(result.getString("GRANTS")).getAsJsonArray().get(0).getAsJsonObject();
            assertEquals("db1", grant.get("dbName").getAsString());
            assertEquals("t", grant.get("objectName").getAsString());
            assertEquals("Search", grant.get("privilege").getAsString());
            assertFalse(result.next());
        }
        assertEquals("r", request(DescribeRoleReq.class).getRoleName());
        assertEquals("db1", request(DescribeRoleReq.class).getDbName());
    }

    @Test
    public void roleWithoutGrantsStillReturnsOneMetadataRow() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SHOW ROLE empty")) {
            assertTrue(result.next());
            assertEquals("empty", result.getString("ROLE"));
            assertNull(result.getString("DESCRIPTION"));
            assertTrue(result.wasNull());
            assertEquals("[]", result.getString("GRANTS"));
            assertFalse(result.next());
        }
    }

    @Test
    public void descriptionsUseDedicatedSdkRequestsWithoutPasswordChanges() throws Exception {
        String description = "用户说明 ' \" ; DROP USER admin; --";
        try (Connection connection = connect(); PreparedStatement user = connection.prepareStatement("ALTER USER u WITH (description=?)"); PreparedStatement role = connection.prepareStatement("ALTER ROLE r WITH (description=?)")) {
            user.setString(1, description);
            assertEquals(0, user.executeUpdate());
            role.setObject(1, description, Types.VARCHAR);
            assertEquals(0, role.executeUpdate());
        }
        assertEquals("u", request(UpdateUserReq.class).getUserName());
        assertEquals(description, request(UpdateUserReq.class).getDescription());
        assertEquals("r", request(AlterRoleReq.class).getRoleName());
        assertEquals(description, request(AlterRoleReq.class).getDescription());
        assertFalse(methods.contains("updatePassword"));
    }

    @Test
    public void creationDescriptionsAndForceDropUseTypedSdkFields() throws Exception {
        String description = "说明 '\" ; DROP ROLE admin; --";
        try (Connection connection = connect(); PreparedStatement user = connection.prepareStatement("CREATE USER new_user PASSWORD ? WITH (description=?)"); PreparedStatement role = connection.prepareStatement("CREATE ROLE new_role WITH (description=?)"); PreparedStatement drop = connection.prepareStatement("DROP ROLE r WITH (force_drop=?)")) {
            user.setString(1, "password'\\\"123");
            user.setString(2, description);
            assertEquals(0, user.executeUpdate());
            role.setObject(1, description, Types.VARCHAR);
            assertEquals(0, role.executeUpdate());
            drop.setBoolean(1, true);
            assertEquals(0, drop.executeUpdate());
        }
        assertEquals("password'\\\"123", request(CreateUserReq.class).getPassword());
        assertEquals(description, request(CreateUserReq.class).getDescription());
        assertEquals(description, request(CreateRoleReq.class).getDescription());
        assertTrue(request(DropRoleReq.class).isForceDrop());
        assertFalse(methods.contains("revokeRole"));
        assertFalse(methods.contains("revokePrivilege"));
        assertFalse(methods.contains("updateUser"));
        assertFalse(methods.contains("alterRole"));
    }

    @Test
    public void absentPrincipalOptionsRetainSdkDefaultsAndEmptyDescriptionsAreAllowed() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE USER new_user PASSWORD 'password123'");
            statement.executeUpdate("CREATE ROLE new_role WITH (description='')");
            statement.executeUpdate("DROP ROLE r");
        }
        assertEquals(CreateUserReq.builder().build().getDescription(), request(CreateUserReq.class).getDescription());
        assertEquals("", request(CreateRoleReq.class).getDescription());
        assertEquals(DropRoleReq.builder().build().isForceDrop(), request(DropRoleReq.class).isForceDrop());
    }

    @Test
    public void invalidPrincipalOptionsFailBeforeExistenceChecksOrWrites() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String sql : Arrays.asList("CREATE USER IF NOT EXISTS u PASSWORD 'password123'", "CREATE ROLE IF NOT EXISTS r")) {
                for (String option : Arrays.asList("description=null", "description=1", "description=true", "unknown='x'", "description='x', force_drop=true")) {
                    assertThrows(SQLException.class, () -> statement.executeUpdate(sql + " WITH (" + option + ")"));
                }
            }
            for (String option : Arrays.asList("force_drop=null", "force_drop=1", "force_drop='true'", "description='x'", "force_drop=true, unknown=false")) {
                assertThrows(SQLException.class, () -> statement.executeUpdate("DROP ROLE IF EXISTS missing WITH (" + option + ")"));
            }
        }
        assertFalse(methods.contains("listUsers"));
        assertFalse(methods.contains("listRoles"));
        assertFalse(methods.contains("createUser"));
        assertFalse(methods.contains("createRole"));
        assertFalse(methods.contains("dropRole"));
    }

    @Test
    public void skippedPrincipalCommandsDoNotShiftLaterStatementBindings() throws Exception {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("""
                CREATE USER IF NOT EXISTS u PASSWORD ? WITH (description=?);
                CREATE ROLE IF NOT EXISTS r WITH (description=?);
                DROP ROLE IF EXISTS missing WITH (force_drop=?);
                CREATE USER new_user PASSWORD ? WITH (description=?);
                CREATE ROLE new_role WITH (description=?);
                DROP ROLE r WITH (force_drop=?)
                """)) {
            statement.setString(1, "skippedPassword123");
            statement.setString(2, "skipped user");
            statement.setString(3, "skipped role");
            statement.setObject(4, true, Types.BOOLEAN);
            statement.setString(5, "actualPassword123");
            statement.setObject(6, "actual user");
            statement.setString(7, "actual role");
            statement.setObject(8, false);
            assertFalse(statement.execute());
            for (int i = 0; i < 6; i++) {
                assertEquals(0, statement.getUpdateCount());
                assertFalse(statement.getMoreResults());
            }
            assertEquals(-1, statement.getUpdateCount());
        }
        assertEquals("actualPassword123", request(CreateUserReq.class).getPassword());
        assertEquals("actual user", request(CreateUserReq.class).getDescription());
        assertEquals("actual role", request(CreateRoleReq.class).getDescription());
        assertFalse(request(DropRoleReq.class).isForceDrop());
        assertEquals(1, Collections.frequency(methods, "createUser"));
        assertEquals(1, Collections.frequency(methods, "createRole"));
        assertEquals(1, Collections.frequency(methods, "dropRole"));
    }

    @Test
    public void principalWriteFailuresPropagateWithoutFallbackAndStatementsRemainReusable() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            failPrincipalWrites = true;
            for (String sql : Arrays.asList("CREATE USER new_user PASSWORD 'password123' WITH (description='x')", "CREATE ROLE new_role WITH (description='x')", "DROP ROLE r WITH (force_drop=true)")) {
                assertThrows(SQLException.class, () -> statement.executeUpdate(sql));
            }
            failPrincipalWrites = false;
            assertEquals(0, statement.executeUpdate("DROP ROLE r WITH (force_drop=false)"));
        }
        assertEquals(1, Collections.frequency(methods, "createUser"));
        assertEquals(1, Collections.frequency(methods, "createRole"));
        assertEquals(2, Collections.frequency(methods, "dropRole"));
        assertFalse(methods.contains("revokePrivilege"));
        assertFalse(methods.contains("revokeRole"));
    }

    @Test
    public void descriptionUpdatesRejectInvalidOptionsBeforeSdkCalls() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String target : Arrays.asList("USER u", "ROLE r")) {
                for (String options : Arrays.asList("description=null", "description=1", "description=true", "unknown='x'", "description='x', reset_connection=true")) {
                    assertThrows(SQLException.class, () -> statement.executeUpdate("ALTER " + target + " WITH (" + options + ")"));
                }
            }
        }
        assertFalse(methods.contains("updateUser"));
        assertFalse(methods.contains("alterRole"));
    }

    @Test
    public void descriptionObjectBindingsValidateValuesAndAllowStatementReuse() throws Exception {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("ALTER USER u WITH (description=?)")) {
            for (Object invalid : Arrays.asList(null, 12, true)) {
                statement.setObject(1, invalid);
                assertThrows(SQLException.class, statement::executeUpdate);
            }
            assertFalse(methods.contains("updateUser"));
            statement.setObject(1, "bound description");
            assertEquals(0, statement.executeUpdate());
            assertEquals("bound description", request(UpdateUserReq.class).getDescription());
        }
    }

    @Test
    public void emptyDescriptionCanClearMetadata() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("ALTER USER u WITH (description='')"));
            assertEquals(0, statement.executeUpdate("ALTER ROLE r WITH (description='')"));
        }
        assertEquals("", request(UpdateUserReq.class).getDescription());
        assertEquals("", request(AlterRoleReq.class).getDescription());
    }

    @Test
    public void unsupportedDescriptionUpdatesDoNotFallBackToCredentialOrRoleMutation() throws Exception {
        failDescriptions = true;
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeUpdate("ALTER USER u WITH (description='x')"));
            assertThrows(SQLException.class, () -> statement.executeUpdate("ALTER ROLE r WITH (description='x')"));
        }
        assertEquals(1, Collections.frequency(methods, "updateUser"));
        assertEquals(1, Collections.frequency(methods, "alterRole"));
        assertFalse(methods.contains("updatePassword"));
        assertFalse(methods.contains("dropRole"));
        assertFalse(methods.contains("createRole"));
    }

    @Test
    public void transfersKeepNativeScopeAndCountWidths() throws Exception {
        try (Connection connection = connect()) {
            try (PreparedStatement statement = connection.prepareStatement("TRANSFER NODES ? FROM RESOURCE GROUP src TO RESOURCE GROUP dst")) {
                statement.setInt(1, 2);
                assertEquals(0, statement.executeUpdate());
            }
            try (Statement statement = connection.createStatement()) {
                assertEquals(0, statement.executeUpdate("TRANSFER REPLICAS 3000000000 OF TABLE t FROM RESOURCE GROUP src TO RESOURCE GROUP dst"));
            }
        }
        TransferNodeReq nodes = request(TransferNodeReq.class);
        assertEquals("src", nodes.getSourceGroupName());
        assertEquals("dst", nodes.getTargetGroupName());
        assertEquals(Integer.valueOf(2), nodes.getNumOfNodes());
        TransferReplicaReq replicas = request(TransferReplicaReq.class);
        assertEquals("src", replicas.getSourceGroupName());
        assertEquals("dst", replicas.getTargetGroupName());
        assertEquals("db1", replicas.getDatabaseName());
        assertEquals("t", replicas.getCollectionName());
        assertEquals(Long.valueOf(3_000_000_000L), replicas.getNumberOfReplicas());
        assertFalse(methods.contains("loadCollection"));
        assertFalse(methods.contains("createResourceGroup"));
    }

    @Test
    public void invalidTransferCountsFailBeforeSdkCalls() throws Exception {
        Object[] invalid = { null, 0, -1, "2", new java.math.BigDecimal("1.5"), new java.math.BigInteger("9223372036854775808") };
        try (Connection connection = connect()) {
            for (String operation : new String[] { "NODES ?", "REPLICAS ? OF TABLE t" }) {
                try (PreparedStatement statement = connection.prepareStatement("TRANSFER " + operation + " FROM RESOURCE GROUP src TO RESOURCE GROUP dst")) {
                    for (Object value : invalid) {
                        statement.setObject(1, value);
                        try {
                            statement.executeUpdate();
                            fail("Expected invalid count rejection");
                        } catch (SQLException expected) {
                            assertTrue(expected.getMessage().contains("TRANSFER"));
                        }
                    }
                }
            }
            try (Statement statement = connection.createStatement()) {
                try {
                    statement.executeUpdate("TRANSFER NODES 2147483648 FROM RESOURCE GROUP src TO RESOURCE GROUP dst");
                    fail("Expected SDK count range validation");
                } catch (SQLException expected) {
                    assertTrue(expected.getMessage().contains("INTEGER range"));
                }
            }
        }
        assertFalse(methods.contains("transferNode"));
        assertFalse(methods.contains("transferReplica"));
    }

    @Test
    public void transferFailureIsReportedWithoutAdditionalSchedulingCalls() throws Exception {
        failTransfers = true;
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            try {
                statement.executeUpdate("TRANSFER REPLICAS 1 OF t FROM RESOURCE GROUP src TO RESOURCE GROUP dst");
                fail("Expected native migration failure");
            } catch (SQLException expected) {
                assertTrue(expected.getMessage().contains("migration rejected"));
            }
        }
        assertEquals(1, Collections.frequency(methods, "transferReplica"));
        assertFalse(methods.contains("transferNode"));
        assertFalse(methods.contains("updateResourceGroups"));
    }

    @Test
    public void resourceGroupConfigMapsAllNativeFields() throws Exception {
        String config = """
                {"requests":{"nodeNum":1},"limits":{"nodeNum":3},
                 "transferFrom":[{"resourceGroup":"source"}],"transferTo":[{"resourceGroup":"target"}],
                 "nodeFilter":{"nodeLabels":[{"key":"zone","value":"east"}]}}
                """;
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("CREATE RESOURCE GROUP rg CONFIG ?")) {
            statement.setString(1, config);
            assertEquals(0, statement.executeUpdate());
        }
        io.milvus.common.resourcegroup.ResourceGroupConfig value = request(CreateResourceGroupReq.class).getConfig();
        assertEquals(Integer.valueOf(1), value.getRequests().getNodeNum());
        assertEquals(Integer.valueOf(3), value.getLimits().getNodeNum());
        assertEquals("source", value.getFrom().get(0).getResourceGroupName());
        assertEquals("target", value.getTo().get(0).getResourceGroupName());
        assertEquals("east", value.getNodeFilter().getNodeLabels().get("zone"));
        try (Connection connection = connect(); Statement show = connection.createStatement()) {
            String snapshot;
            try (ResultSet result = show.executeQuery("SHOW RESOURCE GROUP rg")) {
                assertTrue(result.next());
                snapshot = result.getString("CONFIG");
            }
            try (PreparedStatement update = connection.prepareStatement("ALTER RESOURCE GROUP rg CONFIG ?")) {
                update.setString(1, snapshot);
                assertEquals(0, update.executeUpdate());
            }
        }
        assertEquals(value.toGRPC(), request(UpdateResourceGroupsReq.class).getResourceGroups().get("rg").toGRPC());
    }

    @Test
    public void resourceGroupMultiUpdateUsesOneSdkCallAndDropIsNative() throws Exception {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("ALTER RESOURCE GROUPS CONFIG ?")) {
            statement.setObject(1, Map.of("rg1", Map.of("requests", Map.of("nodeNum", 0)), "rg2", Map.of("limits", Map.of("nodeNum", 2))));
            assertEquals(0, statement.executeUpdate());
            try (Statement drop = connection.createStatement()) {
                assertEquals(0, drop.executeUpdate("DROP RESOURCE GROUP rg1"));
            }
        }
        assertEquals(2, request(UpdateResourceGroupsReq.class).getResourceGroups().size());
        assertEquals(1, Collections.frequency(methods, "updateResourceGroups"));
        assertEquals("rg1", request(DropResourceGroupReq.class).getGroupName());
    }

    @Test
    public void invalidResourceGroupConfigNeverReachesSdk() throws Exception {
        String[] values = { "[]", "{\"unknown\":1}", "{\"requests\":{\"nodeNum\":1.5}}", "{\"requests\":{\"nodeNum\":4294967296}}" };
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("ALTER RESOURCE GROUP rg CONFIG ?")) {
            for (String value : values) {
                statement.setString(1, value);
                try {
                    statement.executeUpdate();
                    fail(value);
                } catch (SQLException expected) {
                    assertNotNull(expected.getMessage());
                }
            }
        }
        assertFalse(methods.contains("updateResourceGroups"));
    }

    @Test
    public void resourceGroupDefaultsAndInvalidMultiConfigDoNotInventWrites() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("CREATE RESOURCE GROUP rg"));
            assertNull(request(CreateResourceGroupReq.class).getConfig());
            try (PreparedStatement update = connection.prepareStatement("ALTER RESOURCE GROUPS CONFIG ?")) {
                update.setString(1, "{\"rg1\":{},\"rg2\":{\"unknown\":1}}");
                try {
                    update.executeUpdate();
                    fail("All configurations must be validated before update");
                } catch (SQLException expected) {
                    assertTrue(expected.getMessage().contains("CONFIG"));
                }
            }
        }
        assertFalse(methods.contains("updateResourceGroups"));
    }

    @Test
    public void resourceGroupListHonorsMaxRows() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.setMaxRows(1);
            try (ResultSet result = statement.executeQuery("SHOW RESOURCE GROUPS")) {
                assertEquals(Types.VARCHAR, result.getMetaData().getColumnType(1));
                assertTrue(result.next());
                assertEquals("rg1", result.getString("RESOURCE_GROUP"));
                assertFalse(result.next());
            }
        }
        assertFalse(methods.contains("describeResourceGroup"));
    }

    @Test
    public void resourceGroupDescriptionPreservesNativeMapsAndAbsentConfig() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SHOW RESOURCE GROUP rg1")) {
            assertEquals(8, result.getMetaData().getColumnCount());
            assertEquals(Types.INTEGER, result.getMetaData().getColumnType(2));
            assertTrue(result.next());
            assertEquals(8, result.getInt("CAPACITY"));
            assertEquals(5, result.getInt("AVAILABLE_NODES"));
            assertEquals("{\"books\":2}", result.getString("LOADED_REPLICAS"));
            assertEquals("{\"rg2\":1}", result.getString("INCOMING_NODES"));
            assertEquals("{\"rg2\":3}", result.getString("OUTGOING_NODES"));
            assertNull(result.getString("CONFIG"));
            assertTrue(result.wasNull());
            assertEquals("[]", result.getString("NODES"));
            assertFalse(result.next());
        }
        assertEquals("rg1", request(DescribeResourceGroupReq.class).getGroupName());
    }

    @Test
    public void passwordBindingsReachSdkWithoutSqlConcatenation() throws Exception {
        String oldPassword = "old'\\\"; DROP USER u; --";
        String newPassword = "new\"\\'password";
        try (Connection connection = connect()) {
            try (PreparedStatement statement = connection.prepareStatement("CREATE USER u PASSWORD ?")) {
                statement.setString(1, oldPassword);
                assertEquals(0, statement.executeUpdate());
            }
            try (PreparedStatement statement = connection.prepareStatement("ALTER USER u PASSWORD ? REPLACE ? WITH (reset_connection=?, description=?)")) {
                statement.setObject(1, newPassword, Types.VARCHAR);
                statement.setString(2, oldPassword);
                statement.setBoolean(3, true);
                statement.setString(4, "description'\\\"");
                assertEquals(0, statement.executeUpdate());
            }
        }
        assertEquals(oldPassword, request(CreateUserReq.class).getPassword());
        UpdatePasswordReq update = request(UpdatePasswordReq.class);
        assertEquals("u", update.getUserName());
        assertEquals(oldPassword, update.getPassword());
        assertEquals(newPassword, update.getNewPassword());
        assertEquals(Boolean.TRUE, update.getResetConnection());
        assertEquals("description'\\\"", update.getDescription());
        assertFalse(methods.contains("dropUser"));
    }

    @Test
    public void passwordUpdateDefaultsDoNotSwitchConnectionIdentity() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("ALTER USER u PASSWORD 'new-value' REPLACE 'old-value'"));
        }
        assertEquals(Boolean.FALSE, request(UpdatePasswordReq.class).getResetConnection());
        assertEquals("old-value", request(UpdatePasswordReq.class).getPassword());
        assertEquals("new-value", request(UpdatePasswordReq.class).getNewPassword());
    }

    @Test
    public void userDescriptionReturnsOneRowWithRolesJson() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SHOW USER u")) {
            assertEquals(3, result.getMetaData().getColumnCount());
            assertTrue(result.next());
            assertEquals("u", result.getString("USER"));
            assertEquals("[\"reader\",\"writer\"]", result.getString("ROLES"));
            assertEquals("test user", result.getString("DESCRIPTION"));
            assertFalse(result.next());
        }
        assertEquals("u", request(DescribeUserReq.class).getUserName());
    }

    @Test
    public void invalidPasswordValuesDoNotCallSdk() throws Exception {
        try (Connection connection = connect()) {
            for (Object value : new Object[] { null, 123 }) {
                try (PreparedStatement statement = connection.prepareStatement("CREATE USER u PASSWORD ?")) {
                    statement.setObject(1, value);
                    try {
                        statement.executeUpdate();
                        fail("Expected password type validation");
                    } catch (SQLException expected) {
                        assertTrue(expected.getMessage().contains("non-null string"));
                    }
                }
            }
            try (Statement statement = connection.createStatement()) {
                for (String option : new String[] { "reset_connection='true'", "description=123", "unknown=true" }) {
                    try {
                        statement.execute("ALTER USER u PASSWORD 'new' REPLACE 'old' WITH (" + option + ")");
                        fail(option);
                    } catch (SQLException expected) {
                        assertNotNull(expected.getMessage());
                    }
                }
            }
        }
        assertFalse(methods.contains("createUser"));
        assertFalse(methods.contains("updatePassword"));
    }

    @Test
    public void compactionSubmissionReturnsTaskIdAndMapsOptions() throws Exception {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("COMPACT TABLE t WITH (is_clustering=?, is_l0=?, target_size=?)")) {
            statement.setBoolean(1, true);
            statement.setBoolean(2, false);
            statement.setLong(3, 512L);
            try (ResultSet result = statement.executeQuery()) {
                assertEquals(Types.BIGINT, result.getMetaData().getColumnType(1));
                assertTrue(result.next());
                assertEquals(3_000_000_000L, result.getLong("COMPACTION_ID"));
                assertFalse(result.next());
            }
        }
        CompactReq compact = request(CompactReq.class);
        assertEquals("db1", compact.getDatabaseName());
        assertEquals("t", compact.getCollectionName());
        assertEquals(Boolean.TRUE, compact.getIsClustering());
        assertEquals(Boolean.FALSE, compact.getIsL0());
        assertEquals(Long.valueOf(512), compact.getTargetSize());
        assertEquals(1, Collections.frequency(methods, "compact"));
        assertFalse(methods.contains("getCompactionState"));
    }

    @Test
    public void compactionStatusAndPlansUseLongTaskIds() throws Exception {
        try (Connection connection = connect()) {
            try (PreparedStatement statement = connection.prepareStatement("SHOW COMPACTION ?")) {
                statement.setLong(1, 3_000_000_000L);
                try (ResultSet result = statement.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals("Executing", result.getString("STATE"));
                    assertEquals(3L, result.getLong("EXECUTING_PLANS"));
                    assertEquals(2L, result.getLong("COMPLETED_PLANS"));
                    assertEquals(1L, result.getLong("TIMEOUT_PLANS"));
                    assertFalse(result.next());
                }
            }
            try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SHOW COMPACTION PLANS 3000000000")) {
                assertTrue(result.next());
                assertEquals("Completed", result.getString("STATE"));
                assertEquals(com.google.gson.JsonParser.parseString("[{\"sources\":[6,7],\"target\":8}]"), com.google.gson.JsonParser.parseString(result.getString("PLANS")));
                assertFalse(result.next());
            }
        }
        assertEquals(Long.valueOf(3_000_000_000L), request(GetCompactionStateReq.class).getCompactionID());
        assertEquals(Long.valueOf(3_000_000_000L), request(GetCompactionPlansReq.class).getCompactionID());
    }

    @Test
    public void compactionDefaultsAndEmptyPlansPreserveSdkSemantics() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("COMPACT t")) {
                assertTrue(result.next());
            }
            CompactReq compact = request(CompactReq.class);
            assertEquals(Boolean.FALSE, compact.getIsClustering());
            assertEquals(Boolean.FALSE, compact.getIsL0());
            assertNull(compact.getTargetSize());
            assertFalse(methods.contains("flush"));
            try (ResultSet result = statement.executeQuery("SHOW COMPACTION PLANS 0")) {
                assertTrue(result.next());
                assertEquals("Completed", result.getString("STATE"));
                assertEquals("[]", result.getString("PLANS"));
                assertFalse(result.next());
            }
        }
    }

    @Test
    public void compactionRejectsInvalidOptionsWithoutSubmission() throws Exception {
        String[] options = { "is_l0='true'", "is_clustering=1", "target_size=0", "target_size=1.5", "target_size=9223372036854775808", "unknown=1" };
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String option : options) {
                try {
                    statement.execute("COMPACT t WITH (" + option + ")");
                    fail(option);
                } catch (SQLException expected) {
                    assertNotNull(expected.getMessage());
                }
            }
        }
        assertFalse(methods.contains("compact"));
    }

    @Test
    public void aliasQueriesExposeNativeNamesAndRespectJdbcMaxRows() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.setMaxRows(1);
            try (ResultSet result = statement.executeQuery("SHOW ALIASES FROM TABLE t")) {
                assertEquals(2, result.getMetaData().getColumnCount());
                assertTrue(result.next());
                assertEquals("a", result.getString("ALIAS"));
                assertEquals("t", result.getString("TABLE"));
                assertFalse(result.next());
            }
            try (ResultSet result = statement.executeQuery("SHOW ALIAS a")) {
                assertEquals(3, result.getMetaData().getColumnCount());
                assertTrue(result.next());
                assertEquals("db1", result.getString("DATABASE"));
                assertEquals("a", result.getString("ALIAS"));
                assertEquals("t", result.getString("TABLE"));
                assertFalse(result.next());
            }
        }
        assertEquals("db1", request(ListAliasesReq.class).getDatabaseName());
        assertEquals("t", request(ListAliasesReq.class).getCollectionName());
        assertEquals("db1", request(DescribeAliasReq.class).getDatabaseName());
        assertEquals("a", request(DescribeAliasReq.class).getAlias());
    }

    @Test
    public void emptyAliasListRetainsResultMetadata() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SHOW ALIASES FROM empty")) {
            assertEquals(2, result.getMetaData().getColumnCount());
            assertEquals("ALIAS", result.getMetaData().getColumnLabel(1));
            assertEquals("TABLE", result.getMetaData().getColumnLabel(2));
            assertFalse(result.next());
        }
    }

    @Test
    public void storageStatisticsUseNativeRequestsWithoutFlushOrCount() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("SHOW STATS FROM t")) {
                assertTrue(result.next());
                assertEquals(java.sql.Types.BIGINT, result.getMetaData().getColumnType(1));
                assertEquals(3_000_000_000L, result.getLong("NUM_ENTITIES"));
                assertEquals("3000000000", com.google.gson.JsonParser.parseString(result.getString("STATS")).getAsJsonObject().get("row_count").getAsString());
                assertFalse(result.next());
            }
            try (ResultSet result = statement.executeQuery("SHOW STATS FROM TABLE t PARTITION p")) {
                assertTrue(result.next());
                assertEquals(12L, result.getLong("NUM_ENTITIES"));
                assertEquals("12", com.google.gson.JsonParser.parseString(result.getString("STATS")).getAsJsonObject().get("row_count").getAsString());
                assertFalse(result.next());
            }
        }
        assertEquals("db1", request(GetCollectionStatsReq.class).getDatabaseName());
        assertEquals("t", request(GetCollectionStatsReq.class).getCollectionName());
        GetPartitionStatsReq partition = request(GetPartitionStatsReq.class);
        assertEquals("db1", partition.getDatabaseName());
        assertEquals("t", partition.getCollectionName());
        assertEquals("p", partition.getPartitionName());
        assertEquals(2, requests.size());
        assertFalse(methods.contains("flush"));
        assertFalse(methods.contains("query"));
        assertFalse(methods.contains("queryIterator"));
    }

    @Test
    public void defaultDatabaseNameIsNotMistakenForFieldDefaultSyntax() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SHOW DATABASE default")) {
            assertTrue(result.next());
            assertEquals("default", result.getString("DATABASE"));
            assertFalse(result.next());
        }
        assertEquals("default", request(DescribeDatabaseReq.class).getDatabaseName());
    }

    @Test
    public void databasePropertiesAreBoundAndDescriptionIsReadableThroughJdbc() throws Exception {
        try (Connection connection = connect()) {
            try (java.sql.PreparedStatement statement = connection.prepareStatement("CREATE DATABASE db2 WITH (description=?, 'database.replica.number'=?)")) {
                statement.setString(1, "'); DROP DATABASE db1; --");
                statement.setInt(2, 1);
                assertEquals(0, statement.executeUpdate());
            }
            try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SHOW DATABASE db2")) {
                assertEquals(2, result.getMetaData().getColumnCount());
                assertTrue(result.next());
                assertEquals("db2", result.getString("DATABASE"));
                assertEquals("quote\" and slash\\", com.google.gson.JsonParser.parseString(result.getString("PROPERTIES")).getAsJsonObject().get("description").getAsString());
                assertFalse(result.next());
            }
        }
        assertEquals("db2", request(CreateDatabaseReq.class).getDatabaseName());
        assertEquals("'); DROP DATABASE db1; --", request(CreateDatabaseReq.class).getProperties().get("description"));
        assertEquals("1", request(CreateDatabaseReq.class).getProperties().get("database.replica.number"));
        assertEquals("db2", request(DescribeDatabaseReq.class).getDatabaseName());
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
                assertEquals("r", result.getString("ROLE"));
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
        assertEquals(Boolean.TRUE, request(LoadPartitionsReq.class).getSync());
        assertEquals(Boolean.FALSE, request(LoadCollectionReq.class).getSync());
        assertEquals("p", request(GetLoadStateReq.class).getPartitionName());
        assertEquals("db1", request(GetLoadStateReq.class).getDatabaseName());
        assertEquals(1L, methods.stream().filter("getLoadStateV2"::equals).count());
    }
}
