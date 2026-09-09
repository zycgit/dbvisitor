package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import io.milvus.grpc.*;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchIteratorReqV2;
import io.milvus.v2.service.vector.request.SearchReq;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.*;

public class MilvusVectorArrayTest extends AbstractJdbcTest {
    private final List<Object> captured = new ArrayList<>();

    private Object[] vectors() {
        return new Object[] { new byte[] { -2, 3 }, new short[] { -2, 3 }, new int[] { -2, 3 }, new long[] { -2, 3 }, new float[] { -2, 3 }, new double[] { -2, 3 }, Arrays.asList(-2, 3), Arrays.asList(-2D, 3D) };
    }

    private Connection connect() throws SQLException {
        MilvusCommandInterceptor.resetInterceptor();
        captured.clear();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("describeCollection".equals(method.getName())) {
                return v2Response(method.getName(), DescribeCollectionResponse.newBuilder().setSchema(CollectionSchema.newBuilder().addFields(FieldSchema.newBuilder().setName("id").setDataType(DataType.Int64).setIsPrimaryKey(true)).addFields(FieldSchema.newBuilder().setName("val").setDataType(DataType.Int32)).addFields(FieldSchema.newBuilder().setName("v").setDataType(DataType.FloatVector).addTypeParams(KeyValuePair.newBuilder().setKey(MilvusCommandKeys.DIMENSION).setValue("2")))).build());
            }
            if ("search".equals(method.getName())) {
                captured.add(args[0]);
                return v2Response(method.getName(), SearchResults.newBuilder().setResults(SearchResultData.newBuilder().setNumQueries(1).setTopK(0)).build());
            }
            if ("searchIteratorV2".equals(method.getName())) {
                captured.add(args[0]);
                SearchIteratorV2 iterator = PowerMockito.mock(SearchIteratorV2.class);
                PowerMockito.when(iterator.next()).thenReturn(Collections.emptyList());
                return v2Response(method.getName(), iterator);
            }
            if ("insert".equals(method.getName()) || "upsert".equals(method.getName())) {
                captured.add(args[0]);
                return v2Response(method.getName(), MutationResult.newBuilder().setInsertCnt(1).setUpsertCnt(1).build());
            }
            return null;
        });
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530", properties);
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    @Test
    public void numericArraysAndListsInKnnAndRangeSearch() throws Exception {
        for (String sql : Arrays.asList("SELECT * FROM t ORDER BY v <-> ? LIMIT 2", "SELECT * FROM t WHERE vector_range(v, ?, 10) LIMIT 2", "SELECT * FROM t WHERE v <-> ? < 10 LIMIT 2")) {
            try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Object vector : vectors()) {
                    ps.setObject(1, vector);
                    try (ResultSet rs = ps.executeQuery()) {
                        assertFalse(rs.next());
                    }
                    SearchReq param = (SearchReq) captured.get(captured.size() - 1);
                    assertEquals(Collections.singletonList(Arrays.asList(-2F, 3F)), param.getData().stream().map(v -> v.getData()).collect(java.util.stream.Collectors.toList()));
                }
                assertEquals(vectors().length, captured.size());
            }
        }
    }

    @Test
    public void numericArraysAndListsInInsertAndUpsert() throws Exception {
        for (String command : Arrays.asList("INSERT", "UPSERT")) {
            try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(command + " INTO t (id, v) VALUES (1, ?)")) {
                for (Object vector : vectors()) {
                    ps.setObject(1, vector);
                    assertEquals(1, ps.executeUpdate());
                    Object param = captured.get(captured.size() - 1);
                    com.google.gson.JsonObject entity = param instanceof InsertReq ? ((InsertReq) param).getData().get(0) : ((io.milvus.v2.service.vector.request.UpsertReq) param).getData().get(0);
                    assertEquals(new com.google.gson.Gson().toJsonTree(Arrays.asList(-2F, 3F)), entity.get("v"));
                }
            }
        }
    }

    @Test
    public void numericArraysInUpdateAndDeleteSelection() throws Exception {
        for (String command : Arrays.asList("UPDATE t SET val = 7", "DELETE FROM t")) {
            for (String filter : Arrays.asList(" ORDER BY v <-> ? LIMIT 2", " WHERE vector_range(v, ?, 10) LIMIT 2")) {
                try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(command + filter)) {
                    for (Object vector : vectors()) {
                        ps.setObject(1, vector);
                        assertEquals(0, ps.executeUpdate());
                        SearchIteratorReqV2 param = (SearchIteratorReqV2) captured.get(captured.size() - 1);
                        assertEquals(Collections.singletonList(Arrays.asList(-2F, 3F)), param.getVectors().stream().map(v -> v.getData()).collect(java.util.stream.Collectors.toList()));
                    }
                }
            }
        }
    }

    @Test
    public void floatingPointAndLargeIntegerConversion() throws Exception {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM t ORDER BY v <-> ? LIMIT 2")) {
            Object[] inputs = { new float[] { 0.1F, -0.25F }, new double[] { 0.1D, -0.25D }, new long[] { 16777217L, Long.MAX_VALUE } };
            List<List<Float>> expected = Arrays.asList(Arrays.asList(0.1F, -0.25F), Arrays.asList((float) 0.1D, (float) -0.25D), Arrays.asList((float) 16777217L, (float) Long.MAX_VALUE));
            for (int i = 0; i < inputs.length; i++) {
                ps.setObject(1, inputs[i]);
                try (ResultSet rs = ps.executeQuery()) {
                    assertFalse(rs.next());
                }
                assertEquals(Collections.singletonList(expected.get(i)), ((SearchReq) captured.get(i)).getData().stream().map(v -> v.getData()).collect(java.util.stream.Collectors.toList()));
            }
        }
    }

    @Test
    public void nestedOrderByVectorsAreRejectedBeforeSearch() throws Exception {
        for (String command : Arrays.asList("SELECT * FROM t", "UPDATE t SET val = 7", "DELETE FROM t")) {
            for (String limit : Arrays.asList(" LIMIT 2", "")) {
                try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(command + " ORDER BY v <-> ?" + limit)) {
                    for (Object vector : new Object[] { Arrays.asList(Arrays.asList(1F, 1F), Arrays.asList(99F, 99F)), Collections.singletonList(Arrays.asList(1F, 1F)), Arrays.asList(1F, List.of(2F)), Arrays.asList(new float[] { 1, 1 }, new float[] { 99, 99 }) }) {
                        ps.setObject(1, vector);
                        try {
                            ps.execute();
                            fail("Expected nested query vector rejection");
                        } catch (SQLException e) {
                            assertTrue(e.getMessage(), e.getMessage().contains("single query vector"));
                        }
                    }
                    assertTrue(captured.isEmpty());
                }
            }
        }
    }

    @Test
    public void literalOrderByAcceptsOneVectorAndRejectsNestedVectors() throws Exception {
        for (String limit : Arrays.asList(" LIMIT 2", "")) {
            try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeQuery("SELECT * FROM t ORDER BY v <-> [[1, 1], [99, 99]]" + limit);
                    fail("Expected nested literal rejection");
                } catch (SQLException e) {
                    assertTrue(e.getMessage(), e.getMessage().contains("single query vector"));
                }
                assertTrue(captured.isEmpty());
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM t ORDER BY v <-> [1, 1]" + limit)) {
                    assertFalse(rs.next());
                }
                assertEquals(1, captured.size());
                Object param = captured.get(0);
                assertEquals(Collections.singletonList(Arrays.asList(1F, 1F)), param instanceof SearchReq ? ((SearchReq) param).getData().stream().map(v -> v.getData()).collect(java.util.stream.Collectors.toList()) : ((SearchIteratorReqV2) param).getVectors().stream().map(v -> v.getData()).collect(java.util.stream.Collectors.toList()));
            }
        }
    }

    @Test
    public void nonNumericPrimitiveArraysAreRejectedBeforeSearch() throws Exception {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM t ORDER BY v <-> ? LIMIT 2")) {
            for (Object vector : new Object[] { new boolean[] { true }, new char[] { 'a' } }) {
                ps.setObject(1, vector);
                try {
                    ps.executeQuery();
                    fail("Expected nonnumeric array rejection");
                } catch (SQLException e) {
                    assertTrue(e.getMessage(), e.getMessage().contains("numeric primitive array"));
                }
            }
            assertTrue(captured.isEmpty());
        }
    }
}
