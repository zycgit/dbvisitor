package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.response.QueryResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusSqlCountTest {
    private final List<QueryReq> queries = new ArrayList<>();
    private       Connection     connection;

    @Before
    public void connect() throws SQLException {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("getServerVersion".equals(method.getName())) {
                return "v2.6.2";
            }
            if ("query".equals(method.getName())) {
                queries.add((QueryReq) args[0]);
                QueryResp.QueryResult row = QueryResp.QueryResult.builder().entity(Collections.singletonMap("count(*)", 42L)).build();
                return QueryResp.builder().queryResults(Collections.singletonList(row)).build();
            }
            throw new AssertionError("COUNT must use only the native query operation: " + method.getName());
        });
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        this.connection = new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/db1", properties);
    }

    @After
    public void close() throws SQLException {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } finally {
            MilvusCommandInterceptor.resetInterceptor();
        }
    }

    @Test
    public void bothSpellingsUseNativeCountWithBoundFilter() throws SQLException {
        String value = "\" or id > 0 or name == \"";
        for (String prefix : Arrays.asList("COUNT", "SELECT COUNT(*)")) {
            try (PreparedStatement statement = this.connection.prepareStatement(prefix + " FROM t PARTITION p WHERE name = ?")) {
                statement.setString(1, value);
                statement.setFetchSize(2);
                statement.setMaxRows(1);
                try (ResultSet result = statement.executeQuery()) {
                    assertEquals(1, result.getMetaData().getColumnCount());
                    assertTrue(result.next());
                    assertEquals(42L, result.getLong(1));
                    assertFalse(result.next());
                }
            }
        }
        assertEquals(2, this.queries.size());
        for (QueryReq query : this.queries) {
            assertEquals("db1", query.getDatabaseName());
            assertEquals("t", query.getCollectionName());
            assertEquals(Collections.singletonList("p"), query.getPartitionNames());
            assertEquals(Collections.singletonList("count(*)"), query.getOutputFields());
            assertTrue(query.getFilterTemplateValues().containsValue(value));
            assertFalse(query.getFilter().contains(value));
        }
    }

    @Test
    public void jdbcArrayCanBeFreedAfterBindingAndReusedWithStatement() throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement("SELECT COUNT(*) FROM t WHERE id IN ?")) {
            java.sql.Array array = this.connection.createArrayOf("BIGINT", new Object[] { 1L, 2L });
            statement.setArray(1, array);
            array.free();
            for (int execution = 0; execution < 2; execution++) {
                try (ResultSet result = statement.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals(42L, result.getLong(1));
                }
            }
        }
        assertEquals(2, this.queries.size());
        for (QueryReq query : this.queries) {
            assertTrue(query.getFilterTemplateValues().containsValue(Arrays.asList(1L, 2L)));
        }
    }

    @Test
    public void unsupportedAggregatesDoNotBecomeClientSideScans() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeQuery("SELECT COUNT(id) FROM t"));
            assertThrows(SQLException.class, () -> statement.executeQuery("SELECT COUNT(*) FROM t GROUP BY name"));
            assertThrows(SQLException.class, () -> statement.executeQuery("SELECT COUNT(*) FROM t WHERE v <-> [1,0] < 2"));
        }
        assertTrue(this.queries.isEmpty());
    }
}
