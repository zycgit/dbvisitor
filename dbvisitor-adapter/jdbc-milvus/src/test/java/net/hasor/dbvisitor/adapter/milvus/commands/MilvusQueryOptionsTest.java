package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import io.milvus.grpc.CollectionSchema;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.FieldSchema;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchIteratorReqV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.*;

public class MilvusQueryOptionsTest {
    private final List<Object> requests = new ArrayList<>();
    private final List<String> methods  = new ArrayList<>();
    private       Connection   connection;

    @Before
    public void connect() throws SQLException {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            String name = method.getName();
            if (name.equals("getServerVersion")) {
                return "v2.6.2";
            }
            methods.add(name);
            if (name.equals("describeCollection")) {
                CollectionSchema schema = CollectionSchema.newBuilder().setName("t").addFields(FieldSchema.newBuilder().setName("id").setDataType(DataType.Int64).setIsPrimaryKey(true)).addFields(FieldSchema.newBuilder().setName("v").setDataType(DataType.FloatVector)).build();
                return v2Response(name, DescribeCollectionResponse.newBuilder().setSchema(schema).build());
            }
            requests.add(args[0]);
            switch (name) {
                case "query":
                    return QueryResp.builder().queryResults(Collections.singletonList(QueryResp.QueryResult.builder().entity(Collections.singletonMap("count(*)", 42L)).build())).build();
                case "queryIterator":
                    QueryIterator query = Mockito.mock(QueryIterator.class);
                    Mockito.when(query.next()).thenReturn(Collections.emptyList());
                    return query;
                case "search":
                    return SearchResp.builder().searchResults(Collections.emptyList()).build();
                case "searchIteratorV2":
                    SearchIteratorV2 search = Mockito.mock(SearchIteratorV2.class);
                    Mockito.when(search.next()).thenReturn(Collections.emptyList());
                    return search;
                default:
                    throw new AssertionError("Unexpected SDK call " + name);
            }
        });
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        connection = new JdbcDriver().connect("jdbc:dbvisitor:milvus://mock:19530/options_db", properties);
    }

    @After
    public void close() throws SQLException {
        try {
            if (connection != null) {
                connection.close();
            }
        } finally {
            MilvusCommandInterceptor.resetInterceptor();
        }
    }

    @Test
    public void scalarOptionsShouldSurviveSwitchingToIteratorAndKeepParameterOrder() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM t PARTITION p WHERE id > ? LIMIT ? OFFSET ? WITH(ignore_growing=?,timezone=?)")) {
            statement.setLong(1, 5);
            statement.setInt(2, 2);
            statement.setInt(3, 0);
            statement.setBoolean(4, true);
            statement.setObject(5, "Asia/Shanghai", Types.VARCHAR);
            for (int fetchSize : new int[] { 10, 1 }) {
                statement.setFetchSize(fetchSize);
                try (ResultSet ignored = statement.executeQuery()) {
                    ignored.next();
                }
            }
        }
        QueryReq bounded = (QueryReq) requests.get(0);
        QueryIteratorReq paged = (QueryIteratorReq) requests.get(1);
        assertTrue(bounded.isIgnoreGrowing());
        assertTrue(paged.isIgnoreGrowing());
        assertEquals("Asia/Shanghai", bounded.getTimezone());
        assertEquals(bounded.getTimezone(), paged.getTimezone());
        assertEquals("options_db", paged.getDatabaseName());
        assertEquals(Collections.singletonList("p"), paged.getPartitionNames());
        assertEquals(bounded.getFilter(), paged.getExpr());
        assertEquals(Collections.singletonMap("arg1", 5L), paged.getFilterTemplateValues());
        assertEquals(bounded.getFilterTemplateValues(), paged.getFilterTemplateValues());
        assertEquals(2L, paged.getLimit());
        assertEquals(1L, paged.getBatchSize());
    }

    @Test
    public void nativeCountAndCountHintShouldConsumeOptionsWithoutScanningEntities() throws SQLException {
        for (String prefix : Arrays.asList("COUNT", "SELECT COUNT(*)", "/*+ overwrite_find_as_count */ SELECT id")) {
            try (PreparedStatement statement = connection.prepareStatement(prefix + " FROM t WHERE id > ? WITH(ignore_growing=?,timezone=?)")) {
                statement.setLong(1, 9);
                statement.setBoolean(2, true);
                statement.setString(3, "UTC");
                try (ResultSet rows = statement.executeQuery()) {
                    assertTrue(rows.next());
                    assertEquals(42L, rows.getLong(1));
                    assertFalse(rows.next());
                }
            }
        }
        assertEquals(Arrays.asList("query", "query", "query"), methods);
        for (Object request : requests) {
            QueryReq query = (QueryReq) request;
            assertTrue(query.isIgnoreGrowing());
            assertEquals("UTC", query.getTimezone());
            assertEquals(Collections.singletonList("count(*)"), query.getOutputFields());
            assertEquals(Collections.singletonMap("arg1", 9L), query.getFilterTemplateValues());
        }
    }

    @Test
    public void searchOptionsShouldUseSdkFieldsForBoundedIteratorAndGroupedPaths() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.setFetchSize(2);
            for (String suffix : Arrays.asList("LIMIT 1", "LIMIT 3", "")) {
                try (ResultSet rows = statement.executeQuery("SELECT id FROM t ORDER BY v <-> [1,2] " + suffix + " WITH(timezone='UTC',ignore_growing=true,round_decimal=2,nprobe=8)")) {
                    assertFalse(rows.next());
                }
            }
            try (ResultSet rows = statement.executeQuery("SELECT id FROM t ORDER BY v <-> [1,2] " + "WITH(group_by_field='id',group_limit=2,timezone='UTC',ignore_growing=true,round_decimal=2,nprobe=8)")) {
                assertFalse(rows.next());
            }
        }
        assertEquals(4, requests.size());
        for (Object request : requests) {
            Map<String, Object> params;
            if (request instanceof SearchReq query) {
                assertTrue(query.isIgnoreGrowing());
                assertEquals("UTC", query.getTimezone());
                assertEquals(2, query.getRoundDecimal());
                params = query.getSearchParams();
            } else {
                SearchIteratorReqV2 query = (SearchIteratorReqV2) request;
                assertTrue(query.isIgnoreGrowing());
                assertEquals("UTC", query.getTimezone());
                assertEquals(2, query.getRoundDecimal());
                params = query.getSearchParams();
            }
            assertEquals(8, ((Number) params.get("nprobe")).intValue());
            assertFalse(params.containsKey(MilvusCommandKeys.TIMEZONE));
            assertFalse(params.containsKey(MilvusCommandKeys.IGNORE_GROWING));
            assertFalse(params.containsKey(MilvusCommandKeys.ROUND_DECIMAL));
        }
    }

    @Test
    public void unknownScalarOptionsAndInvalidTypesMustNotBeSilentlyIgnored() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String prefix : Arrays.asList("SELECT id", "COUNT", "SELECT COUNT(*)", "/*+ overwrite_find_as_count */ SELECT id")) {
                for (String option : Arrays.asList("nprobe=4", "limit=1", "offset=1", "round_decimal=2", "ignore_growing='true'", "ignore_growing=1", "timezone=5")) {
                    String sql = prefix + " FROM t WITH(" + option + ")";
                    assertThrows(sql, SQLException.class, () -> statement.executeQuery(sql));
                }
            }
        }
        assertTrue(methods.isEmpty());
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM t WITH(timezone=?)")) {
            statement.setNull(1, Types.VARCHAR);
            assertThrows(SQLException.class, statement::executeQuery);
        }
        assertTrue(methods.isEmpty());
    }

    @Test
    public void repeatedBindingsShouldNotLeakReadOptionsIntoOtherExecutions() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("COUNT FROM t WHERE id > ? WITH(timezone=?,ignore_growing=?)")) {
            for (boolean ignoreGrowing : new boolean[] { true, false }) {
                statement.setInt(1, 7);
                statement.setString(2, ignoreGrowing ? "UTC" : "Asia/Shanghai");
                statement.setBoolean(3, ignoreGrowing);
                try (ResultSet rows = statement.executeQuery()) {
                    assertTrue(rows.next());
                }
            }
        }
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("COUNT FROM t")) {
            assertTrue(rows.next());
        }
        QueryReq first = (QueryReq) requests.get(0);
        QueryReq second = (QueryReq) requests.get(1);
        QueryReq defaults = (QueryReq) requests.get(2);
        assertTrue(first.isIgnoreGrowing());
        assertFalse(second.isIgnoreGrowing());
        assertEquals("UTC", first.getTimezone());
        assertEquals("Asia/Shanghai", second.getTimezone());
        assertFalse(defaults.isIgnoreGrowing());
        assertEquals(QueryReq.builder().build().getTimezone(), defaults.getTimezone());
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("COUNT FROM t WITH(timezone='')")) {
            assertTrue(rows.next());
        }
        QueryReq explicitDefault = (QueryReq) requests.get(3);
        assertEquals("", explicitDefault.getTimezone());
        assertFalse(new io.milvus.v2.utils.VectorUtils().ConvertToGrpcQueryRequest(explicitDefault).getQueryParamsList().stream().anyMatch(param -> param.getKey().equals(io.milvus.param.Constant.TIMEZONE)));
    }

    @Test
    public void duplicatePropertyBindingsShouldFollowExistingLastValueSemantics() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("COUNT FROM t WHERE id > ? WITH(timezone=?,timezone=?,ignore_growing=?)")) {
            statement.setLong(1, 12L);
            statement.setString(2, "UTC");
            statement.setString(3, "Asia/Shanghai");
            statement.setBoolean(4, true);
            try (ResultSet rows = statement.executeQuery()) {
                assertTrue(rows.next());
            }
        }
        QueryReq query = (QueryReq) requests.get(0);
        assertEquals("Asia/Shanghai", query.getTimezone());
        assertTrue(query.isIgnoreGrowing());
        assertEquals(Collections.singletonMap("arg1", 12L), query.getFilterTemplateValues());
    }
}
