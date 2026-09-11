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
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.utility.request.GetPersistentSegmentInfoReq;
import io.milvus.v2.service.utility.request.GetQuerySegmentInfoReq;
import io.milvus.v2.service.utility.response.CheckHealthResp;
import io.milvus.v2.service.utility.response.GetPersistentSegmentInfoResp;
import io.milvus.v2.service.utility.response.GetPersistentSegmentInfoResp.PersistentSegmentInfo;
import io.milvus.v2.service.utility.response.GetQuerySegmentInfoResp;
import io.milvus.v2.service.utility.response.GetQuerySegmentInfoResp.QuerySegmentInfo;
import io.milvus.v2.service.utility.response.GetServerVersionResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusDiagnosticsTest {
    private final List<String>                 methods    = new ArrayList<>();
    private final List<Object>                 requests   = new ArrayList<>();
    private       CheckHealthResp              health     = CheckHealthResp.builder().isHealthy(true).build();
    private       GetPersistentSegmentInfoResp persistent = GetPersistentSegmentInfoResp.builder().build();
    private       GetQuerySegmentInfoResp      query      = GetQuerySegmentInfoResp.builder().build();
    private       boolean                      fail;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if (method.getName().equals("getServerVersion")) {
                return "v2.6.2";
            }
            methods.add(method.getName());
            if (args != null && args.length > 0) {
                requests.add(args[0]);
            }
            if (fail) {
                throw new IllegalStateException("diagnostics unavailable");
            }
            switch (method.getName()) {
                case "checkHealth":
                    return health;
                case "getServerVersionV2":
                    return GetServerVersionResp.builder().version("v2.6.2").gitCommit("abc123").build();
                case "getPersistentSegmentInfo":
                    return persistent;
                case "getQuerySegmentInfo":
                    return query;
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
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/diagnostics_db", properties);
    }

    @Test
    public void healthAndVersionShouldUseIndependentJdbcResultsAndKeepUnhealthyDetails() throws SQLException {
        health = CheckHealthResp.builder().isHealthy(false).reasons(Arrays.asList("node \"one\" unavailable", "quota\nlimited")).quotaStates(Collections.singletonList("DenyToWrite")).build();
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertTrue(statement.execute("SHOW HEALTH; SHOW VERSION"));
            try (ResultSet rows = statement.getResultSet()) {
                assertTrue(rows.next());
                assertFalse(rows.getBoolean("IS_HEALTHY"));
                assertFalse(rows.wasNull());
                assertEquals(Types.BOOLEAN, rows.getMetaData().getColumnType(1));
                assertEquals("[\"node \\\"one\\\" unavailable\",\"quota\\nlimited\"]", rows.getString("REASONS"));
                assertEquals("[\"DenyToWrite\"]", rows.getString("QUOTA_STATES"));
                assertFalse(rows.next());
            }
            assertTrue(statement.getMoreResults());
            try (ResultSet rows = statement.getResultSet()) {
                assertTrue(rows.next());
                assertEquals(5, rows.getMetaData().getColumnCount());
                assertEquals("v2.6.2", rows.getString("VERSION"));
                assertEquals("abc123", rows.getString("GIT_COMMIT"));
                assertNull(rows.getString("BUILD_TIME"));
                assertTrue(rows.wasNull());
                assertFalse(rows.next());
            }
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
        assertEquals(Arrays.asList("checkHealth", "getServerVersionV2"), methods);
    }

    @Test
    public void segmentColumnsShouldPreserveAllNativeFieldsAndJdbcMaxRows() throws SQLException {
        persistent = GetPersistentSegmentInfoResp.builder().segmentInfos(Arrays.asList(PersistentSegmentInfo.builder().segmentID(9007199254740993L).collectionID(20L).partitionID(30L).collectionName("health").numOfRows(40L).state("Flushed").level("L1").storageVersion(2L).isSorted(true).build(), PersistentSegmentInfo.builder().segmentID(2L).build())).build();
        query = GetQuerySegmentInfoResp.builder().segmentInfos(Collections.singletonList(QuerySegmentInfo.builder().segmentID(10L).collectionID(20L).partitionID(30L).memSize(4096L).numOfRows(40L).indexName("idx").indexID(50L).state("Sealed").level("L1").nodeIDs(Arrays.asList(60L, 70L)).storageVersion(2L).isSorted(false).build())).build();
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.setMaxRows(1);
            try (ResultSet rows = statement.executeQuery("SHOW PERSISTENT SEGMENTS FROM TABLE health")) {
                assertTrue(rows.next());
                assertEquals(9, rows.getMetaData().getColumnCount());
                assertEquals(Types.BIGINT, rows.getMetaData().getColumnType(1));
                assertEquals(9007199254740993L, rows.getLong("SEGMENT_ID"));
                assertEquals(20L, rows.getLong("COLLECTION_ID"));
                assertEquals(30L, rows.getLong("PARTITION_ID"));
                assertEquals("health", rows.getString("COLLECTION_NAME"));
                assertEquals(40L, rows.getLong("NUM_ROWS"));
                assertEquals("Flushed", rows.getString("STATE"));
                assertEquals("L1", rows.getString("LEVEL"));
                assertEquals(2L, rows.getLong("STORAGE_VERSION"));
                assertTrue(rows.getBoolean("IS_SORTED"));
                assertFalse(rows.next());
            }
            try (ResultSet rows = statement.executeQuery("SHOW QUERY SEGMENTS FROM persistent")) {
                assertTrue(rows.next());
                assertEquals(12, rows.getMetaData().getColumnCount());
                assertEquals(10L, rows.getLong("SEGMENT_ID"));
                assertEquals(20L, rows.getLong("COLLECTION_ID"));
                assertEquals(30L, rows.getLong("PARTITION_ID"));
                assertEquals(4096L, rows.getLong("MEM_SIZE"));
                assertEquals(40L, rows.getLong("NUM_ROWS"));
                assertEquals("idx", rows.getString("INDEX_NAME"));
                assertEquals(50L, rows.getLong("INDEX_ID"));
                assertEquals("Sealed", rows.getString("STATE"));
                assertEquals("L1", rows.getString("LEVEL"));
                assertEquals("[60,70]", rows.getString("NODE_IDS"));
                assertEquals(2L, rows.getLong("STORAGE_VERSION"));
                assertFalse(rows.getBoolean("IS_SORTED"));
                assertFalse(rows.next());
            }
        }
        GetPersistentSegmentInfoReq first = (GetPersistentSegmentInfoReq) requests.get(0);
        assertEquals("diagnostics_db", first.getDatabaseName());
        assertEquals("health", first.getCollectionName());
        GetQuerySegmentInfoReq second = (GetQuerySegmentInfoReq) requests.get(1);
        assertEquals("diagnostics_db", second.getDatabaseName());
        assertEquals("persistent", second.getCollectionName());
        assertEquals(Arrays.asList("getPersistentSegmentInfo", "getQuerySegmentInfo"), methods);
    }

    @Test
    public void emptySnapshotsShouldKeepMetadataAndNullableFieldsShouldStayNull() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String kind : Arrays.asList("PERSISTENT", "QUERY")) {
                try (ResultSet rows = statement.executeQuery("SHOW " + kind + " SEGMENTS FROM segments")) {
                    assertEquals(kind.equals("PERSISTENT") ? 9 : 12, rows.getMetaData().getColumnCount());
                    assertFalse(rows.next());
                }
            }
            query = GetQuerySegmentInfoResp.builder().segmentInfos(Collections.singletonList(QuerySegmentInfo.builder().nodeIDs(null).build())).build();
            try (ResultSet rows = statement.executeQuery("SHOW QUERY SEGMENTS FROM segments")) {
                assertTrue(rows.next());
                assertEquals(0L, rows.getLong("MEM_SIZE"));
                assertTrue(rows.wasNull());
                assertNull(rows.getString("NODE_IDS"));
                assertTrue(rows.wasNull());
                assertFalse(rows.getBoolean("IS_SORTED"));
                assertTrue(rows.wasNull());
            }
            try (ResultSet rows = statement.executeQuery("SHOW HEALTH")) {
                assertTrue(rows.next());
                assertTrue(rows.getBoolean("IS_HEALTHY"));
                assertEquals("[]", rows.getString("REASONS"));
            }
        }
    }

    @Test
    public void diagnosticsShouldRejectMalformedSqlAndBoundCollectionNames() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String sql : Arrays.asList("SHOW HEALTH FROM books", "SHOW VERSION ?", "SHOW SEGMENTS FROM books", "SHOW QUERY SEGMENTS", "SHOW PERSISTENT SEGMENTS FROM ?", "SHOW QUERY SEGMENTS FROM books LIMIT 1")) {
                assertThrows(sql, SQLException.class, () -> statement.executeQuery(sql));
            }
        }
        assertTrue(methods.isEmpty());
    }

    @Test
    public void nativeErrorsShouldRemainErrorsWithoutMutationOrEntityScan() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            fail = true;
            SQLException error = assertThrows(SQLException.class, () -> statement.executeQuery("SHOW HEALTH"));
            assertTrue(error.getMessage().contains("diagnostics unavailable"));
        }
        assertEquals(Collections.singletonList("checkHealth"), methods);
    }
}
