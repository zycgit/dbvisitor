package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.grpc.ErrorCode;
import io.milvus.grpc.SearchResultData;
import io.milvus.grpc.SearchResults;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.*;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;

public class MilvusExhaustiveArgsTest extends AbstractJdbcTest {
    private static final Logger logger = LoggerFactory.getLogger(MilvusExhaustiveArgsTest.class);

    private Connection getConnection() throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        prop.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://xxxxxx:19530", prop);
    }

    private interface SqlTestVerifier {
        void verify(List<Object> capturedArgs) throws Exception;
    }

    private void runTest(String sql, List<Object> params, String expectedMethod, SqlTestVerifier verifier) {
        List<Object> capturedArgs = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if (expectedMethod.equals(method.getName())) {
                capturedArgs.addAll(Arrays.asList(args));
                // Mock Responses
                if ("query".equals(expectedMethod)) {
                    return v2Response(method.getName(), io.milvus.grpc.QueryResults.newBuilder().setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success)).build());
                } else if ("search".equals(expectedMethod)) {
                    SearchResultData resultData = SearchResultData.newBuilder().setNumQueries(1).setTopK(0).build();
                    return v2Response(method.getName(), SearchResults.newBuilder().setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success)).setResults(resultData).build());
                } else if ("insert".equals(expectedMethod)) {
                    return v2Response(method.getName(), io.milvus.grpc.MutationResult.newBuilder().setInsertCnt(1).build());
                } else if ("delete".equals(expectedMethod)) {
                    return v2Response(method.getName(), io.milvus.grpc.MutationResult.newBuilder().setDeleteCnt(1).build());
                } else if ("upsert".equals(expectedMethod)) { // For UPDATE
                    return v2Response(method.getName(), io.milvus.grpc.MutationResult.newBuilder().setInsertCnt(1).build());
                } else if ("queryIterator".equals(expectedMethod)) {
                    QueryIterator iterator = PowerMockito.mock(QueryIterator.class);
                    PowerMockito.when(iterator.next()).thenReturn(new ArrayList<>());
                    return v2Response(method.getName(), iterator);
                } else if ("searchIteratorV2".equals(expectedMethod)) {
                    SearchIteratorV2 iterator = PowerMockito.mock(SearchIteratorV2.class);
                    PowerMockito.when(iterator.next()).thenReturn(new ArrayList<>());
                    return v2Response(method.getName(), iterator);
                }
            }
            if ("describeCollection".equals(method.getName())) {
                return v2Response(method.getName(), io.milvus.grpc.DescribeCollectionResponse.newBuilder().setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success).build()).setSchema(
                        io.milvus.grpc.CollectionSchema.newBuilder().setName("t").addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("a").setDataType(io.milvus.grpc.DataType.VarChar)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("b").setDataType(io.milvus.grpc.DataType.Int32)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("id").setDataType(io.milvus.grpc.DataType.Int64).setIsPrimaryKey(true).build())
                                .addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("val").setDataType(io.milvus.grpc.DataType.VarChar).addTypeParams(io.milvus.grpc.KeyValuePair.newBuilder().setKey(MilvusCommandKeys.MAX_LENGTH).setValue("100").build()).build()).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("v").setDataType(io.milvus.grpc.DataType.FloatVector).addTypeParams(io.milvus.grpc.KeyValuePair.newBuilder().setKey(MilvusCommandKeys.DIMENSION).setValue("2").build()).build()).build()).build());
            }
            if ("close".equals(method.getName())) {
                return null;
            }
            return null; // Let other calls pass or return null
        });

        logger.info("Testing SQL: " + sql);
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try {
                if (sql.trim().toUpperCase().startsWith("SELECT")) {
                    ps.executeQuery();
                } else {
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                // If the adapter throws "param size not match" or similar, catch it here
                throw new RuntimeException("Execution failed for SQL: " + sql + " | Params: " + params, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Connection failed", e);
        }

        if (capturedArgs.isEmpty()) {
            throw new RuntimeException("Method " + expectedMethod + " was NOT called for SQL: " + sql);
        }

        try {
            verifier.verify(capturedArgs);
        } catch (Exception e) {
            throw new RuntimeException("Verification failed for SQL: " + sql, e);
        }
    }

    @Test
    public void testExhaustiveParams() {
        AtomicInteger testCount = new AtomicInteger(0);

        // ==========================================
        // SELECT Scenarios
        // ==========================================

        // 1. Simple Where
        runTest("SELECT * FROM t WHERE a = ?", List.of(10), "queryIterator", args -> {
            QueryIteratorReq p = (QueryIteratorReq) args.get(0);
            assertExpr(p.getExpr(), "a == {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 10), p.getFilterTemplateValues());
        });
        testCount.incrementAndGet();

        // 2. Binary Ops
        runTest("SELECT * FROM t WHERE a > ?", List.of(10), "queryIterator", args -> { assertExpr(((QueryIteratorReq) args.get(0)).getExpr(), "a > {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 10), ((QueryIteratorReq) args.get(0)).getFilterTemplateValues()); });
        runTest("SELECT * FROM t WHERE a < ?", List.of(10), "queryIterator", args -> { assertExpr(((QueryIteratorReq) args.get(0)).getExpr(), "a < {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 10), ((QueryIteratorReq) args.get(0)).getFilterTemplateValues()); });
        runTest("SELECT * FROM t WHERE a >= ?", List.of(10), "queryIterator", args -> { assertExpr(((QueryIteratorReq) args.get(0)).getExpr(), "a >= {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 10), ((QueryIteratorReq) args.get(0)).getFilterTemplateValues()); });
        runTest("SELECT * FROM t WHERE a <= ?", List.of(10), "queryIterator", args -> { assertExpr(((QueryIteratorReq) args.get(0)).getExpr(), "a <= {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 10), ((QueryIteratorReq) args.get(0)).getFilterTemplateValues()); });
        runTest("SELECT * FROM t WHERE a != ?", List.of(10), "queryIterator", args -> { assertExpr(((QueryIteratorReq) args.get(0)).getExpr(), "a != {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 10), ((QueryIteratorReq) args.get(0)).getFilterTemplateValues()); });
        runTest("SELECT * FROM t WHERE a <> ?", List.of(10), "queryIterator", args -> { assertExpr(((QueryIteratorReq) args.get(0)).getExpr(), "a <> {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 10), ((QueryIteratorReq) args.get(0)).getFilterTemplateValues()); });
        testCount.addAndGet(6);

        // 3. Like
        runTest("SELECT * FROM t WHERE a LIKE ?", List.of("pref%"), "queryIterator", args -> {
            assertExpr(((QueryIteratorReq) args.get(0)).getExpr(), "a like {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", "pref%"), ((QueryIteratorReq) args.get(0)).getFilterTemplateValues());
        });
        testCount.incrementAndGet();

        // 4. IN (List Object)
        runTest("SELECT * FROM t WHERE a IN ?", List.of(Arrays.asList(1, 2)), "queryIterator", args -> {
            assertExpr(((QueryIteratorReq) args.get(0)).getExpr(), "a in {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", Arrays.asList(1, 2)), ((QueryIteratorReq) args.get(0)).getFilterTemplateValues());
        });
        testCount.incrementAndGet();

        // 5. IN [?, ?]
        runTest("SELECT * FROM t WHERE a IN [?, ?]", Arrays.asList(1, 2), "queryIterator", args -> {
            assertExpr(((QueryIteratorReq) args.get(0)).getExpr(), "a in {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", Arrays.asList(1, 2)), ((QueryIteratorReq) args.get(0)).getFilterTemplateValues());
        });
        testCount.incrementAndGet();

        // 6. Logic
        runTest("SELECT * FROM t WHERE a = ? AND b = ?", Arrays.asList(1, 2), "queryIterator", args -> {
            assertExpr(((QueryIteratorReq) args.get(0)).getExpr(), "a == {arg1} && b == {arg2}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 1, "arg2", 2), ((QueryIteratorReq) args.get(0)).getFilterTemplateValues());
        });
        testCount.incrementAndGet();

        // 7. Limit / Offset
        runTest("SELECT * FROM t LIMIT ?", List.of(10), "query", args -> {
            assert ((QueryReq) args.get(0)).getLimit() == 10;
        });
        runTest("SELECT * FROM t OFFSET ?", List.of(5), "queryIterator", args -> {
            assert ((QueryIteratorReq) args.get(0)).getOffset() == 0;
        });
        runTest("SELECT * FROM t LIMIT ? OFFSET ?", Arrays.asList(10, 5), "queryIterator", args -> {
            QueryIteratorReq p = (QueryIteratorReq) args.get(0);
            assert p.getLimit() == 15;
            assert p.getOffset() == 0;
        });
        testCount.addAndGet(3);

        // 8. Mixed Where + Limit + Offset
        runTest("SELECT * FROM t WHERE a = ? LIMIT ? OFFSET ?", Arrays.asList(99, 10, 5), "queryIterator", args -> {
            QueryIteratorReq p = (QueryIteratorReq) args.get(0);
            assertExpr(p.getExpr(), "a == {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 99), p.getFilterTemplateValues());
            assert p.getLimit() == 15;
            assert p.getOffset() == 0;
        });
        testCount.incrementAndGet();

        // ==========================================
        // SEARCH Scenarios (ORDER BY vector <-> ?)
        // ==========================================

        // 9. Simple Search with literal limit
        List<Float> vec = Arrays.asList(0.1f, 0.2f);
        runTest("SELECT * FROM t ORDER BY v <-> ? LIMIT 10", List.of(vec), "search", args -> {
            SearchReq p = (SearchReq) args.get(0);
            assert p.getData().get(0).getData().equals(vec);
        });
        testCount.incrementAndGet();

        // 10. Search with TopK (Limit)
        runTest("SELECT * FROM t ORDER BY v <-> ? LIMIT ?", Arrays.asList(vec, 5), "search", args -> {
            SearchReq p = (SearchReq) args.get(0);
            assert p.getTopK() == 5;
        });
        testCount.incrementAndGet();

        // 11. Search with Where and literal limit
        // SQL: WHERE a = ? ORDER BY v <-> ? LIMIT 10
        // EXPECTED: Param 1 -> Where, Param 2 -> Vector
        runTest("SELECT * FROM t WHERE a = ? ORDER BY v <-> ? LIMIT 10", Arrays.asList(123, vec), "search", args -> {
            SearchReq p = (SearchReq) args.get(0);
            assertExpr(p.getFilter(), "a == {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 123), p.getFilterTemplateValues());
            assert p.getData().get(0).getData().equals(vec);
        });
        testCount.incrementAndGet();

        // 12. Search with Where + Limit
        runTest("SELECT * FROM t WHERE a = ? ORDER BY v <-> ? LIMIT ?", Arrays.asList(123, vec, 10), "search", args -> {
            SearchReq p = (SearchReq) args.get(0);
            assertExpr(p.getFilter(), "a == {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 123), p.getFilterTemplateValues());
            assert p.getData().get(0).getData().equals(vec);
            assert p.getTopK() == 10;
        });
        testCount.incrementAndGet();

        // 13. Search with Vector Literal [?, ?]
        runTest("SELECT * FROM t ORDER BY v <-> [?, ?] LIMIT 10", Arrays.asList(0.1f, 0.2f), "search", args -> {
            SearchReq p = (SearchReq) args.get(0);
            assert p.getData().get(0).getData().equals(Arrays.asList(0.1f, 0.2f));
        });
        testCount.incrementAndGet();

        // 14. Complex Search: WHERE (a=? OR b=?) AND c=? ORDER BY v <-> ? LIMIT ?
        // Params: 1, 2, 3, vec, 10
        runTest("SELECT * FROM t WHERE (a=? OR b=?) AND c=? ORDER BY v <-> ? LIMIT ?", Arrays.asList(1, 2, 3, vec, 10), "search", args -> {
            SearchReq p = (SearchReq) args.get(0);
            assertExpr(p.getFilter(), "(a == {arg1} || b == {arg2}) && c == {arg3}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 1, "arg2", 2, "arg3", 3), p.getFilterTemplateValues());
            assert p.getData().get(0).getData().equals(vec);
            assert p.getTopK() == 10;
        });
        testCount.incrementAndGet();

        // 15. Vector Range Search: vector_range(v, vec, radius)
        runTest("SELECT * FROM t WHERE vector_range(v, ?, ?) LIMIT 10", Arrays.asList(vec, 1.5), "search", args -> {
            SearchReq p = (SearchReq) args.get(0);
            assert p.getData().get(0).getData().equals(vec);
            assert new com.google.gson.Gson().toJson(p.getSearchParams()).contains("\"radius\":1.5");
        });
        testCount.incrementAndGet();

        // 16. Vector Range + Scalar Filter: vector_range(v, ?, ?) AND a = ?
        String sqlRangeMixed = "SELECT * FROM t WHERE vector_range(v, ?, ?) AND a = ? LIMIT 10";
        runTest(sqlRangeMixed, Arrays.asList(vec, 1.5, 999), "search", args -> {
            SearchReq p = (SearchReq) args.get(0);
            assert p.getData().get(0).getData().equals(vec);
            // JSON construction in Adapter has no spaces: "radius":1.5
            assert new com.google.gson.Gson().toJson(p.getSearchParams()).contains("\"radius\":1.5");
            assertExpr(p.getFilter(), "a == {arg3}"); org.junit.Assert.assertEquals(java.util.Map.of("arg3", 999), p.getFilterTemplateValues());
        });
        testCount.incrementAndGet();

        // 17. Reverse Order: a = ? AND vector_range(v, ?, ?)
        // This fails if parser skipping logic is flawed.
        String sqlRangeMixed2 = "SELECT * FROM t WHERE a = ? AND vector_range(v, ?, ?) LIMIT 10";
        runTest(sqlRangeMixed2, Arrays.asList(999, vec, 1.5), "search", args -> {
            SearchReq p = (SearchReq) args.get(0);
            assertExpr(p.getFilter(), "a == {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 999), p.getFilterTemplateValues());
            assert p.getData().get(0).getData().equals(vec);
            assert new com.google.gson.Gson().toJson(p.getSearchParams()).contains("\"radius\":1.5");
        });
        testCount.incrementAndGet();

        // ==========================================
        // INSERT Scenarios
        // ==========================================
        // 18. Insert Simple
        runTest("INSERT INTO t (id, val) VALUES (?, ?)", Arrays.asList(1L, "Test"), "insert", args -> {
            InsertReq p = (InsertReq) args.get(0);
            assert p.getData().get(0).get("id").getAsLong() == 1L;
            assert p.getData().get(0).get("val").getAsString().equals("Test");
        });
        testCount.incrementAndGet();

        // 19. Insert Vector
        runTest("INSERT INTO t (id, v) VALUES (?, ?)", Arrays.asList(1L, vec), "insert", args -> {
            InsertReq p = (InsertReq) args.get(0);
            assert p.getData().get(0).get("id").getAsLong() == 1L;
            assert p.getData().get(0).get("v").equals(new com.google.gson.Gson().toJsonTree(vec));
        });
        testCount.incrementAndGet();

        // ==========================================
        // UPDATE Scenarios
        // ==========================================
        // Milvus Update usually involves Search -> Delete -> Insert or simple Upsert.
        // The adapter might map it to QUERY first. 
        // Standard "UPDATE t SET a=? WHERE b=? LIMIT n" -> Query(b=?) -> Upsert(a=?)

        // 20. Update Simple
        runTest("UPDATE t SET a=? WHERE b=? LIMIT 5", Arrays.asList("newVal", 10), "queryIterator", args -> {
            QueryIteratorReq p = (QueryIteratorReq) args.get(0);
            assertExpr(p.getExpr(), "b == {arg2}"); org.junit.Assert.assertEquals(java.util.Map.of("arg2", 10), p.getFilterTemplateValues());
            assert p.getLimit() == 5;
        });
        testCount.incrementAndGet();

        // 21. Update with Limit
        runTest("UPDATE t SET a=? WHERE b=? LIMIT ?", Arrays.asList("newVal", 10, 5), "queryIterator", args -> {
            QueryIteratorReq p = (QueryIteratorReq) args.get(0);
            assertExpr(p.getExpr(), "b == {arg2}"); org.junit.Assert.assertEquals(java.util.Map.of("arg2", 10), p.getFilterTemplateValues());
            assert p.getLimit() == 5;
        });
        testCount.incrementAndGet();

        // ==========================================
        // DELETE Scenarios
        // ==========================================

        // 22. Delete Simple (Maps to DeleteReq directly? No, Milvus delete is "delete by expr".
        runTest("DELETE FROM t WHERE a=?", List.of(10), "delete", args -> {
            DeleteReq p = (DeleteReq) args.get(0);
            assertExpr(p.getFilter(), "a == {arg1}"); org.junit.Assert.assertEquals(java.util.Map.of("arg1", 10), p.getFilterTemplateValues());
        });
        testCount.incrementAndGet();

        // 23. Delete by Range (Special case) -> Maps to SEARCH first!
        runTest("DELETE FROM t WHERE vector_range(v, ?, ?) LIMIT 10", Arrays.asList(vec, 1.0), "searchIteratorV2", args -> {
            SearchIteratorReqV2 p = (SearchIteratorReqV2) args.get(0);
            assert p.getVectors().stream().map(v -> v.getData()).collect(java.util.stream.Collectors.toList()).get(0).equals(vec);
            assert p.getTopK() == 10;
        });
        testCount.incrementAndGet();

        System.out.println("Ran " + testCount.get() + " distinct patterns successfully.");
    }

    private void assertExpr(String actual, String expected) {
        // Normalize
        String a = actual.replace(" ", "").replace("==", "=").replace("&&", "and").replace("||", "or").toLowerCase();
        String e = expected.replace(" ", "").replace("==", "=").replace("&&", "and").replace("||", "or").toLowerCase();
        // Handle "in[1,2]" format
        if (!a.equals(e)) {
            // Try lenient match for list content
            if (a.contains(e))
                return;
            throw new RuntimeException("Expression mismatch! Expected similar to: " + expected + ", Got: " + actual);
        }
    }
}
