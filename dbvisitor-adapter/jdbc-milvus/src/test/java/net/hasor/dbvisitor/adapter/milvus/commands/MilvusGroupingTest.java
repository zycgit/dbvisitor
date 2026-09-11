package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import io.milvus.grpc.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.HybridSearchReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.SearchResp;
import io.milvus.v2.utils.VectorUtils;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.*;

public class MilvusGroupingTest {
    private final List<String>        methods      = new ArrayList<>();
    private       SearchReq           search;
    private       SearchRequest       wireSearch;
    private       HybridSearchReq     hybrid;
    private       HybridSearchRequest wireHybrid;
    private       boolean             fail;
    private       int                 returnedRows = 6;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            String name = method.getName();
            if (name.equals("getServerVersion")) {
                return "v2.6.2";
            }
            if (name.equals("describeCollection")) {
                CollectionSchema schema = CollectionSchema.newBuilder().setName("docs").addFields(FieldSchema.newBuilder().setName("id").setDataType(DataType.Int64).setIsPrimaryKey(true)).addFields(FieldSchema.newBuilder().setName("category").setDataType(DataType.VarChar)).addFields(FieldSchema.newBuilder().setName("v").setDataType(DataType.FloatVector)).addFields(FieldSchema.newBuilder().setName("w").setDataType(DataType.FloatVector)).build();
                return v2Response(name, DescribeCollectionResponse.newBuilder().setSchema(schema).build());
            }
            methods.add(name);
            if (name.equals("search")) {
                search = (SearchReq) args[0];
                wireSearch = new VectorUtils().ConvertToGrpcSearchRequest(search);
            } else if (name.equals("hybridSearch")) {
                hybrid = (HybridSearchReq) args[0];
                wireHybrid = new VectorUtils().ConvertToGrpcHybridSearchRequest(hybrid);
            } else {
                throw new AssertionError("Grouped search must not use " + name);
            }
            if (fail) {
                throw new IllegalStateException("grouping not available for this index");
            }
            List<SearchResp.SearchResult> rows = new ArrayList<>();
            for (int i = 1; i <= returnedRows; i++) {
                rows.add(SearchResp.SearchResult.builder().id((long) i).primaryKey("id").score(i / 10F).entity(Collections.singletonMap("category", "group" + ((i + 1) / 2))).build());
            }
            return SearchResp.builder().searchResults(Collections.singletonList(rows)).build();
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
    public void groupLimitMustNotTruncateRowsOrChooseAnIteratorFromFetchSize() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.setFetchSize(1);
            try (ResultSet rows = statement.executeQuery("SELECT id FROM docs ORDER BY v <-> [0,0] WITH (group_by_field='category', group_limit=3, group_size=2)")) {
                assertEquals(1, rows.getMetaData().getColumnCount());
                assertEquals(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L), ids(rows));
            }
        }
        assertEquals(Collections.singletonList("search"), methods);
        assertEquals(3, search.getTopK());
        assertEquals(Integer.valueOf(2), search.getGroupSize());
        assertNull(search.getStrictGroupSize());
        assertEquals("category", search.getGroupByFieldName());
        assertEquals("3", params(wireSearch.getSearchParamsList()).get("topk"));
        assertEquals("category", params(wireSearch.getSearchParamsList()).get(MilvusCommandKeys.GROUP_BY_FIELD));
        assertEquals("2", params(wireSearch.getSearchParamsList()).get(MilvusCommandKeys.GROUP_SIZE));
        assertFalse(search.getSearchParams().containsKey(MilvusCommandKeys.GROUP_LIMIT));
        assertFalse(search.getSearchParams().containsKey(MilvusCommandKeys.GROUP_BY_FIELD));
    }

    @Test
    public void boundGroupWindowAndSqlRowWindowMustRemainIndependent() throws SQLException {
        String sql = "SELECT id,category,score FROM docs WHERE category != ? ORDER BY v <-> ? LIMIT ? OFFSET ? " + "WITH (group_by_field=?, group_limit=?, group_offset=?, group_size=?, strict_group_size=?, nprobe=8)";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setFetchSize(1);
            statement.setMaxRows(1);
            statement.setString(1, "quoted\" or id > 0");
            statement.setObject(2, new float[] { 0, 0 });
            statement.setInt(3, 4);
            statement.setInt(4, 1);
            statement.setString(5, "category");
            statement.setInt(6, 3);
            statement.setLong(7, 2);
            statement.setInt(8, 2);
            statement.setBoolean(9, true);
            try (ResultSet rows = statement.executeQuery()) {
                assertEquals(Collections.singletonList(2L), ids(rows));
            }
            statement.setMaxRows(0);
            try (ResultSet rows = statement.executeQuery()) {
                assertEquals(Arrays.asList(2L, 3L, 4L, 5L), ids(rows));
            }
        }
        assertEquals(3, search.getTopK());
        assertEquals(2, search.getOffset());
        assertEquals("db1", search.getDatabaseName());
        assertEquals(Boolean.TRUE, search.getStrictGroupSize());
        assertEquals("category != {arg1}", search.getFilter());
        assertEquals("quoted\" or id > 0", search.getFilterTemplateValues().get("arg1"));
        assertEquals(8L, ((Number) search.getSearchParams().get("nprobe")).longValue());
        assertEquals("2", params(wireSearch.getSearchParamsList()).get("offset"));
        assertEquals("true", params(wireSearch.getSearchParamsList()).get(MilvusCommandKeys.STRICT_GROUP_SIZE));
    }

    @Test
    public void hybridGroupingMustApplyOnlyToFusedRequestAndKeepOneResultSet() throws SQLException {
        String sql = "SELECT id FROM docs WHERE category='keep' ORDER BY HYBRID (v <-> [0,0] LIMIT 10, w <-> [0,0] LIMIT 8) " + "OFFSET 2 WITH (reranker='rrf', group_by_field='category', group_limit=3, group_offset=1, group_size=2, strict_group_size=false)";
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.setFetchSize(1);
            assertTrue(statement.execute(sql));
            try (ResultSet rows = statement.getResultSet()) {
                assertEquals(Arrays.asList(3L, 4L, 5L, 6L), ids(rows));
            }
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
        assertEquals(Collections.singletonList("hybridSearch"), methods);
        assertEquals(3, hybrid.getLimit());
        assertEquals(1, hybrid.getOffset());
        assertEquals(Integer.valueOf(2), hybrid.getGroupSize());
        assertEquals(Boolean.FALSE, hybrid.getStrictGroupSize());
        assertEquals("category", hybrid.getGroupByFieldName());
        assertEquals("category", params(wireHybrid.getRankParamsList()).get(MilvusCommandKeys.GROUP_BY_FIELD));
        assertEquals(10, hybrid.getSearchRequests().get(0).getLimit());
        assertEquals(8, hybrid.getSearchRequests().get(1).getLimit());
        for (SearchRequest candidate : wireHybrid.getRequestsList()) {
            assertFalse(params(candidate.getSearchParamsList()).containsKey(MilvusCommandKeys.GROUP_BY_FIELD));
            assertEquals("category == \"keep\"", candidate.getDsl());
        }
    }

    @Test
    public void invalidGroupOptionsMustNotBeSilentlyIgnored() throws SQLException {
        String base = "SELECT id FROM docs ORDER BY v <-> [0,0] LIMIT 2 WITH (";
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String options : Arrays.asList("group_by_field='category'", "group_limit=3", "group_by_field='',group_limit=3", "group_by_field=10,group_limit=3", "group_by_field='category',group_limit=0", "group_by_field='category',group_limit=2147483648", "group_by_field='category',group_limit=1.5", "group_by_field='category',group_limit=3,group_size=0", "group_by_field='category',group_limit=3,group_size=2147483648", "group_by_field='category',group_limit=3,strict_group_size='true'", "group_by_field='category',group_limit=3,group_offset=null",
                    "group_by_field='category',group_limit=3,group_offset=9223372036854775807")) {
                assertThrows(options, SQLException.class, () -> statement.executeQuery(base + options + ")"));
            }
            assertThrows(SQLException.class, () -> statement.executeQuery("SELECT id FROM docs WITH (group_by_field='category',group_limit=3)"));
            assertThrows(SQLException.class, () -> statement.executeQuery("SELECT id FROM docs ORDER BY HYBRID (v <-> [0,0] LIMIT 8 WITH (group_size=2)) LIMIT 3 WITH (reranker='rrf')"));
        }
        assertTrue(methods.isEmpty());
    }

    @Test
    public void emptyOrRejectedNativeGroupsMustNotTriggerClientGroupingFallback() throws SQLException {
        String sql = "SELECT id FROM docs ORDER BY v <-> [0,0] WITH (group_by_field='category',group_limit=3)";
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            returnedRows = 0;
            try (ResultSet rows = statement.executeQuery(sql)) {
                assertEquals(1, rows.getMetaData().getColumnCount());
                assertFalse(rows.next());
            }
            assertNull(search.getGroupSize());
            fail = true;
            SQLException error = assertThrows(SQLException.class, () -> statement.executeQuery(sql));
            assertTrue(error.getMessage().contains("grouping not available for this index"));
        }
        assertEquals(Arrays.asList("search", "search"), methods);
    }

    private List<Long> ids(ResultSet rows) throws SQLException {
        List<Long> ids = new ArrayList<>();
        while (rows.next()) {
            ids.add(rows.getLong("id"));
        }
        return ids;
    }

    private Map<String, String> params(List<KeyValuePair> values) {
        Map<String, String> result = new LinkedHashMap<>();
        for (KeyValuePair value : values) {
            result.put(value.getKey(), value.getValue());
        }
        return result;
    }
}
