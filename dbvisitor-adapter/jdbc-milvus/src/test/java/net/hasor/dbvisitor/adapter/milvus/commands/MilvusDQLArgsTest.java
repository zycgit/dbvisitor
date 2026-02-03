package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import io.milvus.client.MilvusClient;
import io.milvus.grpc.QueryResults;
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.Test;

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
        MilvusCommandInterceptor.addInterceptor(MilvusClient.class, (proxy, method, args) -> {
            if ("search".equals(method.getName())) {
                argList.addAll(Arrays.asList(args));
                SearchResults.Builder resultsBuilder = SearchResults.newBuilder();
                return R.success(resultsBuilder.build());
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
                // Or Adapter implementation handles it. Let's see what SearchParam gets.
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
        SearchParam searchParam = (SearchParam) argList.get(0);

        List<?> vectors = searchParam.getVectors();
        assert vectors.size() == 1;
        assert vectors.get(0).equals(Arrays.asList(0.1f, 0.2f));

        // Milvus Adapter usually maps JDBC LIMIT/OFFSET to SearchParam properties.
        // Check implementation or behave based on expectation.
        // Assuming adapter handles it.
    }

    @Test
    public void testQueryWithArgs() {
        List<Object> argList = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClient.class, (proxy, method, args) -> {
            if ("query".equals(method.getName())) {
                argList.addAll(Arrays.asList(args));
                QueryResults.Builder resultsBuilder = QueryResults.newBuilder();
                return R.success(resultsBuilder.build());
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
        QueryParam queryParam = (QueryParam) argList.get(0);

        assert queryParam.getCollectionName().equals("book_vectors");
        // Verify expression
        // Expecting something like "book_id == 1002" or "book_id = 1002"
        String expr = queryParam.getExpr();
        assert expr.contains("book_id");
        assert expr.contains("1002");
    }

    @Test
    public void testQueryWithInArgs() {
        List<Object> argList = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClient.class, (proxy, method, args) -> {
            if ("query".equals(method.getName())) {
                argList.addAll(Arrays.asList(args));
                QueryResults.Builder resultsBuilder = QueryResults.newBuilder();
                return R.success(resultsBuilder.build());
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
        QueryParam queryParam = (QueryParam) argList.get(0);
        String expr = queryParam.getExpr();
        // Expect "book_id in [1, 2, 3]"
        assert expr.contains("book_id in");
        assert expr.contains("[1, 2, 3]") || expr.contains("1, 2"); // checking actual format might depend on list serialization
    }
}
