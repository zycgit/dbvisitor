package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import io.milvus.client.MilvusClient;
import io.milvus.grpc.*;
import io.milvus.param.R;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.Test;

public class MilvusExtremeArgsTest extends AbstractJdbcTest {

    private Connection getConnection() throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        prop.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://xxxxxx:19530", prop);
    }

    /*
     * SCENARIO 1: Complex Scalar Query with Logic, IN, LIMIT, OFFSET
     * SQL: SELECT * FROM book_vectors WHERE (book_id > ? OR word_count < ?) AND book_id IN ? LIMIT ? OFFSET ?
     */
    @Test
    public void testComplexScalarQuery() {
        List<Object> queryArgs = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClient.class, (proxy, method, args) -> {
            if ("query".equals(method.getName())) {
                queryArgs.addAll(Arrays.asList(args));

                // Return valid empty result to avoid NPE in wrapper
                QueryResults.Builder resultsBuilder = QueryResults.newBuilder();
                resultsBuilder.setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success).build());
                return R.success(resultsBuilder.build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM book_vectors WHERE (book_id > ? OR word_count < ?) AND book_id IN ? LIMIT ? OFFSET ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                // book_id > 1000
                ps.setLong(1, 1000L);
                // word_count < 500
                ps.setInt(2, 500);
                // book_id IN (2001, 2002)
                ps.setObject(3, Arrays.asList(2001L, 2002L));
                // LIMIT 10
                ps.setInt(4, 10);
                // OFFSET 5
                ps.setInt(5, 5);

                ps.executeQuery();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        assert queryArgs.size() == 1;
        QueryParam param = (QueryParam) queryArgs.get(0);

        // Check Limit/Offset
        assert param.getLimit() == 10;
        assert param.getOffset() == 5;

        // Check Expr
        String expr = param.getExpr();
        System.out.println("Generated Expr: " + expr);

        assert expr.contains("book_id > 1000");
        assert expr.contains("word_count < 500");
        assert expr.contains("book_id in [2001,2002]") || expr.contains("book_id in [2001, 2002]");
    }

    /*
     * SCENARIO 2: Extreme KNN Search
     * SQL: SELECT * FROM book_vectors WHERE book_id > ? ORDER BY book_intro <-> ? LIMIT ?
     * Note: OFFSET is usually not supported in Milvus SearchParam via standard SDK unless specifically handled (not present in current adapter logic for Search).
     */
    @Test
    public void testExtremeKNNSearch() {
        List<Object> searchArgs = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClient.class, (proxy, method, args) -> {
            if ("search".equals(method.getName())) {
                searchArgs.addAll(Arrays.asList(args));

                // Mock Valid Result Data (1 Query, 0 Hits)
                SearchResultData resultData = SearchResultData.newBuilder().setNumQueries(1).setTopK(0).build();

                SearchResults.Builder builder = SearchResults.newBuilder();
                builder.setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success).build());
                builder.setResults(resultData);
                return R.success(builder.build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM book_vectors WHERE book_id > ? ORDER BY book_intro <-> ? LIMIT ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, 500L); // Filter
                ps.setObject(2, Arrays.asList(0.1f, 0.2f)); // Vector
                ps.setInt(3, 20); // TopK

                ps.executeQuery();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        assert searchArgs.size() == 1;
        SearchParam param = (SearchParam) searchArgs.get(0);

        assert param.getExpr().contains("book_id > 500");
        assert param.getTopK() == 20;
        assert param.getVectorFieldName().equals("book_intro");
        assert param.getVectors().size() == 1;
        assert param.getVectors().get(0).equals(Arrays.asList(0.1f, 0.2f));
    }

    /*
     * SCENARIO 3: Extreme DML - Update with Vector Range
     * SQL: UPDATE book_vectors SET word_count = ? WHERE vector_range(book_intro, ?, ?) AND book_id > ?
     * Logic: RangeSearch -> DescribeCollection (PK) -> Delete (Batch) -> Insert (New) ??
     * Wait, standard UPDATE is: Query -> Memory Update -> Upsert.
     * With Vector Range, it should be: Search (Range) -> Memory Update -> Upsert.
     */
    @Test
    public void testUpdateWithVectorRange() {
        List<Object> searchArgs = new ArrayList<>();
        List<Object> upsertArgs = new ArrayList<>();

        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClient.class, (proxy, method, args) -> {
            // 1. Range Search
            if ("search".equals(method.getName())) {
                searchArgs.addAll(Arrays.asList(args));

                // MOCK Valid Result Data (1 Query, 0 Hits) to satisfy SearchResultsWrapper
                SearchResultData resultData = SearchResultData.newBuilder().setNumQueries(1).setTopK(0).build();

                SearchResults.Builder resultsBuilder = SearchResults.newBuilder();
                resultsBuilder.setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success).build());
                resultsBuilder.setResults(resultData);

                return R.success(resultsBuilder.build());
            } else if ("upsert".equals(method.getName())) {
                upsertArgs.addAll(Arrays.asList(args));
                return R.success(MutationResult.newBuilder().setInsertCnt(1).build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            // vector_range(col, target_vector, radius)
            String sql = "UPDATE book_vectors SET word_count = ? WHERE vector_range(book_intro, ?, ?) AND book_id > ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                // SET word_count = 999
                ps.setInt(1, 999);
                // Vector Term
                ps.setObject(2, Arrays.asList(0.5f, 0.5f));
                // Radius Term
                ps.setDouble(3, 1.5);
                // book_id Term
                ps.setLong(4, 100L);

                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        assert searchArgs.size() == 1;
        SearchParam param = (SearchParam) searchArgs.get(0);

        // Verify Range Search Params
        // The adapter might set specific params in JSON
        System.out.println("Search Params: " + param.getParams());
        assert param.getParams().contains("\"radius\": 1.5");

        assert param.getVectorFieldName().equals("book_intro");
        assert param.getExpr().contains("book_id > 100");
        assert param.getVectors().get(0).equals(Arrays.asList(0.5f, 0.5f));

        // Since search returned empty, Upsert won't be called. That's fine, we verified the "Condition Parsing".
        assert upsertArgs.isEmpty();
    }

    /*
     * SCENARIO 4: Extreme Delete - Delete by Range
     * SQL: DELETE FROM book_vectors WHERE vector_range(book_intro, ?, ?)
     */
    @Test
    public void testDeleteByVectorRange() {
        List<Object> searchArgs = new ArrayList<>();

        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClient.class, (proxy, method, args) -> {
            if ("search".equals(method.getName())) {
                searchArgs.addAll(Arrays.asList(args));

                SearchResultData resultData = SearchResultData.newBuilder().setNumQueries(1).setTopK(0).build();

                return R.success(SearchResults.newBuilder().setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success)).setResults(resultData).build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            String sql = "DELETE FROM book_vectors WHERE vector_range(book_intro, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, Arrays.asList(0.9f, 0.9f));
                ps.setDouble(2, 0.5);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        assert searchArgs.size() == 1;
        SearchParam param = (SearchParam) searchArgs.get(0);
        assert param.getParams().contains("\"radius\": 0.5");
        assert param.getVectors().get(0).equals(Arrays.asList(0.9f, 0.9f));
        // Adapter uses a hardcoded 16384 or similar TopK for Delete By Range
        assert param.getTopK() > 0;
    }
}