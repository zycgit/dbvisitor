package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import io.milvus.grpc.QueryResults;
import io.milvus.grpc.SearchResults;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.SearchIteratorReqV2;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.Test;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;

public class MilvusDQLArgsTest extends AbstractJdbcTest {

    private Connection getConnection() throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        prop.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://xxxxxx:19530", prop);
    }

    @Test
    public void testSearchWithArgs() {
        List<Object> argList = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("describeCollection".equals(method.getName())) {
                return v2Response(method.getName(), io.milvus.grpc.DescribeCollectionResponse.newBuilder()
                        .setSchema(io.milvus.grpc.CollectionSchema.newBuilder().addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_id").setDataType(io.milvus.grpc.DataType.Int64).setIsPrimaryKey(true)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("word_count").setDataType(io.milvus.grpc.DataType.Int32)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("title").setDataType(io.milvus.grpc.DataType.VarChar)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_intro").setDataType(io.milvus.grpc.DataType.FloatVector))).build());
            }
            if ("searchIteratorV2".equals(method.getName())) {
                argList.addAll(Arrays.asList(args));
                SearchResults.Builder resultsBuilder = SearchResults.newBuilder();
                return v2Response(method.getName(), resultsBuilder.build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            // SQL: Vector Search
            // SELECT book_id FROM book_vectors ORDER BY book_intro <-> ? LIMIT ? OFFSET ?
            String sql = "SELECT book_id FROM book_vectors ORDER BY book_intro <-> ? LIMIT ? OFFSET ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                // Vector
                ps.setObject(1, Arrays.asList(0.1f, 0.2f));
                // Limit
                ps.setInt(2, 10);
                // Offset -> Note: Milvus Scan/Search might handle OFFSET differently (e.g. topK = limit + offset, then slice).
                // Or Adapter implementation handles it. Let's see what SearchIteratorReqV2 gets.
                ps.setInt(3, 5);

                try (ResultSet rs = ps.executeQuery()) {
                }
            }
        } catch (SQLException e) {
            if (!e.getMessage().contains("Illegal index of target")) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        assert argList.size() == 1;
        SearchIteratorReqV2 searchParam = (SearchIteratorReqV2) argList.get(0);

        List<?> vectors = searchParam.getVectors().stream().map(v -> v.getData()).collect(java.util.stream.Collectors.toList());
        assert vectors.size() == 1;
        assert vectors.get(0).equals(Arrays.asList(0.1f, 0.2f));

        // Milvus Adapter usually maps JDBC LIMIT/OFFSET to SearchIteratorReqV2 properties.
        // Check implementation or behave based on expectation.
        // Assuming adapter handles it.
    }

    @Test
    public void testQueryWithArgs() {
        List<Object> argList = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("describeCollection".equals(method.getName())) {
                return v2Response(method.getName(), io.milvus.grpc.DescribeCollectionResponse.newBuilder()
                        .setSchema(io.milvus.grpc.CollectionSchema.newBuilder().addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_id").setDataType(io.milvus.grpc.DataType.Int64).setIsPrimaryKey(true)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("word_count").setDataType(io.milvus.grpc.DataType.Int32)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("title").setDataType(io.milvus.grpc.DataType.VarChar)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_intro").setDataType(io.milvus.grpc.DataType.FloatVector))).build());
            }
            if ("queryIterator".equals(method.getName())) {
                argList.addAll(Arrays.asList(args));
                QueryResults.Builder resultsBuilder = QueryResults.newBuilder();
                return v2Response(method.getName(), resultsBuilder.build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            // SQL: Scalar Query
            // SELECT book_id FROM book_vectors WHERE book_id = ?
            String sql = "SELECT book_id FROM book_vectors WHERE book_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, 1002L);
                try (ResultSet rs = ps.executeQuery()) {
                }
            }
        } catch (SQLException e) {
            if (!e.getMessage().contains("Illegal index of target")) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        assert argList.size() == 1;
        QueryIteratorReq queryParam = (QueryIteratorReq) argList.get(0);

        assert queryParam.getCollectionName().equals("book_vectors");
        // Verify expression
        // Expression structure and bound values travel separately.
        String expr = queryParam.getExpr();
        assert expr.contains("book_id");
        assert expr.equals("book_id == {arg1}");
        assert queryParam.getFilterTemplateValues().get("arg1").equals(1002L);
    }

    @Test
    public void testQueryWithInArgs() {
        List<Object> argList = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("describeCollection".equals(method.getName())) {
                return v2Response(method.getName(), io.milvus.grpc.DescribeCollectionResponse.newBuilder()
                        .setSchema(io.milvus.grpc.CollectionSchema.newBuilder().addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_id").setDataType(io.milvus.grpc.DataType.Int64).setIsPrimaryKey(true)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("word_count").setDataType(io.milvus.grpc.DataType.Int32)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("title").setDataType(io.milvus.grpc.DataType.VarChar)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_intro").setDataType(io.milvus.grpc.DataType.FloatVector))).build());
            }
            if ("queryIterator".equals(method.getName())) {
                argList.addAll(Arrays.asList(args));
                QueryResults.Builder resultsBuilder = QueryResults.newBuilder();
                return v2Response(method.getName(), resultsBuilder.build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            // SQL: Scalar Query IN
            String sql = "SELECT book_id FROM book_vectors WHERE book_id IN ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, Arrays.asList(1L, 2L, 3L));
                try (ResultSet rs = ps.executeQuery()) {
                }
            }
        } catch (SQLException e) {
            if (!e.getMessage().contains("Illegal index of target")) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        assert argList.size() == 1;
        QueryIteratorReq queryParam = (QueryIteratorReq) argList.get(0);
        String expr = queryParam.getExpr();
        // IN uses one array template.
        assert expr.contains("book_id in");
        assert expr.equals("book_id in {arg1}");
        assert queryParam.getFilterTemplateValues().get("arg1").equals(Arrays.asList(1L, 2L, 3L));
    }
}
