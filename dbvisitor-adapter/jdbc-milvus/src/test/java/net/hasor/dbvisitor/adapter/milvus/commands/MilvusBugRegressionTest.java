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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;

import io.milvus.grpc.*;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.param.Constant;
import io.milvus.param.ParamUtils;
import io.milvus.param.collection.FieldType;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.IndexParam.MetricType;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.response.UpsertResp;
import io.milvus.v2.utils.ConvertUtils;
import io.milvus.v2.utils.DataUtils;
import io.milvus.v2.utils.SchemaUtils;
import io.milvus.v2.utils.VectorUtils;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;

public class MilvusBugRegressionTest {
    private final List<Object> requests = new ArrayList<>();
    private QueryIterator      queryIterator;
    private SearchIteratorV2   searchIterator;
    private QueryResults       queryResult;
    private SearchResults      searchResult;
    private CollectionSchema   schema;
    private Statement          cancelOnWrite;
    private Statement          cancelOnRead;
    private boolean            failWrite;
    private boolean            delayRead;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        schema = CollectionSchema.newBuilder().setName("t").addFields(FieldSchema.newBuilder().setName("id").setDataType(DataType.Int64).setIsPrimaryKey(true)).addFields(FieldSchema.newBuilder().setName("a").setDataType(DataType.Int32)).addFields(FieldSchema.newBuilder().setName("j").setDataType(DataType.JSON)).addFields(FieldSchema.newBuilder().setName("v").setDataType(DataType.FloatVector).addTypeParams(KeyValuePair.newBuilder().setKey(MilvusCommandKeys.DIMENSION).setValue("2"))).build();
        queryResult = QueryResults.newBuilder().addFieldsData(longField("id", 1, 2, 3)).build();
        searchResult = SearchResults.newBuilder().setResults(SearchResultData.newBuilder().setNumQueries(1).setTopK(3).addTopks(3).setPrimaryFieldName("id").setIds(IDs.newBuilder().setIntId(LongArray.newBuilder().addData(1).addData(2).addData(3))).addScores(0.1F).addScores(0.2F).addScores(0.3F)).build();
        queryIterator = Mockito.mock(QueryIterator.class);
        searchIterator = Mockito.mock(SearchIteratorV2.class);
        Mockito.when(queryIterator.next()).thenAnswer(invocation -> nextPage());
        Mockito.when(searchIterator.next()).thenAnswer(invocation -> nextPage());
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            String name = method.getName();
            if ("describeCollection".equals(name)) {
                return v2Response(method.getName(), DescribeCollectionResponse.newBuilder().setSchema(schema).build());
            }
            if ("query".equals(name)) {
                requests.add(args[0]);
                return v2Response(method.getName(), queryResult);
            }
            if ("search".equals(name)) {
                requests.add(args[0]);
                return v2Response(method.getName(), searchResult);
            }
            if ("queryIterator".equals(name)) {
                requests.add(args[0]);
                return v2Response(method.getName(), queryIterator);
            }
            if ("searchIteratorV2".equals(name)) {
                requests.add(args[0]);
                return v2Response(method.getName(), searchIterator);
            }
            if ("delete".equals(name)) {
                requests.add(args[0]);
                if (cancelOnWrite != null) {
                    cancelOnWrite.cancel();
                }
                if (failWrite) {
                    throw new java.sql.SQLException("write failed");
                }
                return v2Response(method.getName(), MutationResult.newBuilder().setDeleteCnt(1).build());
            }
            if ("insert".equals(name)) {
                requests.add(args[0]);
                return v2Response(method.getName(), MutationResult.newBuilder().setInsertCnt(1).build());
            }
            if ("createCollection".equals(name)) {
                requests.add(args[0]);
                return v2Response(method.getName(), Status.newBuilder().build());
            }
            if ("upsert".equals(method.getName())) {
                requests.add(args[0]);
                if (cancelOnWrite != null) {
                    cancelOnWrite.cancel();
                }
                if (failWrite) {
                    throw new IllegalStateException("write failed");
                }
                return UpsertResp.builder().upsertCnt(((UpsertReq) args[0]).getData().size()).build();
            }
            return null;
        });
    }

    private List<QueryResultsWrapper.RowRecord> nextPage() throws Exception {
        if (delayRead) {
            Thread.sleep(1100);
        }
        if (cancelOnRead != null) {
            cancelOnRead.cancel();
        }
        return Collections.emptyList();
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    private Connection connect() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://mock:19530?" + MilvusKeys.MAX_RETRY + "=2", properties);
    }

    private void execute(String sql, Object... arguments) throws Exception {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < arguments.length; i++) {
                statement.setObject(i + 1, arguments[i]);
            }
            if (statement.execute()) {
                try (ResultSet result = statement.getResultSet()) {
                    while (result.next()) {
                        /* Exercise the full query in these binding regressions. */ }
                }
            }
        }
    }

    private void reject(String sql, Object... arguments) throws Exception {
        requests.clear();
        try {
            execute(sql, arguments);
            fail("Expected rejection: " + sql);
        } catch (SQLException expected) {
            assertFalse(expected.getMessage().isEmpty());
        }
        assertTrue("No search or mutation may be sent: " + sql, requests.isEmpty());
    }

    private static FieldData longField(String name, long... values) {
        LongArray.Builder data = LongArray.newBuilder();
        for (long value : values) {
            data.addData(value);
        }
        return FieldData.newBuilder().setFieldName(name).setType(DataType.Int64).setScalars(ScalarField.newBuilder().setLongData(data)).build();
    }

    private static List<QueryResultsWrapper.RowRecord> page(long id) {
        QueryResultsWrapper.RowRecord row = new QueryResultsWrapper.RowRecord();
        row.put("id", id);
        return Collections.singletonList(row);
    }

    @Test
    public void invalidRangeNeverSendsSearchOrWrite() throws Exception {
        for (String command : Arrays.asList("SELECT id FROM t", "DELETE FROM t", "UPDATE t SET a = 7")) {
            for (String limit : Arrays.asList("", " LIMIT 2")) {
                for (String range : Arrays.asList("v <-> [1,2] < ?", "vector_range(v, [1,2], ?)")) {
                    for (Object radius : new Object[] { null, "bad", "0.5", Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1e100, -1, new BigDecimal("1e1000") }) {
                        reject(command + " WHERE " + range + limit, radius);
                    }
                }
            }
        }
    }

    @Test
    public void unsupportedRangeShapesFailClosed() throws Exception {
        // @formatter:off
        for (String expression : Arrays.asList(
            "v <-> [1,2] > 1",
            "v <-> [1,2] <= 1",
            "v <=> [1,2] < 0.5",
            "v <#> [1,2] >= 1",
            "a = 1 OR v <-> [1,2] < 2",
            "NOT (v <-> [1,2] < 2)",
            "v <-> [1,2] < 1 AND v <-> [1,2] < 2",
            "vector_range(v, [1,2])"
        )) {
            // @formatter:on
            reject("DELETE FROM t WHERE " + expression + " LIMIT 2");
        }
        reject("DELETE FROM t WHERE v <=> [1,2] > ?", 1.1);
        reject("DELETE FROM t WHERE v <=> [1,2] > ?", -1.1);
        reject("SELECT id FROM t WHERE v <-> [1,2] < 1 ORDER BY v <-> [1,2] LIMIT 2");
    }

    @Test
    public void scalarDmlParametersAreConsumedExactlyOnce() throws Exception {
        execute("DELETE FROM t WHERE (a > ? AND (a < ?)) LIMIT ?", 10, 20, 2);
        QueryIteratorReq delete = (QueryIteratorReq) requests.get(0);
        assertEquals("(a > {arg1} AND (a < {arg2}))", delete.getExpr());
        assertEquals(Map.of("arg1", 10, "arg2", 20), delete.getFilterTemplateValues());
        assertEquals(2L, delete.getLimit());
        requests.clear();
        execute("UPDATE t SET a = ? WHERE (a > ? AND a < ?) LIMIT ?", 7, 10, 20, 2);
        assertEquals(2L, ((QueryIteratorReq) requests.get(0)).getLimit());
        requests.clear();
        execute("DELETE FROM t WHERE a > ? AND a < ?", 10, 20);
        assertTrue(requests.get(0) instanceof DeleteReq);
    }

    @Test
    public void knnDmlBindsWhereBeforeVectorAndLimit() throws Exception {
        execute("DELETE FROM t WHERE a > ? ORDER BY v <=> ? LIMIT ?", 10, new float[] { 1, 2 }, 2);
        SearchIteratorReqV2 selected = (SearchIteratorReqV2) requests.get(0);
        assertEquals(MetricType.COSINE, selected.getMetricType());
        assertEquals(2L, selected.getLimit());
        assertEquals("a > {arg1}", selected.getFilter());
        assertEquals(Map.of("arg1", 10), selected.getFilterTemplateValues());
        assertEquals(Collections.singletonList(Arrays.asList(1F, 2F)), selected.getVectors().stream().map(v -> v.getData()).collect(java.util.stream.Collectors.toList()));
    }

    @Test
    public void rangeScalarParametersKeepLeftToRightOrder() throws Exception {
        for (String condition : Arrays.asList("(a > ? AND v <-> ? < ?) AND a < ?", "a > ? AND (vector_range(v, ?, ?) AND a < ?)")) {
            requests.clear();
            execute("DELETE FROM t WHERE " + condition + " LIMIT ?", 10, new double[] { 1, 2 }, 3, 20, 2);
            SearchIteratorReqV2 selected = (SearchIteratorReqV2) requests.get(0);
            assertTrue(selected.getFilter(), selected.getFilter().contains("a > {arg1}") && selected.getFilter().contains("a < {arg4}"));
            assertEquals(Map.of("arg1", 10, "arg4", 20), selected.getFilterTemplateValues());
            assertEquals(3.0, JsonParser.parseString(new com.google.gson.Gson().toJson(selected.getSearchParams())).getAsJsonObject().get(MilvusCommandKeys.RADIUS).getAsDouble(), 0);
            assertEquals(2L, selected.getLimit());
        }
        requests.clear();
        execute("DELETE FROM t WHERE v <-> ? < ? AND (a > ? AND a < ?) LIMIT ?", new float[] { 1, 2 }, 3, 10, 20, 2);
        assertEquals("(a > {arg3} AND a < {arg4})", ((SearchIteratorReqV2) requests.get(0)).getFilter());
        assertEquals(Map.of("arg3", 10, "arg4", 20), ((SearchIteratorReqV2) requests.get(0)).getFilterTemplateValues());
    }

    @Test
    public void allSearchPathsPreserveMetricAndRangeDirection() throws Exception {
        String[] operators = { "<->", "<=>", "<#>" };
        MetricType[] metrics = { MetricType.L2, MetricType.COSINE, MetricType.IP };
        for (String command : Arrays.asList("SELECT id FROM t", "DELETE FROM t", "UPDATE t SET a = 7")) {
            for (String limit : Arrays.asList(" LIMIT 2", "")) {
                for (int i = 0; i < operators.length; i++) {
                    requests.clear();
                    execute(command + " ORDER BY v " + operators[i] + " [1,2]" + limit);
                    assertMetric(requests.get(0), metrics[i], false);
                    requests.clear();
                    execute(command + " WHERE v " + operators[i] + " [1,2] " + (i == 0 ? "<" : ">") + " ?" + limit, i == 0 ? 0 : -0.5);
                    assertMetric(requests.get(0), metrics[i], true);
                }
            }
        }
    }

    private static void assertMetric(Object request, MetricType metric, boolean range) {
        String params;
        if (request instanceof SearchReq) {
            assertEquals(metric, ((SearchReq) request).getMetricType());
            params = new Gson().toJson(((SearchReq) request).getSearchParams());
            SearchRequest wire = new VectorUtils().ConvertToGrpcSearchRequest((SearchReq) request);
            assertTrue(wire.getSearchParamsList().stream().anyMatch(k -> "metric_type".equals(k.getKey()) && metric.name().equals(k.getValue())));
        } else {
            assertEquals(metric, ((SearchIteratorReqV2) request).getMetricType());
            params = new com.google.gson.Gson().toJson(((SearchIteratorReqV2) request).getSearchParams());
        }
        JsonObject json = JsonParser.parseString(params).getAsJsonObject();
        assertEquals(range, json.has(MilvusCommandKeys.RADIUS));
        assertFalse(json.has(MilvusCommandKeys.RANGE_FILTER));
    }

    @Test
    public void allSelectPathsValidateLimitAndOffsetStrictly() throws Exception {
        for (String base : Arrays.asList("SELECT id FROM t", "SELECT id FROM t ORDER BY v <-> [1,2]", "SELECT id FROM t WHERE v <-> [1,2] < 2")) {
            for (Object limit : new Object[] { null, 0, -1, 1.5, 1.0, "2", new BigInteger("9223372036854775808") }) {
                reject(base + " LIMIT ?", limit);
            }
            for (Object offset : new Object[] { null, -1, 0.5, "2", new BigInteger("9223372036854775808") }) {
                reject(base + " LIMIT 2 OFFSET ?", offset);
            }
        }
        execute("SELECT id FROM t ORDER BY v <-> [1,2] LIMIT ?", (long) Integer.MAX_VALUE + 1);
        assertEquals(io.milvus.param.Constant.UNLIMITED, ((SearchIteratorReqV2) requests.get(0)).getLimit());
    }

    @Test
    public void selectBindsEveryClauseInSqlOrderAndEscapesProperties() throws Exception {
        String payload = "x\", \"radius\":999, \"dummy\":\"y\\\n\t\r\b\f";
        execute("SELECT id FROM t WHERE a > ? ORDER BY v <-> ? LIMIT ? OFFSET ? WITH (custom=?, nprobe=?)", 10, new float[] { 1, 2 }, 2, 5, payload, 16);
        SearchIteratorReqV2 search = (SearchIteratorReqV2) requests.get(0);
        JsonObject params = JsonParser.parseString(new com.google.gson.Gson().toJson(search.getSearchParams())).getAsJsonObject();
        assertEquals(7L, search.getLimit());
        assertEquals(16, params.get("nprobe").getAsInt());
        assertEquals(payload, params.get("custom").getAsString());
        assertFalse(params.has(MilvusCommandKeys.RADIUS));
        assertFalse(params.has("dummy"));
        assertEquals("a > {arg1}", search.getFilter());
        assertEquals(Map.of("arg1", 10), search.getFilterTemplateValues());
    }

    @Test
    public void literalAndBoundWithPropertiesPreserveJsonTypes() throws Exception {
        execute("SELECT id FROM t ORDER BY v <-> [1,2] LIMIT 2 WITH ('custom'='x\", \"radius\":999', flag=true, number=2.5, quoted='123', 'key'=word)");
        JsonObject params = JsonParser.parseString(new Gson().toJson(((SearchReq) requests.get(0)).getSearchParams())).getAsJsonObject();
        assertEquals("x\", \"radius\":999", params.get("custom").getAsString());
        assertFalse(params.has(MilvusCommandKeys.RADIUS));
        assertTrue(params.get("flag").getAsBoolean());
        assertEquals(2.5, params.get("number").getAsDouble(), 0);
        assertTrue(params.get("quoted").getAsJsonPrimitive().isString());
        assertEquals("word", params.get("key").getAsString());
        requests.clear();
        execute("SELECT id FROM t ORDER BY v <-> [1,2] WITH (custom=?)", "tail\\\n");
        assertEquals("tail\\\n", JsonParser.parseString(new com.google.gson.Gson().toJson(((SearchIteratorReqV2) requests.get(0)).getSearchParams())).getAsJsonObject().get("custom").getAsString());
        reject("SELECT id FROM t ORDER BY v <-> [1,2] LIMIT 2 WITH (metric_type=IP)");
        reject("SELECT id FROM t WHERE v <-> [1,2] < 1 LIMIT 2 WITH (radius=999)");
    }

    @Test
    public void hintsOverrideBoundsWithoutSkippingSqlArguments() throws Exception {
        execute("/*+ overwrite_find_limit=?, overwrite_find_skip=? */ SELECT id FROM t WHERE a > ? ORDER BY v <-> ? LIMIT ? OFFSET ? WITH (custom=?)", 1, 3, 10, new float[] { 1, 2 }, 2, 5, "last");
        SearchIteratorReqV2 search = (SearchIteratorReqV2) requests.get(0);
        assertEquals(4L, search.getLimit());
        JsonObject params = JsonParser.parseString(new com.google.gson.Gson().toJson(search.getSearchParams())).getAsJsonObject();
        assertEquals("last", params.get("custom").getAsString());
        reject("/*+ overwrite_find_limit=2 */ SELECT id FROM t LIMIT ? WITH (custom=?)", 1.5, "last");
        reject("/*+ overwrite_find_skip=? */ SELECT id FROM t LIMIT 2", -1);
    }

    @Test
    public void jdbcMaxRowsCapsScalarSearchAndRangeResults() throws Exception {
        for (String sql : Arrays.asList("SELECT id FROM t LIMIT 2 OFFSET 5", "SELECT id FROM t ORDER BY v <-> [1,2] LIMIT 2 OFFSET 5", "SELECT id FROM t WHERE v <-> [1,2] < 2 LIMIT 2 OFFSET 5")) {
            Mockito.when(queryIterator.next()).thenReturn(page(1), page(2), page(3), page(4), page(5), page(6), Collections.emptyList());
            Mockito.when(searchIterator.next()).thenReturn(net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(1)), net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(2)), net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(3)), net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(4)), net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(5)), net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(6)), Collections.emptyList());
            try (Connection conn = connect(); Statement statement = conn.createStatement()) {
                statement.setMaxRows(1);
                try (ResultSet result = statement.executeQuery(sql)) {
                    assertTrue(result.next());
                    assertFalse(result.next());
                }
            }
        }
        QueryIteratorReq query = (QueryIteratorReq) requests.get(0);
        assertEquals(6L, query.getLimit());
        assertEquals(0L, query.getOffset());
    }

    @Test
    public void unlimitedIteratorSkipsOffsetAndHonorsMaxRowsAndFetchSize() throws Exception {
        for (String sql : Arrays.asList("SELECT id FROM t ORDER BY v <-> [1,2] OFFSET ?", "/*+ overwrite_find_skip=1 */ SELECT id FROM t WHERE v <-> [1,2] < 2 OFFSET ?")) {
            Mockito.reset(searchIterator);
            Mockito.when(searchIterator.next()).thenReturn(net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(1)), net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(2)), net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(3)), Collections.emptyList());
            try (Connection conn = connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, 1);
                statement.setFetchSize(1);
                statement.setMaxRows(1);
                try (ResultSet result = statement.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals(2, result.getLong(1));
                    assertFalse(result.next());
                }
            }
            SearchIteratorReqV2 request = (SearchIteratorReqV2) requests.get(requests.size() - 1);
            assertEquals(1L, request.getBatchSize());
            assertEquals(2L, request.getTopK());
            Mockito.verify(searchIterator, Mockito.times(2)).next();
            Mockito.verify(searchIterator).close();
        }
    }

    @Test
    public void unlimitedIteratorHasNoArbitraryTotalCap() throws Exception {
        Mockito.when(searchIterator.next()).thenReturn(net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(1)), net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(2)), net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(3)), Collections.emptyList());
        execute("SELECT id FROM t ORDER BY v <-> [1,2] OFFSET 1");
        assertEquals(Constant.UNLIMITED_L, ((SearchIteratorReqV2) requests.get(0)).getTopK());
        Mockito.verify(searchIterator, Mockito.times(4)).next();
        Mockito.verify(searchIterator).close();
    }

    @Test
    public void cancelStopsSubsequentPagesAndRetriesForEveryDmlPath() throws Exception {
        for (String command : Arrays.asList("DELETE FROM t", "UPDATE t SET a = 7")) {
            for (String selection : Arrays.asList(" WHERE a > 0 LIMIT 2", " ORDER BY v <-> [1,2] LIMIT 2", " WHERE v <-> [1,2] < 2 LIMIT 2")) {
                for (boolean fail : new boolean[] { false, true }) {
                    requests.clear();
                    Mockito.reset(queryIterator, searchIterator);
                    Mockito.when(queryIterator.next()).thenReturn(page(1), page(2), Collections.emptyList());
                    Mockito.when(searchIterator.next()).thenReturn(net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(1)), net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(2)), Collections.emptyList());
                    failWrite = fail;
                    try (Connection conn = connect(); Statement statement = conn.createStatement()) {
                        cancelOnWrite = statement;
                        try {
                            statement.executeUpdate(command + selection);
                            fail("Expected cancellation");
                        } catch (SQLException expected) {
                            assertTrue(expected.getMessage(), expected.getMessage().toLowerCase(Locale.ROOT).contains("cancel"));
                        }
                        assertEquals(2, requests.size()); // one selection request and exactly one write
                        boolean scalar = selection.startsWith(" WHERE a");
                        if (scalar) {
                            Mockito.verify(queryIterator).close();
                        } else {
                            Mockito.verify(searchIterator).close();
                        }
                        cancelOnWrite = null;
                        failWrite = false;
                        // Cancellation belongs to the old execution, not the next JDBC request.
                        statement.executeUpdate("DELETE FROM t WHERE id = 99");
                    }
                }
            }
        }
    }

    @Test
    public void cancelAfterReadingPreventsTheFirstWrite() throws Exception {
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            cancelOnRead = statement;
            Mockito.when(queryIterator.next()).thenAnswer(invocation -> {
                statement.cancel();
                return page(1);
            });
            try {
                statement.executeUpdate("DELETE FROM t WHERE a > 0 LIMIT 2");
                fail("Expected cancellation");
            } catch (SQLException expected) {
                assertEquals(1, requests.size());
            }
            Mockito.verify(queryIterator).close();
        }
    }

    @Test
    public void queryTimeoutClosesIteratorWithoutWriting() throws Exception {
        delayRead = true;
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            statement.setQueryTimeout(1);
            try {
                statement.executeUpdate("DELETE FROM t WHERE a > 0 LIMIT 2");
                fail("Expected timeout");
            } catch (SQLException expected) {
                assertEquals(1, requests.size());
            }
            Mockito.verify(queryIterator).close();
        }
    }

    @Test
    public void jsonListsAreNotConvertedToFloatVectors() throws Exception {
        // @formatter:off
        Object[] values = {
            Arrays.asList(16777217L, Long.MAX_VALUE),
            Collections.emptyList(),
            Arrays.asList(List.of(16777217L), Arrays.asList(1, 2)),
            Collections.singletonMap("numbers", List.of(16777217L)),
            new long[] { 16777217L },
            "[16777217]"
        };
        // @formatter:on
        for (String command : Arrays.asList("INSERT", "UPSERT")) {
            for (Object value : values) {
                requests.clear();
                execute(command + " INTO t (id,j,v) VALUES (?,?,?)", 1L, value, new double[] { 1, 2 });
                Object request = requests.get(0);
                JsonObject entity = request instanceof InsertReq ? ((InsertReq) request).getData().get(0) : ((UpsertReq) request).getData().get(0);
                JsonElement json = entity.get("j");
                JsonElement expected = value instanceof String ? JsonParser.parseString((String) value) : new Gson().toJsonTree(value);
                assertTrue(json instanceof JsonElement);
                assertEquals(expected, json);
                assertEquals(new Gson().toJsonTree(Arrays.asList(1F, 2F)), entity.get("v"));
                FieldData encoded = ParamUtils.genFieldData("j", DataType.JSON, DataType.None, false, null, Collections.singletonList(json), false);
                assertEquals(expected, JsonParser.parseString(encoded.getScalars().getJsonData().getData(0).toStringUtf8()));
            }
        }
    }

    @Test
    public void scalarDefaultsAreTypedAndSerializedBySdk() throws Exception {
        execute("""
                CREATE TABLE t (id INT64 PRIMARY KEY, b BOOL DEFAULT true, i8 INT8 DEFAULT -128, i16 INT16 DEFAULT 32767,
                 i32 INT32 DEFAULT 7, i64 INT64 DEFAULT 9223372036854775807, f FLOAT DEFAULT -1.5, d DOUBLE DEFAULT 2.5,
                 s VARCHAR(20) DEFAULT 'a''b', v FLOAT_VECTOR(2))
                """);
        List<CreateCollectionReq.FieldSchema> fields = ((CreateCollectionReq) requests.get(0)).getCollectionSchema().getFieldSchemaList();
        Object[] expected = { true, (short) -128, (short) 32767, 7, Long.MAX_VALUE, -1.5F, 2.5D, "a'b" };
        for (int i = 0; i < expected.length; i++) {
            CreateCollectionReq.FieldSchema field = fields.get(i + 1);
            assertEquals(expected[i], field.getDefaultValue());
            FieldSchema wire = SchemaUtils.convertToGrpcFieldSchema(field);
            assertTrue(field.getName(), wire.hasDefaultValue());
        }
    }

    @Test
    public void invalidOrUnsupportedDefaultsFailBeforeCreate() throws Exception {
        // @formatter:off
        for (String definition : Arrays.asList(
            "a INT8 DEFAULT 128",
            "a INT16 DEFAULT 32768",
            "a INT32 DEFAULT 1.5",
            "a INT64 DEFAULT 9223372036854775808",
            "a FLOAT DEFAULT 'NaN'",
            "a DOUBLE DEFAULT 'Infinity'",
            "a BOOL DEFAULT bad",
            "a VARCHAR(2) DEFAULT 'abc'",
            "a VARCHAR(2) DEFAULT '中'",
            "a INT32 DEFAULT 1 DEFAULT 2",
            "a JSON DEFAULT '{}'",
            "v FLOAT_VECTOR(2) DEFAULT 1"
        )) {
            // @formatter:on
            reject("CREATE TABLE t (id INT64 PRIMARY KEY, " + definition + ")");
        }
        reject("CREATE TABLE t (id INT64 PRIMARY KEY DEFAULT 1, v FLOAT_VECTOR(2))");
    }

    @Test
    public void omittedDefaultFieldsSurviveSdkInsertAndUpsertConversion() throws Exception {
        schema = schema.toBuilder().clearFields().addFields(schema.getFields(0)).addFields(schema.getFields(3)).addFields(ParamUtils.ConvertField(FieldType.newBuilder().withName("age").withDataType(DataType.Int32).withDefaultValue(7).build())).build();
        io.milvus.response.DescCollResponseWrapper wrapper = new io.milvus.response.DescCollResponseWrapper(DescribeCollectionResponse.newBuilder().setSchema(schema).build());
        for (String operation : Arrays.asList("INSERT", "UPSERT")) {
            for (boolean explicitNull : new boolean[] { false, true }) {
                requests.clear();
                if (explicitNull) {
                    execute(operation + " INTO t (id,v,age) VALUES (?,?,?)", 1L, new float[] { 1, 2 }, null);
                } else {
                    execute(operation + " INTO t (id,v) VALUES (?,?)", 1L, new float[] { 1, 2 });
                }
                Object request = requests.get(0);
                List<FieldData> encoded;
                if (request instanceof UpsertReq) {
                    encoded = new DataUtils.InsertBuilderWrapper().convertGrpcUpsertRequest((UpsertReq) request, new ConvertUtils().convertDescCollectionResp(DescribeCollectionResponse.newBuilder().setSchema(schema).build())).getFieldsDataList();
                } else {
                    encoded = new DataUtils.InsertBuilderWrapper().convertGrpcInsertRequest((InsertReq) request, new ConvertUtils().convertDescCollectionResp(DescribeCollectionResponse.newBuilder().setSchema(schema).build())).getFieldsDataList();
                }
                FieldData age = encoded.stream().filter(field -> "age".equals(field.getFieldName())).findFirst().get();
                // V2 resolves the schema default when encoding row-based writes.
                assertEquals(Collections.singletonList(true), age.getValidDataList());
                assertEquals(Collections.singletonList(7), age.getScalars().getIntData().getDataList());
                assertEquals(7, wrapper.getFieldByName("age").getDefaultValue());
            }
        }
    }

    @Test
    public void showCreateBooleanCanBeExecutedAgain() throws Exception {
        execute("CREATE TABLE t (id INT64 PRIMARY KEY, enabled BOOL DEFAULT true, v FLOAT_VECTOR(2))");
        CreateCollectionReq original = (CreateCollectionReq) requests.get(0);
        CollectionSchema.Builder described = CollectionSchema.newBuilder().setName("t");
        original.getCollectionSchema().getFieldSchemaList().forEach(field -> described.addFields(SchemaUtils.convertToGrpcFieldSchema(field)));
        schema = described.build();
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            String ddl;
            try (ResultSet result = statement.executeQuery("SHOW CREATE TABLE t")) {
                assertTrue(result.next());
                ddl = result.getString("CREATE SCRIPT");
            }
            statement.executeUpdate(ddl);
            CreateCollectionReq recreated = (CreateCollectionReq) requests.get(1);
            assertEquals(3, recreated.getCollectionSchema().getFieldSchemaList().size());
            for (int i = 0; i < 3; i++) {
                assertEquals(SchemaUtils.convertToGrpcFieldSchema(original.getCollectionSchema().getFieldSchemaList().get(i)), SchemaUtils.convertToGrpcFieldSchema(recreated.getCollectionSchema().getFieldSchemaList().get(i)));
            }
        }
    }

    @Test
    public void showCreateRetainsDefaultValues() throws Exception {
        execute("CREATE TABLE t (id INT64 PRIMARY KEY, a INT32 DEFAULT -7, s VARCHAR(20) DEFAULT 'a''b', v FLOAT_VECTOR(2))");
        CreateCollectionReq created = (CreateCollectionReq) requests.get(0);
        CollectionSchema.Builder schemaBuilder = CollectionSchema.newBuilder().setName("t");
        created.getCollectionSchema().getFieldSchemaList().forEach(field -> schemaBuilder.addFields(SchemaUtils.convertToGrpcFieldSchema(field)));
        schema = schemaBuilder.build();
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            String ddl;
            try (ResultSet result = statement.executeQuery("SHOW CREATE TABLE t")) {
                assertTrue(result.next());
                ddl = result.getString("CREATE SCRIPT");
                assertTrue(ddl.contains("DEFAULT -7"));
                assertTrue(ddl.contains("DEFAULT 'a''b'"));
            }
            statement.executeUpdate(ddl);
            CreateCollectionReq recreated = (CreateCollectionReq) requests.get(1);
            assertEquals(-7, recreated.getCollectionSchema().getFieldSchemaList().get(1).getDefaultValue());
            assertEquals("a'b", recreated.getCollectionSchema().getFieldSchemaList().get(2).getDefaultValue());
        }
    }

    @Test
    public void smallIntegerMetadataAndObjectsAgree() throws Exception {
        QueryResults.Builder query = QueryResults.newBuilder();
        DataType[] types = { DataType.Int8, DataType.Int16, DataType.Int32 };
        for (int i = 0; i < types.length; i++) {
            query.addFieldsData(FieldData.newBuilder().setFieldName("i" + i).setType(types[i]).setScalars(ScalarField.newBuilder().setIntData(IntArray.newBuilder().addData(7))));
        }
        queryResult = query.build();
        Mockito.when(queryIterator.next()).thenReturn(new QueryResultsWrapper(queryResult).getRowRecords(), Collections.emptyList());
        CollectionSchema.Builder described = schema.toBuilder();
        for (int i = 0; i < types.length; i++) {
            described.addFields(FieldSchema.newBuilder().setName("i" + i).setDataType(types[i]));
        }
        schema = described.build();
        try (Connection conn = connect(); Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery("SELECT i2,i0,i1 FROM t")) {
            assertTrue(result.next());
            Class<?>[] expected = { Integer.class, Byte.class, Short.class };
            int[] jdbcTypes = { Types.INTEGER, Types.TINYINT, Types.SMALLINT };
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], result.getObject(i + 1).getClass());
                assertEquals(expected[i].getName(), result.getMetaData().getColumnClassName(i + 1));
                assertEquals(jdbcTypes[i], result.getMetaData().getColumnType(i + 1));
            }
        }
    }

    @Test
    public void propertyArgumentsDoNotLeakAcrossStatements() throws Exception {
        execute("SELECT id FROM t WHERE a > ? ORDER BY v <-> ? LIMIT ? WITH (custom=?);" + " DELETE FROM t WHERE a > ? AND a < ? LIMIT ?", 10, new float[] { 1, 2 }, 1, "first", 20, 30, 2);
        SearchReq search = (SearchReq) requests.get(0);
        assertEquals("first", JsonParser.parseString(new com.google.gson.Gson().toJson(search.getSearchParams())).getAsJsonObject().get("custom").getAsString());
        QueryIteratorReq delete = (QueryIteratorReq) requests.get(1);
        assertEquals(2L, delete.getLimit());
        assertEquals("a > {arg5} AND a < {arg6}", delete.getExpr());
        assertEquals(Map.of("arg5", 20, "arg6", 30), delete.getFilterTemplateValues());
    }

    @Test
    public void sqlLimitCapsResultsEvenIfSdkReturnsMore() throws Exception {
        for (String sql : Arrays.asList("SELECT id FROM t LIMIT 1", "SELECT id FROM t ORDER BY v <-> [1,2] LIMIT 1", "SELECT id FROM t WHERE v <-> [1,2] < 2 LIMIT 1")) {
            try (Connection conn = connect(); Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery(sql)) {
                assertTrue(result.next());
                assertFalse(result.next());
            }
        }
    }

    @Test
    public void resultMetadataUsesSdkTypesAndSelectOrder() throws Exception {
        queryResult = QueryResults.newBuilder().addFieldsData(longField("id", 9)).addFieldsData(FieldData.newBuilder().setFieldName("b").setType(DataType.Bool).setScalars(ScalarField.newBuilder().setBoolData(BoolArray.newBuilder().addData(true)))).addFieldsData(FieldData.newBuilder().setFieldName("f").setType(DataType.Float).setScalars(ScalarField.newBuilder().setFloatData(FloatArray.newBuilder().addData(1.5F)))).addFieldsData(FieldData.newBuilder().setFieldName("d").setType(DataType.Double).setScalars(ScalarField.newBuilder().setDoubleData(DoubleArray.newBuilder().addData(2.5)))).addFieldsData(FieldData.newBuilder().setFieldName("s").setType(DataType.VarChar).setScalars(ScalarField.newBuilder().setStringData(StringArray.newBuilder().addData("hello")))).addFieldsData(FieldData.newBuilder().setFieldName("j").setType(DataType.JSON).setScalars(ScalarField.newBuilder().setJsonData(JSONArray.newBuilder().addData(ByteString.copyFromUtf8("{\"n\":16777217}"))))).addFieldsData(FieldData.newBuilder().setFieldName("v").setType(DataType.FloatVector).setVectors(VectorField.newBuilder().setDim(2).setFloatVector(FloatArray.newBuilder().addData(1).addData(2)))).build();
        schema = schema.toBuilder().addFields(FieldSchema.newBuilder().setName("b").setDataType(DataType.Bool)).addFields(FieldSchema.newBuilder().setName("f").setDataType(DataType.Float)).addFields(FieldSchema.newBuilder().setName("d").setDataType(DataType.Double)).addFields(FieldSchema.newBuilder().setName("s").setDataType(DataType.VarChar).addTypeParams(KeyValuePair.newBuilder().setKey(MilvusCommandKeys.MAX_LENGTH).setValue("100"))).build();
        try (Connection conn = connect(); Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery("SELECT s,id,b,f,d,j,v FROM t LIMIT 1")) {
            int[] jdbcTypes = { Types.VARCHAR, Types.BIGINT, Types.BOOLEAN, Types.FLOAT, Types.DOUBLE, Types.OTHER, Types.ARRAY };
            String[] names = { "s", "id", "b", "f", "d", "j", "v" };
            assertTrue(result.next());
            for (int i = 0; i < names.length; i++) {
                assertEquals(names[i], result.getMetaData().getColumnName(i + 1));
                assertEquals(jdbcTypes[i], result.getMetaData().getColumnType(i + 1));
                assertTrue(Class.forName(result.getMetaData().getColumnClassName(i + 1)).isInstance(result.getObject(i + 1)));
            }
            assertEquals("Long", result.getMetaData().getColumnTypeName(2));
            assertEquals("JSON", result.getMetaData().getColumnTypeName(6));
        }
    }

    @Test
    public void emptyResultsStillExposeSchemaMetadata() throws Exception {
        queryResult = QueryResults.newBuilder().build();
        searchResult = SearchResults.newBuilder().setResults(SearchResultData.newBuilder().setNumQueries(1).setTopK(0)).build();
        for (String suffix : Arrays.asList("", " ORDER BY v <-> [1,2]", " WHERE v <-> [1,2] < 2 LIMIT 1")) {
            try (Connection conn = connect(); Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery("SELECT j,id,v FROM t" + suffix)) {
                assertFalse(result.next());
                assertEquals(3, result.getMetaData().getColumnCount());
                assertEquals(Types.OTHER, result.getMetaData().getColumnType(1));
                assertEquals(Types.BIGINT, result.getMetaData().getColumnType(2));
                assertEquals(Types.ARRAY, result.getMetaData().getColumnType(3));
            }
        }
    }

    @Test
    public void searchScoreIsNotRequestedAsAStoredField() throws Exception {
        try (Connection conn = connect(); Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery("SELECT score,id FROM t ORDER BY v <-> [1,2] LIMIT 1")) {
            assertTrue(result.next());
            assertEquals(2, result.getMetaData().getColumnCount());
            assertEquals("score", result.getMetaData().getColumnName(1));
            assertEquals(Types.FLOAT, result.getMetaData().getColumnType(1));
            assertEquals(0.1F, result.getFloat(1), 0);
            assertEquals(Collections.singletonList("id"), ((SearchReq) requests.get(0)).getOutputFields());
        }
    }
}
