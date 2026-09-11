package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import io.milvus.grpc.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.vector.request.HybridSearchReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.utils.DataUtils;
import io.milvus.v2.utils.SchemaUtils;
import io.milvus.v2.utils.VectorUtils;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Test;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.*;

public class MilvusHybridTest extends AbstractJdbcTest {
    private              CreateCollectionReq.CollectionSchema schema;
    private              HybridSearchRequest                  hybrid;
    private              SearchRequest                        search;
    private              InsertRequest                        insert;
    private              CreateIndexReq                       index;
    private static final String                               DDL = """
            CREATE TABLE docs (id INT64 PRIMARY KEY, body VARCHAR(1000) WITH (enable_analyzer=true, analyzer_params='{"type":"standard"}'),
            dense FLOAT_VECTOR(2), sparse SPARSE_FLOAT_VECTOR, FUNCTION bm25_fn USING BM25 (body) INTO (sparse))
            """;

    private Connection connect() throws SQLException {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "createCollection":
                    schema = ((CreateCollectionReq) args[0]).getCollectionSchema();
                    return null;
                case "describeCollection":
                    return DescribeCollectionResp.builder().collectionName("docs").collectionSchema(schema).build();
                case "createIndex":
                    index = (CreateIndexReq) args[0];
                    return null;
                case "insert":
                    insert = new DataUtils.InsertBuilderWrapper().convertGrpcInsertRequest((InsertReq) args[0], DescribeCollectionResp.builder().collectionSchema(schema).build());
                    return v2Response("insert", MutationResult.newBuilder().setInsertCnt(1).build());
                case "search":
                    search = new VectorUtils().ConvertToGrpcSearchRequest((SearchReq) args[0]);
                    break;
                case "hybridSearch":
                    hybrid = new VectorUtils().ConvertToGrpcHybridSearchRequest((HybridSearchReq) args[0]);
                    break;
                default:
                    return null;
            }
            return v2Response("search", SearchResults.newBuilder().setResults(SearchResultData.newBuilder().setNumQueries(1).setTopK(2).addTopks(2).setPrimaryFieldName("id").setIds(IDs.newBuilder().setIntId(LongArray.newBuilder().addData(8).addData(3))).addScores(0.9F).addScores(0.5F)).build());
        });
        Properties props = new Properties();
        props.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        props.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/db1", props);
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    @Test
    public void bm25SchemaIndexInsertAndTextSearch() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(DDL);
            assertEquals(io.milvus.common.clientenum.FunctionType.BM25, schema.getFunctionList().get(0).getFunctionType());
            assertEquals(io.milvus.grpc.FunctionType.BM25, SchemaUtils.convertToGrpcFunction(schema.getFunctionList().get(0)).getType());
            stmt.executeUpdate("CREATE INDEX bm25_idx ON docs(sparse) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25, bm25_k1=1.2, bm25_b=0.75)");
            assertEquals(1.2D, index.getIndexParams().get(0).getExtraParams().get("bm25_k1"));
            stmt.executeUpdate("INSERT INTO docs (id,body,dense) VALUES (1,'hybrid search',[1,2])");
            assertEquals(3, insert.getFieldsDataCount()); // SDK omits the server-generated sparse output.
            try (ResultSet rs = stmt.executeQuery("SELECT id,score FROM docs ORDER BY sparse <?> 'hybrid search' LIMIT 2")) {
                assertTrue(rs.next());
                assertEquals(8, rs.getLong("id"));
            }
            assertEquals(PlaceholderType.VarChar, PlaceholderGroup.parseFrom(search.getPlaceholderGroup()).getPlaceholders(0).getType());
            String script;
            try (ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE docs")) {
                assertTrue(rs.next());
                script = rs.getString("CREATE SCRIPT");
            }
            FunctionSchema original = SchemaUtils.convertToGrpcFunction(schema.getFunctionList().get(0));
            stmt.executeUpdate(script);
            assertEquals(original, SchemaUtils.convertToGrpcFunction(schema.getFunctionList().get(0)));
            assertEquals(Boolean.TRUE, schema.getFieldSchemaList().get(1).getEnableAnalyzer());
        }
    }

    @Test
    public void hybridBindsInSqlOrderAndReturnsOneFusedResult() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(DDL);
            for (String rerank : Arrays.asList("reranker='rrf',k=20", "reranker='weighted',weights='[0.7,0.3]'")) {
                try (PreparedStatement ps = conn.prepareStatement("""
                        SELECT id,score FROM docs PARTITION p WHERE body = ? ORDER BY HYBRID
                        (dense <-> ? LIMIT ? WITH (nprobe=10), sparse <?> ? LIMIT ?) LIMIT ? OFFSET ? WITH (
                        """ + rerank + ")")) {
                    ps.setFetchSize(1);
                    ps.setString(1, "a\" or id > 0");
                    ps.setObject(2, new float[] { 1, 2 });
                    ps.setInt(3, 20);
                    ps.setString(4, "hybrid search");
                    ps.setInt(5, 30);
                    ps.setInt(6, 2);
                    ps.setInt(7, 1);
                    assertTrue(ps.execute());
                    try (ResultSet rs = ps.getResultSet()) {
                        assertTrue(rs.next());
                        assertEquals(8, rs.getLong(1));
                        assertEquals(0.9F, rs.getFloat("score"), 0.0001F);
                        assertTrue(rs.next());
                        assertEquals(3, rs.getLong(1));
                        assertFalse(rs.next());
                    }
                    assertFalse(ps.getMoreResults());
                    assertEquals(-1, ps.getUpdateCount());
                    assertEquals(2, hybrid.getRequestsCount());
                    assertEquals("db1", hybrid.getDbName());
                    assertEquals(Collections.singletonList("p"), hybrid.getPartitionNamesList());
                    assertEquals(1, hybrid.getRequests(0).getNq());
                    assertEquals(1, hybrid.getRequests(1).getNq());
                    assertEquals("body == {arg1}", hybrid.getRequests(0).getDsl());
                    assertEquals("a\" or id > 0", hybrid.getRequests(0).getExprTemplateValuesOrThrow("arg1").getStringVal());
                    assertEquals(hybrid.getRequests(0).getExprTemplateValuesMap(), hybrid.getRequests(1).getExprTemplateValuesMap());
                    assertEquals(hybrid.getRequests(0).getDsl(), hybrid.getRequests(1).getDsl());
                    Map<String, String> rank = new HashMap<>();
                    hybrid.getRankParamsList().forEach(p -> rank.put(p.getKey(), p.getValue()));
                    assertEquals("2", rank.get("limit"));
                    assertEquals("1", rank.get(MilvusCommandKeys.OFFSET));
                    assertEquals(rerank.contains("rrf") ? "rrf" : "weighted", rank.get("strategy"));
                }
            }
        }
    }

    @Test
    public void invalidHybridAndFunctionDefinitionsAreRejected() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(DDL);
            String base = "SELECT id FROM docs ORDER BY HYBRID (dense <-> [1,2] LIMIT 10, sparse <?> 'text' LIMIT 10)";
            // @formatter:off
            for (String sql : Arrays.asList(
                base + " WITH (reranker='rrf')",
                base + " LIMIT 2",
                base + " LIMIT 2 WITH (reranker='weighted',weights='[1]')",
                DDL.replace("enable_analyzer=true", "enable_analyzer=false"),
                DDL.replace("(body) INTO (sparse)", "(missing) INTO (sparse)")
            )) {
                // @formatter:on
                try {
                    stmt.execute(sql);
                    fail(sql);
                } catch (SQLException expected) {
                    assertNotNull(expected.getMessage());
                }
            }
            assertNull(hybrid);
        }
    }

    @Test
    public void perCandidateFiltersTimezoneAndFinalPrecisionShouldReachOfficialProtocol() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(DDL);
            try (PreparedStatement ps = conn.prepareStatement("""
                    SELECT id,score FROM docs WHERE body = ? ORDER BY HYBRID (
                        dense <-> ? WHERE id > ? OR body = ? LIMIT ? WITH(timezone=?,nprobe=?),
                        sparse <?> ? WHERE id IN ? LIMIT ? WITH(timezone=?)
                    ) LIMIT ? OFFSET ? WITH(reranker='rrf',round_decimal=?)
                    """)) {
                ps.setString(1, "tenant\" OR id > 0");
                ps.setObject(2, new float[] { 1, 2 });
                ps.setLong(3, 5);
                ps.setString(4, "local\" OR id > 0");
                ps.setInt(5, 20);
                ps.setString(6, "UTC");
                ps.setInt(7, 8);
                ps.setString(8, "hybrid search");
                ps.setObject(9, new long[] { 8, 9 });
                ps.setInt(10, 30);
                ps.setString(11, "Asia/Shanghai");
                ps.setInt(12, 2);
                ps.setInt(13, 1);
                ps.setInt(14, 3);
                try (ResultSet rows = ps.executeQuery()) {
                    assertTrue(rows.next());
                    assertEquals(8L, rows.getLong("id"));
                }
                assertFalse(ps.getMoreResults());
            }
            SearchRequest first = hybrid.getRequests(0);
            SearchRequest second = hybrid.getRequests(1);
            assertTrue(first.getDsl(), first.getDsl().startsWith("(body == {arg1}) && ("));
            assertTrue(first.getDsl().contains("id > {arg3}"));
            assertTrue(first.getDsl().contains("body == {arg4}"));
            assertFalse(first.getDsl().contains("tenant"));
            assertFalse(first.getDsl().contains("local"));
            assertEquals(new HashSet<>(Arrays.asList("arg1", "arg3", "arg4")), first.getExprTemplateValuesMap().keySet());
            assertEquals(new HashSet<>(Arrays.asList("arg1", "arg9")), second.getExprTemplateValuesMap().keySet());
            assertEquals("tenant\" OR id > 0", second.getExprTemplateValuesOrThrow("arg1").getStringVal());
            assertEquals("local\" OR id > 0", first.getExprTemplateValuesOrThrow("arg4").getStringVal());
            assertEquals("(body == {arg1}) && (id in {arg9})", second.getDsl());
            Map<String, String> firstParams = new HashMap<>();
            first.getSearchParamsList().forEach(p -> firstParams.put(p.getKey(), p.getValue()));
            Map<String, String> secondParams = new HashMap<>();
            second.getSearchParamsList().forEach(p -> secondParams.put(p.getKey(), p.getValue()));
            assertEquals("UTC", firstParams.get(MilvusCommandKeys.TIMEZONE));
            assertEquals("Asia/Shanghai", secondParams.get(MilvusCommandKeys.TIMEZONE));
            assertFalse(firstParams.get("params").contains(MilvusCommandKeys.TIMEZONE));
            Map<String, String> ranks = new HashMap<>();
            hybrid.getRankParamsList().forEach(p -> ranks.put(p.getKey(), p.getValue()));
            assertEquals("3", ranks.get(MilvusCommandKeys.ROUND_DECIMAL));
            assertEquals("2", ranks.get("limit"));
            assertEquals("1", ranks.get(MilvusCommandKeys.OFFSET));
        }
    }

    @Test
    public void absentSharedFilterAndEmptyTimezoneShouldRetainSdkDefaults() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(DDL);
            try (ResultSet rows = stmt.executeQuery("""
                    SELECT id FROM docs ORDER BY HYBRID (
                        dense <-> [1,2] WHERE id > 5 LIMIT 20 WITH(timezone=''),
                        sparse <?> 'text' LIMIT 30
                    ) LIMIT 2 WITH(reranker='rrf')
                    """)) {
                assertTrue(rows.next());
            }
            assertEquals("id > 5", hybrid.getRequests(0).getDsl());
            assertEquals("", hybrid.getRequests(1).getDsl());
            for (SearchRequest request : hybrid.getRequestsList()) {
                assertFalse(request.getSearchParamsList().stream().anyMatch(p -> p.getKey().equals(MilvusCommandKeys.TIMEZONE)));
                assertTrue(request.getExprTemplateValuesMap().isEmpty());
            }
            assertEquals("-1", hybrid.getRankParamsList().stream().filter(p -> p.getKey().equals(MilvusCommandKeys.ROUND_DECIMAL)).findFirst().orElseThrow().getValue());
        }
    }

    @Test
    public void misplacedOrInvalidHybridOptionsShouldFailBeforeSearch() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(DDL);
            String base = "SELECT id FROM docs ORDER BY HYBRID (dense <-> [1,2] %s LIMIT 10 %s, sparse <?> 'text' LIMIT 10) LIMIT 2 WITH(reranker='rrf'%s)";
            for (String sql : Arrays.asList(String.format(base, "WHERE dense <-> [1,2] < 3", "", ""), String.format(base, "", "WITH(timezone=5)", ""), String.format(base, "", "WITH(round_decimal=2)", ""), String.format(base, "", "", ",round_decimal='2'"), String.format(base, "", "", ",round_decimal=2.5"), String.format(base, "", "", ",round_decimal=2147483648"), String.format(base, "", "", ",timezone='UTC'"))) {
                assertThrows(sql, SQLException.class, () -> stmt.executeQuery(sql));
            }
            assertNull(hybrid);
        }
    }

    @Test
    public void textEmbeddingFunctionAndTextQueryUseOfficialProtocol() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                    CREATE TABLE docs (id INT64 PRIMARY KEY, body VARCHAR(1000), dense FLOAT_VECTOR(2),
                    FUNCTION embed USING TEXTEMBEDDING (body) INTO (dense) WITH (provider='openai',model_name='example-model'))
                    """);
            FunctionSchema function = SchemaUtils.convertToGrpcFunction(schema.getFunctionList().get(0));
            assertEquals(io.milvus.grpc.FunctionType.TextEmbedding, function.getType());
            assertEquals(Collections.singletonList("dense"), function.getOutputFieldNamesList());
            assertEquals("example-model", schema.getFunctionList().get(0).getParams().get("model_name"));
            stmt.executeUpdate("INSERT INTO docs (id,body) VALUES (1,'embed this')");
            assertEquals(2, insert.getFieldsDataCount());
            try (ResultSet rs = stmt.executeQuery("SELECT id FROM docs ORDER BY dense <=> 'search text' LIMIT 2")) {
                assertTrue(rs.next());
            }
            assertEquals(PlaceholderType.VarChar, PlaceholderGroup.parseFrom(search.getPlaceholderGroup()).getPlaceholders(0).getType());
        }
    }
}
