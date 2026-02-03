package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.client.MilvusClient;
import io.milvus.grpc.ErrorCode;
import io.milvus.grpc.SearchResultData;
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        MilvusCommandInterceptor.addInterceptor(MilvusClient.class, (proxy, method, args) -> {
            if (expectedMethod.equals(method.getName())) {
                capturedArgs.addAll(Arrays.asList(args));
                // Mock Responses
                if ("query".equals(expectedMethod)) {
                    return R.success(io.milvus.grpc.QueryResults.newBuilder().setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success)).build());
                } else if ("search".equals(expectedMethod)) {
                    SearchResultData resultData = SearchResultData.newBuilder().setNumQueries(1).setTopK(0).build();
                    return R.success(SearchResults.newBuilder().setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success)).setResults(resultData).build());
                } else if ("insert".equals(expectedMethod)) {
                    return R.success(io.milvus.grpc.MutationResult.newBuilder().setInsertCnt(1).build());
                } else if ("delete".equals(expectedMethod)) {
                    return R.success(io.milvus.grpc.MutationResult.newBuilder().setDeleteCnt(1).build());
                } else if ("upsert".equals(expectedMethod)) { // For UPDATE
                    return R.success(io.milvus.grpc.MutationResult.newBuilder().setInsertCnt(1).build());
                }
            }
            if ("describeCollection".equals(method.getName())) {
                return R.success(io.milvus.grpc.DescribeCollectionResponse.newBuilder().setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success).build()).setSchema(io.milvus.grpc.CollectionSchema.newBuilder().setName("t").addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("id").setDataType(io.milvus.grpc.DataType.Int64).build()).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("val").setDataType(io.milvus.grpc.DataType.VarChar).addTypeParams(io.milvus.grpc.KeyValuePair.newBuilder().setKey("max_length").setValue("100").build()).build()).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("v").setDataType(io.milvus.grpc.DataType.FloatVector).addTypeParams(io.milvus.grpc.KeyValuePair.newBuilder().setKey("dim").setValue("2").build()).build()).build()).build());
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
        runTest("SELECT * FROM t WHERE a = ?", Arrays.asList(10), "query", args -> {
            QueryParam p = (QueryParam) args.get(0);
            assertExpr(p.getExpr(), "a == 10");
        });
        testCount.incrementAndGet();

        // 2. Binary Ops
        runTest("SELECT * FROM t WHERE a > ?", Arrays.asList(10), "query", args -> assertExpr(((QueryParam) args.get(0)).getExpr(), "a > 10"));
        runTest("SELECT * FROM t WHERE a < ?", Arrays.asList(10), "query", args -> assertExpr(((QueryParam) args.get(0)).getExpr(), "a < 10"));
        runTest("SELECT * FROM t WHERE a >= ?", Arrays.asList(10), "query", args -> assertExpr(((QueryParam) args.get(0)).getExpr(), "a >= 10"));
        runTest("SELECT * FROM t WHERE a <= ?", Arrays.asList(10), "query", args -> assertExpr(((QueryParam) args.get(0)).getExpr(), "a <= 10"));
        runTest("SELECT * FROM t WHERE a != ?", Arrays.asList(10), "query", args -> assertExpr(((QueryParam) args.get(0)).getExpr(), "a != 10"));
        runTest("SELECT * FROM t WHERE a <> ?", Arrays.asList(10), "query", args -> assertExpr(((QueryParam) args.get(0)).getExpr(), "a <> 10"));
        testCount.addAndGet(6);

        // 3. Like
        runTest("SELECT * FROM t WHERE a LIKE ?", Arrays.asList("pref%"), "query", args -> {
            assertExpr(((QueryParam) args.get(0)).getExpr(), "a like \"pref%\"");
        });
        testCount.incrementAndGet();

        // 4. IN (List Object)
        runTest("SELECT * FROM t WHERE a IN ?", Arrays.asList(Arrays.asList(1, 2)), "query", args -> {
            assertExpr(((QueryParam) args.get(0)).getExpr(), "a in [1, 2]");
        });
        testCount.incrementAndGet();

        // 5. IN [?, ?]
        runTest("SELECT * FROM t WHERE a IN [?, ?]", Arrays.asList(1, 2), "query", args -> {
            assertExpr(((QueryParam) args.get(0)).getExpr(), "a in [1, 2]");
        });
        testCount.incrementAndGet();

        // 6. Logic
        runTest("SELECT * FROM t WHERE a = ? AND b = ?", Arrays.asList(1, 2), "query", args -> {
            assertExpr(((QueryParam) args.get(0)).getExpr(), "a == 1 && b == 2");
        });
        testCount.incrementAndGet();

        // 7. Limit / Offset
        runTest("SELECT * FROM t LIMIT ?", Arrays.asList(10), "query", args -> {
            assert ((QueryParam) args.get(0)).getLimit() == 10;
        });
        runTest("SELECT * FROM t OFFSET ?", Arrays.asList(5), "query", args -> {
            assert ((QueryParam) args.get(0)).getOffset() == 5;
        });
        runTest("SELECT * FROM t LIMIT ? OFFSET ?", Arrays.asList(10, 5), "query", args -> {
            QueryParam p = (QueryParam) args.get(0);
            assert p.getLimit() == 10;
            assert p.getOffset() == 5;
        });
        testCount.addAndGet(3);

        // 8. Mixed Where + Limit + Offset
        runTest("SELECT * FROM t WHERE a = ? LIMIT ? OFFSET ?", Arrays.asList(99, 10, 5), "query", args -> {
            QueryParam p = (QueryParam) args.get(0);
            assertExpr(p.getExpr(), "a == 99");
            assert p.getLimit() == 10;
            assert p.getOffset() == 5;
        });
        testCount.incrementAndGet();

        // ==========================================
        // SEARCH Scenarios (ORDER BY vector <-> ?)
        // ==========================================

        // 9. Simple Search
        List<Float> vec = Arrays.asList(0.1f, 0.2f);
        runTest("SELECT * FROM t ORDER BY v <-> ?", Arrays.asList(vec), "search", args -> {
            SearchParam p = (SearchParam) args.get(0);
            assert p.getVectors().get(0).equals(vec);
        });
        testCount.incrementAndGet();

        // 10. Search with TopK (Limit)
        runTest("SELECT * FROM t ORDER BY v <-> ? LIMIT ?", Arrays.asList(vec, 5), "search", args -> {
            SearchParam p = (SearchParam) args.get(0);
            assert p.getTopK() == 5;
        });
        testCount.incrementAndGet();

        // 11. Search with Where
        // SQL: WHERE a = ? ORDER BY v <-> ?
        // EXPECTED: Param 1 -> Where, Param 2 -> Vector
        runTest("SELECT * FROM t WHERE a = ? ORDER BY v <-> ?", Arrays.asList(123, vec), "search", args -> {
            SearchParam p = (SearchParam) args.get(0);
            assertExpr(p.getExpr(), "a == 123");
            assert p.getVectors().get(0).equals(vec);
        });
        testCount.incrementAndGet();

        // 12. Search with Where + Limit
        runTest("SELECT * FROM t WHERE a = ? ORDER BY v <-> ? LIMIT ?", Arrays.asList(123, vec, 10), "search", args -> {
            SearchParam p = (SearchParam) args.get(0);
            assertExpr(p.getExpr(), "a == 123");
            assert p.getVectors().get(0).equals(vec);
            assert p.getTopK() == 10;
        });
        testCount.incrementAndGet();

        // 13. Search with Vector Literal [?, ?]
        runTest("SELECT * FROM t ORDER BY v <-> [?, ?]", Arrays.asList(0.1f, 0.2f), "search", args -> {
            SearchParam p = (SearchParam) args.get(0);
            assert p.getVectors().get(0).equals(Arrays.asList(0.1f, 0.2f));
        });
        testCount.incrementAndGet();

        // 14. Complex Search: WHERE (a=? OR b=?) AND c=? ORDER BY v <-> ? LIMIT ?
        // Params: 1, 2, 3, vec, 10
        runTest("SELECT * FROM t WHERE (a=? OR b=?) AND c=? ORDER BY v <-> ? LIMIT ?", Arrays.asList(1, 2, 3, vec, 10), "search", args -> {
            SearchParam p = (SearchParam) args.get(0);
            assertExpr(p.getExpr(), "(a == 1 || b == 2) && c == 3");
            assert p.getVectors().get(0).equals(vec);
            assert p.getTopK() == 10;
        });
        testCount.incrementAndGet();

        // 15. Vector Range Search: vector_range(v, vec, radius)
        runTest("SELECT * FROM t WHERE vector_range(v, ?, ?)", Arrays.asList(vec, 1.5), "search", args -> {
            SearchParam p = (SearchParam) args.get(0);
            assert p.getVectors().get(0).equals(vec);
            assert p.getParams().contains("\"radius\":1.5");
        });
        testCount.incrementAndGet();

        // 16. Vector Range + Scalar Filter: vector_range(v, ?, ?) AND a = ?
        String sqlRangeMixed = "SELECT * FROM t WHERE vector_range(v, ?, ?) AND a = ?";
        runTest(sqlRangeMixed, Arrays.asList(vec, 1.5, 999), "search", args -> {
            SearchParam p = (SearchParam) args.get(0);
            assert p.getVectors().get(0).equals(vec);
            // JSON construction in Adapter has no spaces: "radius":1.5
            assert p.getParams().contains("\"radius\":1.5");
            assertExpr(p.getExpr(), "a == 999");
        });
        testCount.incrementAndGet();

        // 17. Reverse Order: a = ? AND vector_range(v, ?, ?)
        // This fails if parser skipping logic is flawed.
        String sqlRangeMixed2 = "SELECT * FROM t WHERE a = ? AND vector_range(v, ?, ?)";
        runTest(sqlRangeMixed2, Arrays.asList(999, vec, 1.5), "search", args -> {
            SearchParam p = (SearchParam) args.get(0);
            assertExpr(p.getExpr(), "a == 999");
            assert p.getVectors().get(0).equals(vec);
            assert p.getParams().contains("\"radius\":1.5");
        });
        testCount.incrementAndGet();

        // ==========================================
        // INSERT Scenarios
        // ==========================================
        // 18. Insert Simple
        runTest("INSERT INTO t (id, val) VALUES (?, ?)", Arrays.asList(1L, "Test"), "insert", args -> {
            InsertParam p = (InsertParam) args.get(0);
            assert p.getFields().get(0).getValues().get(0).equals(1L);
            assert p.getFields().get(1).getValues().get(0).equals("Test");
        });
        testCount.incrementAndGet();

        // 19. Insert Vector
        runTest("INSERT INTO t (id, v) VALUES (?, ?)", Arrays.asList(1L, vec), "insert", args -> {
            InsertParam p = (InsertParam) args.get(0);
            assert p.getFields().get(0).getValues().get(0).equals(1L);
            assert p.getFields().get(1).getValues().get(0).equals(vec);
        });
        testCount.incrementAndGet();

        // ==========================================
        // UPDATE Scenarios
        // ==========================================
        // Milvus Update usually involves Search -> Delete -> Insert or simple Upsert.
        // The adapter might map it to QUERY first. 
        // Standard "UPDATE t SET a=? WHERE b=?" -> Query(b=?) -> Upsert(a=?)

        // 20. Update Simple
        runTest("UPDATE t SET a=? WHERE b=?", Arrays.asList("newVal", 10), "query", args -> {
            QueryParam p = (QueryParam) args.get(0);
            assertExpr(p.getExpr(), "b == 10");
            // Upsert is called after query returns. But since we mock empty query results, upsert won't be called.
            // Verification of "query" args proves the WHERE clause parsed correctly.
        });
        testCount.incrementAndGet();

        // 21. Update with Limit
        runTest("UPDATE t SET a=? WHERE b=? LIMIT ?", Arrays.asList("newVal", 10, 5), "query", args -> {
            QueryParam p = (QueryParam) args.get(0);
            assertExpr(p.getExpr(), "b == 10");
            assert p.getLimit() == 5;
        });
        testCount.incrementAndGet();

        // ==========================================
        // DELETE Scenarios
        // ==========================================

        // 22. Delete Simple (Maps to DeleteParam directly? No, Milvus delete is "delete by expr".
        runTest("DELETE FROM t WHERE a=?", Arrays.asList(10), "delete", args -> {
            DeleteParam p = (DeleteParam) args.get(0);
            assertExpr(p.getExpr(), "a == 10");
        });
        testCount.incrementAndGet();

        // 23. Delete by Range (Special case) -> Maps to SEARCH first!
        runTest("DELETE FROM t WHERE vector_range(v, ?, ?)", Arrays.asList(vec, 1.0), "search", args -> {
            SearchParam p = (SearchParam) args.get(0);
            assert p.getVectors().get(0).equals(vec);
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
