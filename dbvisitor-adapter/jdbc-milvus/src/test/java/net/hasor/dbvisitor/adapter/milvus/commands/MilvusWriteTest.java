package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import com.google.gson.JsonObject;
import io.milvus.grpc.CollectionSchema;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.FieldSchema;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Test;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.*;

public class MilvusWriteTest extends AbstractJdbcTest {
    private final List<List<JsonObject>> pages  = new ArrayList<>();
    private       boolean                stringKey;
    private       boolean                autoId = true;
    private       int                    failPage;

    private Connection connect() throws SQLException {
        MilvusCommandInterceptor.resetInterceptor();
        pages.clear();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("describeCollection".equals(method.getName())) {
                return v2Response(method.getName(), DescribeCollectionResponse.newBuilder().setSchema(CollectionSchema.newBuilder().addFields(FieldSchema.newBuilder().setName("id").setDataType(stringKey ? DataType.VarChar : DataType.Int64).setIsPrimaryKey(true).setAutoID(autoId)).addFields(FieldSchema.newBuilder().setName("val").setDataType(DataType.Int32))).build());
            }
            if ("insert".equals(method.getName()) || "upsert".equals(method.getName())) {
                List<JsonObject> data = args[0] instanceof InsertReq ? ((InsertReq) args[0]).getData() : ((UpsertReq) args[0]).getData();
                pages.add(data);
                if (pages.size() == failPage) {
                    throw new SQLTransientConnectionException("lost write acknowledgement");
                }
                List<Object> ids = new ArrayList<>();
                for (JsonObject row : data) {
                    long id = 100L + row.get("val").getAsLong();
                    ids.add(stringKey ? Long.toString(id) : id);
                }
                return args[0] instanceof InsertReq ? InsertResp.builder().InsertCnt(data.size()).primaryKeys(ids).build() : UpsertResp.builder().upsertCnt(data.size()).primaryKeys(ids).build();
            }
            return null;
        });
        Properties props = new Properties();
        props.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        props.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        props.setProperty(MilvusKeys.MAX_RETRY, "5");
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530", props);
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    @Test
    public void multipleValuesKeysAndCountsUseSdkResponses() throws Exception {
        for (String op : Arrays.asList("INSERT", "UPSERT")) {
            for (boolean strings : new boolean[] { false, true }) {
                stringKey = strings;
                try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(op + " INTO t (val) VALUES (?),(?),(?)", Statement.RETURN_GENERATED_KEYS)) {
                    assertTrue(conn.getMetaData().supportsGetGeneratedKeys());
                    ps.setFetchSize(2);
                    ps.setMaxRows(1); // SELECT row limit must not truncate generated keys or writes.
                    for (int i = 1; i <= 3; i++) {
                        ps.setInt(i, i);
                    }
                    assertEquals(3, ps.executeUpdate());
                    assertEquals(2, pages.size());
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        assertEquals("id", keys.getMetaData().getColumnName(1));
                        assertTrue(keys.getMetaData().isAutoIncrement(1));
                        for (long i = 101; i <= 103; i++) {
                            assertTrue(keys.next());
                            assertEquals(strings ? Long.toString(i) : i, keys.getObject(1));
                        }
                        assertFalse(keys.next());
                    }
                }
            }
        }
    }

    @Test
    public void iterableRowsAreConsumedInFetchSizeGroups() throws Exception {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement("INSERT INTO t VALUES ?")) {
            ps.setFetchSize(2);
            Iterator<Map<String, Integer>> rows = new Iterator<>() {
                int next;

                public boolean hasNext() {
                    return next < 5;
                }

                public Map<String, Integer> next() {
                    assertEquals(next / 2, pages.size());
                    return Map.of("val", ++next);
                }
            };
            ps.setObject(1, rows);
            assertEquals(5, ps.executeUpdate());
            assertEquals(Arrays.asList(2, 2, 1), pages.stream().map(List::size).collect(java.util.stream.Collectors.toList()));
            try (ResultSet keys = ps.getGeneratedKeys()) {
                assertFalse(keys.next());
            }
        }
    }

    @Test
    public void failedWritesAreNotRetriedAndReportConfirmedProgress() throws Exception {
        failPage = 2;
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement("UPSERT INTO t (val) VALUES ?", Statement.RETURN_GENERATED_KEYS)) {
            ps.setFetchSize(2);
            ps.setObject(1, Arrays.asList(new Object[] { 1 }, new Object[] { 2 }, new Object[] { 3 }));
            try {
                ps.executeUpdate();
                fail("Expected failure");
            } catch (SQLException e) {
                assertTrue(e.getMessage(), e.getMessage().contains("confirmedPages=1, confirmedRows=2"));
                assertTrue(e.getMessage(), e.getMessage().contains("not retried"));
            }
            assertEquals(2, pages.size());
        }
    }

    @Test
    public void malformedRowsAreRejectedAndEmptyInputWritesNothing() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            for (String sql : Arrays.asList("INSERT INTO t VALUES (1)", "INSERT INTO t (val,val) VALUES (1,2)", "INSERT INTO t (val) VALUES (1),(2,3)")) {
                try {
                    stmt.executeUpdate(sql);
                    fail(sql);
                } catch (SQLException expected) {
                    assertNotNull(expected.getMessage());
                }
            }
            assertTrue(pages.isEmpty());
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO t VALUES ?", Statement.RETURN_GENERATED_KEYS)) {
                ps.setObject(1, Collections.emptyList());
                assertEquals(0, ps.executeUpdate());
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    assertFalse(keys.next());
                }
            }
        }
    }

    @Test
    public void explicitKeysStillUseAcknowledgedIdsAndNoKeysIsOptOut() throws Exception {
        autoId = false;
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            assertEquals(1, stmt.executeUpdate("INSERT INTO t (id,val) VALUES (999,1)", Statement.RETURN_GENERATED_KEYS));
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                assertTrue(keys.next());
                assertEquals(101, keys.getLong("id")); // SDK response, not an inferred input ID.
                assertFalse(keys.getMetaData().isAutoIncrement(1));
                assertFalse(keys.next());
            }
            assertEquals(1, stmt.executeUpdate("UPSERT INTO t (id,val) VALUES (101,1)", Statement.NO_GENERATED_KEYS));
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                assertFalse(keys.next());
            }
        }
    }

    @Test
    public void cancellationDuringInputDoesNotSendThePreparedPage() throws Exception {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement("INSERT INTO t VALUES ?")) {
            ps.setFetchSize(1);
            ps.setObject(1, new Iterator<Map<String, Integer>>() {
                public boolean hasNext() {
                    return true;
                }

                public Map<String, Integer> next() {
                    try {
                        ps.cancel();
                    } catch (SQLException e) {
                        throw new IllegalStateException(e);
                    }
                    return Map.of("val", 1);
                }
            });
            try {
                ps.executeUpdate();
                fail("Expected cancellation");
            } catch (SQLException e) {
                assertTrue(e.getMessage(), e.getMessage().toLowerCase(Locale.ROOT).contains("cancel"));
            }
            assertTrue(pages.isEmpty());
        }
    }

    @Test
    public void inputFailureReportsPriorAcknowledgementsWithoutReplay() throws Exception {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement("INSERT INTO t VALUES ?")) {
            ps.setFetchSize(2);
            ps.setObject(1, new Iterator<Map<String, Integer>>() {
                int next;

                public boolean hasNext() {
                    if (next == 2) {
                        throw new IllegalStateException("input failed");
                    }
                    return true;
                }

                public Map<String, Integer> next() {
                    return Map.of("val", ++next);
                }
            });
            try {
                ps.executeUpdate();
                fail("Expected input failure");
            } catch (SQLException e) {
                assertTrue(e.getMessage(), e.getMessage().contains("phase=read, confirmedPages=1, confirmedRows=2"));
                assertTrue(e.getMessage(), e.getMessage().contains("input failed"));
            }
            assertEquals(1, pages.size());
        }
    }
}
