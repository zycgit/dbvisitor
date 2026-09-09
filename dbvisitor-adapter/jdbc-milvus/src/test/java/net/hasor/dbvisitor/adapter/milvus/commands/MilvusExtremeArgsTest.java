package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import io.milvus.grpc.*;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.SearchIteratorReqV2;
import io.milvus.v2.service.vector.request.SearchReq;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;

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
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("describeCollection".equals(method.getName())) {
                return v2Response(method.getName(), io.milvus.grpc.DescribeCollectionResponse.newBuilder()
                        .setSchema(io.milvus.grpc.CollectionSchema.newBuilder().addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_id").setDataType(io.milvus.grpc.DataType.Int64).setIsPrimaryKey(true)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("word_count").setDataType(io.milvus.grpc.DataType.Int32)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("title").setDataType(io.milvus.grpc.DataType.VarChar)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_intro").setDataType(io.milvus.grpc.DataType.FloatVector))).build());
            }
            if ("queryIterator".equals(method.getName())) {
                queryArgs.addAll(Arrays.asList(args));

                // Return valid empty result to avoid NPE in wrapper
                QueryResults.Builder resultsBuilder = QueryResults.newBuilder();
                resultsBuilder.setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success).build());
                return v2Response(method.getName(), resultsBuilder.build());
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
        QueryIteratorReq param = (QueryIteratorReq) queryArgs.get(0);

        // Check Limit/Offset
        assert param.getLimit() == 15;
        assert param.getOffset() == 0;

        // Check Expr
        String expr = param.getExpr();
        System.out.println("Generated Expr: " + expr);

        assert expr.equals("(book_id > {arg1} OR word_count < {arg2}) AND book_id in {arg3}");
        assert param.getFilterTemplateValues().equals(java.util.Map.of("arg1", 1000L, "arg2", 500, "arg3", Arrays.asList(2001L, 2002L)));
    }

    /*
     * SCENARIO 2: Extreme KNN Search
     * SQL: SELECT * FROM book_vectors WHERE book_id > ? ORDER BY book_intro <-> ? LIMIT ?
     * Note: OFFSET is usually not supported in Milvus SearchReq via standard SDK unless specifically handled (not present in current adapter logic for Search).
     */
    @Test
    public void testExtremeKNNSearch() {
        List<Object> searchArgs = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("describeCollection".equals(method.getName())) {
                return v2Response(method.getName(), io.milvus.grpc.DescribeCollectionResponse.newBuilder()
                        .setSchema(io.milvus.grpc.CollectionSchema.newBuilder().addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_id").setDataType(io.milvus.grpc.DataType.Int64).setIsPrimaryKey(true)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("word_count").setDataType(io.milvus.grpc.DataType.Int32)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("title").setDataType(io.milvus.grpc.DataType.VarChar)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_intro").setDataType(io.milvus.grpc.DataType.FloatVector))).build());
            }
            if ("search".equals(method.getName())) {
                searchArgs.addAll(Arrays.asList(args));

                // Mock Valid Result Data (1 Query, 0 Hits)
                SearchResultData resultData = SearchResultData.newBuilder().setNumQueries(1).setTopK(0).build();

                SearchResults.Builder builder = SearchResults.newBuilder();
                builder.setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success).build());
                builder.setResults(resultData);
                return v2Response(method.getName(), builder.build());
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
        SearchReq param = (SearchReq) searchArgs.get(0);

        assert param.getFilter().equals("book_id > {arg1}");
        assert param.getFilterTemplateValues().get("arg1").equals(500L);
        assert param.getTopK() == 20;
        assert param.getAnnsField().equals("book_intro");
        assert param.getData().size() == 1;
        assert param.getData().get(0).getData().equals(Arrays.asList(0.1f, 0.2f));
    }

    /*
     * SCENARIO 3: Extreme DML - Update with Vector Range
     * SQL: UPDATE book_vectors SET word_count = ? WHERE vector_range(book_intro, ?, ?) AND book_id > ? LIMIT 20
     * Logic: RangeSearch -> DescribeCollection (PK) -> Delete (Batch) -> Insert (New) ??
     * Wait, standard UPDATE is: Query -> Memory Update -> Upsert.
     * With Vector Range, it should be: Search (Range) -> Memory Update -> Upsert.
     */
    @Test
    public void testUpdateWithVectorRange() {
        List<Object> searchArgs = new ArrayList<>();
        SearchIteratorV2 searchIterator = PowerMockito.mock(SearchIteratorV2.class);
        PowerMockito.when(searchIterator.next()).thenReturn(new ArrayList<>());

        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            // 1. Range Search
            if ("searchIteratorV2".equals(method.getName())) {
                searchArgs.addAll(Arrays.asList(args));
                return v2Response(method.getName(), searchIterator);
            } else if ("describeCollection".equals(method.getName())) {
                CollectionSchema schema = CollectionSchema.newBuilder()//
                        .addFields(FieldSchema.newBuilder().setName("book_id").setDataType(DataType.Int64).setIsPrimaryKey(true))//
                        .addFields(FieldSchema.newBuilder().setName("word_count").setDataType(DataType.Int32))//
                        .addFields(FieldSchema.newBuilder().setName("book_intro").setDataType(DataType.FloatVector))//
                        .build();
                return v2Response(method.getName(), DescribeCollectionResponse.newBuilder()//
                        .setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success))//
                        .setSchema(schema)//
                        .build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            // vector_range(col, target_vector, radius)
            String sql = "UPDATE book_vectors SET word_count = ? WHERE vector_range(book_intro, ?, ?) AND book_id > ? LIMIT 20";
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
        SearchIteratorReqV2 param = (SearchIteratorReqV2) searchArgs.get(0);

        // Verify Range Search Params
        // The adapter might set specific params in JSON
        System.out.println("Search Params: " + param.getSearchParams());
        assert ((Number) param.getSearchParams().get(MilvusCommandKeys.RADIUS)).doubleValue() == 1.5;

        assert param.getVectorFieldName().equals("book_intro");
        assert param.getFilter().equals("book_id > {arg4}");
        assert param.getFilterTemplateValues().get("arg4").equals(100L);
        assert param.getTopK() == 20;
        assert param.getVectors().stream().map(v -> v.getData()).collect(java.util.stream.Collectors.toList()).get(0).equals(Arrays.asList(0.5f, 0.5f));

        // Since search returned empty, Upsert won't be called. That's fine, we verified the "Condition Parsing".
    }

    /*
     * SCENARIO 4: Extreme Delete - Delete by Range
     * SQL: DELETE FROM book_vectors WHERE vector_range(book_intro, ?, ?) LIMIT 20
     */
    @Test
    public void testDeleteByVectorRange() {
        List<Object> searchArgs = new ArrayList<>();
        SearchIteratorV2 searchIterator = PowerMockito.mock(SearchIteratorV2.class);
        PowerMockito.when(searchIterator.next()).thenReturn(new ArrayList<>());

        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("searchIteratorV2".equals(method.getName())) {
                searchArgs.addAll(Arrays.asList(args));
                return v2Response(method.getName(), searchIterator);
            } else if ("describeCollection".equals(method.getName())) {
                CollectionSchema schema = CollectionSchema.newBuilder()//
                        .setName("book_vectors")//
                        .addFields(FieldSchema.newBuilder().setName("book_id").setDataType(DataType.Int64).setIsPrimaryKey(true))//
                        .addFields(FieldSchema.newBuilder().setName("word_count").setDataType(DataType.Int32))//
                        .addFields(FieldSchema.newBuilder().setName("book_intro").setDataType(DataType.FloatVector))//
                        .build();
                return v2Response(method.getName(), DescribeCollectionResponse.newBuilder()//
                        .setStatus(Status.newBuilder().setErrorCode(ErrorCode.Success))//
                        .setSchema(schema)//
                        .build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            String sql = "DELETE FROM book_vectors WHERE vector_range(book_intro, ?, ?) LIMIT 20";
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
        SearchIteratorReqV2 param = (SearchIteratorReqV2) searchArgs.get(0);
        assert ((Number) param.getSearchParams().get(MilvusCommandKeys.RADIUS)).doubleValue() == 0.5;
        assert param.getVectors().stream().map(v -> v.getData()).collect(java.util.stream.Collectors.toList()).get(0).equals(Arrays.asList(0.9f, 0.9f));
        assert param.getTopK() == 20;
    }
}
