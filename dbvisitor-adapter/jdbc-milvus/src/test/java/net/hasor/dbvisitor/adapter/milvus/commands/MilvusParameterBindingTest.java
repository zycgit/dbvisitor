package net.hasor.dbvisitor.adapter.milvus.commands;

import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.milvus.grpc.*;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.response.QueryResultsWrapper.RowRecord;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import io.milvus.v2.utils.DataUtils;
import io.milvus.v2.utils.VectorUtils;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;

/** Public JDBC binding -> intercepted official SDK requests -> typed wire values. */
public class MilvusParameterBindingTest {
    private static final String PAYLOAD  = "\"x\" || id > 0 \\ \n\t\u0000 {arg2} 中文";
    private final List<Object>  requests = new ArrayList<>();
    private QueryIterator       queryIterator;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        queryIterator = Mockito.mock(QueryIterator.class);
        Mockito.when(queryIterator.next()).thenReturn(Collections.emptyList());
        SearchIteratorV2 searchIterator = Mockito.mock(SearchIteratorV2.class);
        Mockito.when(searchIterator.next()).thenReturn(Collections.emptyList());
        CollectionSchema schema = CollectionSchema.newBuilder().setName("t").addFields(FieldSchema.newBuilder().setName("id").setDataType(DataType.VarChar).setIsPrimaryKey(true)).addFields(FieldSchema.newBuilder().setName("title").setDataType(DataType.VarChar)).addFields(FieldSchema.newBuilder().setName("n").setDataType(DataType.Int64)).addFields(FieldSchema.newBuilder().setName("v").setDataType(DataType.FloatVector)).build();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "describeCollection":
                    return v2Response(method.getName(), DescribeCollectionResponse.newBuilder().setSchema(schema).build());
                case "query":
                    requests.add(args[0]);
                    return QueryResp.builder().queryResults(Collections.emptyList()).build();
                case "queryIterator":
                    requests.add(args[0]);
                    return queryIterator;
                case "search":
                case "hybridSearch":
                    requests.add(args[0]);
                    return SearchResp.builder().searchResults(Collections.emptyList()).build();
                case "searchIteratorV2":
                    requests.add(args[0]);
                    return searchIterator;
                case "delete":
                    requests.add(args[0]);
                    return DeleteResp.builder().deleteCnt(1).build();
                case "upsert":
                    requests.add(args[0]);
                    return UpsertResp.builder().upsertCnt(((UpsertReq) args[0]).getData().size()).build();
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
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://mock:19530", properties);
    }

    private void execute(String sql, Object... values) throws Exception {
        try (Connection conn = connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            statement.execute();
        }
    }

    private record BoundFilter(String expression, Map<String, Object> values) {
    }

    private BoundFilter filter(Object request) {
        if (request instanceof QueryReq query) {
            return new BoundFilter(query.getFilter(), query.getFilterTemplateValues());
        }
        if (request instanceof QueryIteratorReq query) {
            return new BoundFilter(query.getExpr(), query.getFilterTemplateValues());
        }
        if (request instanceof SearchReq search) {
            return new BoundFilter(search.getFilter(), search.getFilterTemplateValues());
        }
        if (request instanceof SearchIteratorReqV2 search) {
            return new BoundFilter(search.getFilter(), search.getFilterTemplateValues());
        }
        if (request instanceof DeleteReq delete) {
            return new BoundFilter(delete.getFilter(), delete.getFilterTemplateValues());
        }
        throw new AssertionError("Unexpected request " + request.getClass());
    }

    @Test
    public void allSelectionAndNativeDeletePathsUseTemplates() throws Exception {
        // @formatter:off
        List<String> commands = Arrays.asList(
            "SELECT id FROM t WHERE title = ? LIMIT 1",
            "SELECT id FROM t WHERE title = ?",
            "COUNT FROM t WHERE title = ?",
            "/*+ overwrite_find_as_count */ SELECT id FROM t WHERE title = ?",
            "DELETE FROM t WHERE title = ?",
            "DELETE FROM t WHERE title = ? LIMIT 1",
            "UPDATE t SET n = 1 WHERE title = ?",
            "SELECT id FROM t WHERE title = ? ORDER BY v <-> [1,2] LIMIT 1",
            "SELECT id FROM t WHERE title = ? ORDER BY v <-> [1,2]",
            "SELECT id FROM t WHERE title = ? AND v <-> [1,2] < 3 LIMIT 1",
            "SELECT id FROM t WHERE title = ? AND v <-> [1,2] < 3",
            "DELETE FROM t WHERE title = ? ORDER BY v <-> [1,2] LIMIT 1",
            "DELETE FROM t WHERE title = ? AND v <-> [1,2] < 3",
            "UPDATE t SET n = 1 WHERE title = ? ORDER BY v <-> [1,2] LIMIT 1",
            "UPDATE t SET n = 1 WHERE title = ? AND v <-> [1,2] < 3"
        );
        // @formatter:on
        for (String sql : commands) {
            requests.clear();
            execute(sql, PAYLOAD);
            assertEquals(sql, 1, requests.size());
            BoundFilter bound = filter(requests.get(0));
            assertEquals(sql, "title == {arg1}", bound.expression());
            assertEquals(sql, Collections.singletonMap("arg1", PAYLOAD), bound.values());
            assertEquals(PAYLOAD, VectorUtils.deduceAndCreateTemplateValue(bound.values().get("arg1")).getStringVal());
        }
    }

    @Test
    public void stringObjectAndExplicitVarcharBindingsAreAlwaysData() throws Exception {
        try (Connection conn = connect(); PreparedStatement statement = conn.prepareStatement("DELETE FROM t WHERE title = ?")) {
            statement.setString(1, PAYLOAD);
            statement.executeUpdate();
            statement.setObject(1, PAYLOAD);
            statement.executeUpdate();
            statement.setObject(1, new StringBuilder(PAYLOAD), Types.VARCHAR);
            statement.executeUpdate();
            statement.setObject(1, new StringBuilder(PAYLOAD), JDBCType.VARCHAR);
            statement.executeUpdate();
            statement.setObject(1, new StringBuilder(PAYLOAD));
            statement.executeUpdate();
        }
        assertEquals(5, requests.size());
        for (Object request : requests) {
            DeleteRequest wire = new DataUtils().ConvertToGrpcDeleteRequest((DeleteReq) request);
            assertEquals("title == {arg1}", wire.getExpr());
            assertEquals(PAYLOAD, wire.getExprTemplateValuesOrThrow("arg1").getStringVal());
        }
    }

    @Test
    public void likeInFunctionsAndMixedLiteralListsRetainValues() throws Exception {
        execute("DELETE FROM t WHERE title LIKE ? AND id IN ?", PAYLOAD, Arrays.asList("safe", PAYLOAD));
        BoundFilter bound = filter(requests.get(0));
        assertEquals("title like {arg1} AND id in {arg2}", bound.expression());
        assertEquals(Map.of("arg1", PAYLOAD, "arg2", Arrays.asList("safe", PAYLOAD)), bound.values());

        execute("DELETE FROM t WHERE id IN (?, 'fixed', ?) AND title = ?", PAYLOAD, "?", "tail");
        bound = filter(requests.get(1));
        assertEquals("id in {arg1} AND title == {arg3}", bound.expression());
        assertEquals(Map.of("arg1", Arrays.asList(PAYLOAD, "fixed", "?"), "arg3", "tail"), bound.values());

        execute("DELETE FROM t WHERE array_contains_any(title, [?, 'fixed']) AND NOT (n + ? > ?)", PAYLOAD, 1, 3);
        bound = filter(requests.get(2));
        assertEquals("array_contains_any(title, {arg1}) AND not (n + {arg2} > {arg3})", bound.expression());
        assertEquals(Map.of("arg1", Arrays.asList(PAYLOAD, "fixed"), "arg2", 1, "arg3", 3), bound.values());
    }

    @Test
    public void updateSetAndRangeArgumentsStayInSqlOrder() throws Exception {
        execute("UPDATE t SET title = ? WHERE n > ? AND (v <-> ? < ? AND n < ?) LIMIT ?", PAYLOAD, 10, new float[] { 1, 2 }, 3, 20, 2);
        SearchIteratorReqV2 query = (SearchIteratorReqV2) requests.get(0);
        assertEquals(Map.of("arg2", 10, "arg5", 20), query.getFilterTemplateValues());
        assertEquals("(n < {arg5}) && (n > {arg2})", query.getFilter());
        assertEquals(3.0, ((Number) query.getSearchParams().get(MilvusCommandKeys.RADIUS)).doubleValue(), 0);
        assertEquals(2L, query.getLimit());
    }

    @Test
    public void hybridCandidatesCarryTheSameBoundFilterToTheWire() throws Exception {
        execute("SELECT id FROM t WHERE title = ? ORDER BY HYBRID (v <-> ? LIMIT 4, v <-> ? LIMIT 4) LIMIT 2 WITH (reranker='rrf')", PAYLOAD, new float[] { 1, 2 }, new float[] { 3, 4 });
        HybridSearchReq query = (HybridSearchReq) requests.get(0);
        assertEquals(2, query.getSearchRequests().size());
        for (AnnSearchReq candidate : query.getSearchRequests()) {
            SearchRequest wire = VectorUtils.convertAnnSearchParam(candidate, query.getConsistencyLevel());
            assertEquals("title == {arg1}", wire.getDsl());
            assertEquals(PAYLOAD, wire.getExprTemplateValuesOrThrow("arg1").getStringVal());
        }
    }

    @Test
    public void queryAndSearchSerializeTypedTemplateValues() throws Exception {
        execute("SELECT id FROM t WHERE title = ? AND n > ? LIMIT 1", PAYLOAD, 9);
        QueryRequest query = new VectorUtils().ConvertToGrpcQueryRequest((QueryReq) requests.get(0));
        assertEquals("title == {arg1} AND n > {arg2}", query.getExpr());
        assertEquals(PAYLOAD, query.getExprTemplateValuesOrThrow("arg1").getStringVal());
        assertEquals(9, query.getExprTemplateValuesOrThrow("arg2").getInt64Val());

        execute("SELECT id FROM t WHERE title = ? ORDER BY v <-> ? LIMIT 1", PAYLOAD, new float[] { 1, 2 });
        SearchRequest search = new VectorUtils().ConvertToGrpcSearchRequest((SearchReq) requests.get(1));
        assertEquals("title == {arg1}", search.getDsl());
        assertEquals(PAYLOAD, search.getExprTemplateValuesOrThrow("arg1").getStringVal());
    }

    @Test
    public void pagedDeleteBindsReturnedStringPrimaryKeys() throws Exception {
        RowRecord row = new RowRecord();
        row.put("id", PAYLOAD);
        Mockito.when(queryIterator.next()).thenReturn(Collections.singletonList(row), Collections.emptyList());
        execute("DELETE FROM t WHERE n > ? LIMIT 1", 0);
        DeleteReq delete = (DeleteReq) requests.get(1);
        DeleteRequest wire = new DataUtils().ConvertToGrpcDeleteRequest(delete);
        assertEquals("id in {ids}", wire.getExpr());
        assertEquals(Collections.singletonList(PAYLOAD), wire.getExprTemplateValuesOrThrow("ids").getArrayVal().getStringData().getDataList());
    }

    @Test
    public void updateSetValuesRemainStructuredDataIncludingQuestionMarks() throws Exception {
        RowRecord row = new RowRecord();
        row.put("id", "one");
        for (String value : Arrays.asList(PAYLOAD, "?")) {
            requests.clear();
            Mockito.when(queryIterator.next()).thenReturn(Collections.singletonList(row), Collections.emptyList());
            execute("UPDATE t SET title = ? WHERE n > ? LIMIT 1", value, 0);
            assertEquals(Map.of("arg2", 0), filter(requests.get(0)).values());
            UpsertReq upsert = (UpsertReq) requests.get(1);
            assertTrue(upsert.isPartialUpdate());
            assertEquals(value, upsert.getData().get(0).get("title").getAsString());
        }
    }

    @Test
    public void scalarAndArrayValuesAreNormalizedForSdkTemplates() throws Exception {
        // @formatter:off
        Object[][] cases = {
            { (byte) 1, 1L },
            { (short) 2, 2L },
            { 3, 3 },
            { 4L, 4L },
            { 1.5F, 1.5D },
            { new BigInteger("5"), 5L },
            { new BigDecimal("1.25"), 1.25D },
            { new int[] { 1, 2 }, Arrays.asList(1, 2) },
            { new float[] { 1, 2 }, Arrays.asList(1D, 2D) },
            { new boolean[] { true, false }, Arrays.asList(true, false) },
            { new char[] { 'a', '"' }, Arrays.asList("a", "\"") },
            { new String[] { PAYLOAD }, Collections.singletonList(PAYLOAD) },
            { Collections.emptyList(), Collections.emptyList() }
        };
        // @formatter:on
        for (Object[] item : cases) {
            requests.clear();
            execute("DELETE FROM t WHERE n = ?", item[0]);
            assertEquals(item[1], filter(requests.get(0)).values().get("arg1"));
        }
        try (Connection conn = connect(); PreparedStatement statement = conn.prepareStatement("DELETE FROM t WHERE id IN ?")) {
            java.sql.Array array = conn.createArrayOf("VARCHAR", new String[] { PAYLOAD });
            try {
                statement.setArray(1, array);
                statement.executeUpdate();
                assertEquals(Collections.singletonList(PAYLOAD), filter(requests.get(requests.size() - 1)).values().get("arg1"));
            } finally {
                array.free();
            }
        }
    }

    @Test
    public void invalidValuesFailBeforeSendingAnySelectionOrMutation() throws Exception {
        // @formatter:off
        for (Object value : Arrays.asList(
            null,
            Double.NaN,
            Float.POSITIVE_INFINITY,
            new BigInteger("9223372036854775808"),
            Map.of("x", PAYLOAD),
            Arrays.asList("text", 1),
            Collections.singletonList(null),
            new Object() {
                @Override
                public String toString() {
                    return PAYLOAD;
                }
            }
        )) {
            // @formatter:on
            requests.clear();
            try {
                execute("DELETE FROM t WHERE title = ?", value);
                fail("Expected unsupported parameter to be rejected");
            } catch (SQLException expected) {
                assertFalse(expected.getMessage().isEmpty());
            }
            assertTrue(requests.isEmpty());
        }
    }

    @Test
    public void repeatedExecutionAndMultipleStatementsDoNotLeakBindings() throws Exception {
        List<String> values = new ArrayList<>(Collections.singletonList(PAYLOAD));
        try (Connection conn = connect(); PreparedStatement statement = conn.prepareStatement("DELETE FROM t WHERE id IN ?")) {
            statement.setObject(1, values);
            statement.executeUpdate();
            values.set(0, "changed");
            statement.executeUpdate();
        }
        assertEquals(Collections.singletonList(PAYLOAD), filter(requests.get(0)).values().get("arg1"));
        assertEquals(Collections.singletonList("changed"), filter(requests.get(1)).values().get("arg1"));
        execute("DELETE FROM t WHERE title = ?; DELETE FROM t WHERE title = ?", PAYLOAD, "second");
        assertEquals(Collections.singletonMap("arg1", PAYLOAD), filter(requests.get(2)).values());
        assertEquals(Collections.singletonMap("arg2", "second"), filter(requests.get(3)).values());
    }

    @Test
    public void sqlLiteralsAndNullPredicatesRemainSqlStructure() throws Exception {
        execute("DELETE FROM t WHERE title = 'literal' AND n IS NULL");
        BoundFilter bound = filter(requests.get(0));
        assertEquals("title == \"literal\" AND n is null", bound.expression());
        assertTrue(bound.values().isEmpty());
    }
}
