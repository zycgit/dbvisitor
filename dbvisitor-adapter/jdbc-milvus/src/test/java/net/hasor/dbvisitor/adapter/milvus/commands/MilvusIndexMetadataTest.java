package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.index.request.DescribeIndexReq;
import io.milvus.v2.service.index.request.ListIndexesReq;
import io.milvus.v2.service.index.response.DescribeIndexResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusIndexMetadataTest {
    private final List<Object> requests = new ArrayList<>();
    private       List<String> names    = Arrays.asList("first", "second");
    private       String       failedIndex;
    private       Connection   connection;

    @Before
    public void connect() throws SQLException {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if (method.getName().equals("getServerVersion")) {
                return "v2.6.2";
            }
            if (args.length > 0) {
                this.requests.add(args[0]);
            }
            if (method.getName().equals("listIndexes")) {
                ListIndexesReq list = (ListIndexesReq) args[0];
                assertEquals("db1", list.getDatabaseName());
                assertEquals("books", list.getCollectionName());
                assertNull(list.getFieldName());
                return this.names;
            }
            if (method.getName().equals("describeIndex")) {
                DescribeIndexReq describe = (DescribeIndexReq) args[0];
                assertEquals("db1", describe.getDatabaseName());
                assertEquals("books", describe.getCollectionName());
                assertFalse(describe.getIndexName().isEmpty());
                if (describe.getIndexName().equals(this.failedIndex)) {
                    throw new IllegalStateException("index disappeared");
                }
                DescribeIndexResp.IndexDesc index = DescribeIndexResp.IndexDesc.builder().indexName(describe.getIndexName()).fieldName("v_" + describe.getIndexName()).id(42L).indexType(IndexParam.IndexType.FLAT).metricType(IndexParam.MetricType.L2).extraParams(Collections.singletonMap("custom", "native")).totalRows(3_000_000_000L).indexedRows(2_000_000_000L).build();
                return DescribeIndexResp.builder().indexDescriptions(Collections.singletonList(index)).build();
            }
            return null;
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
    public void listThenDescribePreservesExistingColumnsAndNativeDetails() throws SQLException {
        try (Statement statement = this.connection.createStatement(); ResultSet result = statement.executeQuery("SHOW INDEXES FROM books")) {
            assertEquals(4, result.getMetaData().getColumnCount());
            for (String name : this.names) {
                assertTrue(result.next());
                assertEquals(name, result.getString("INDEX"));
                assertEquals("v_" + name, result.getString("FIELD"));
                assertEquals(42L, result.getLong("ID"));
                assertTrue(result.getString("PARAMS").contains("custom=native"));
            }
            assertFalse(result.next());
        }
        assertEquals(3, this.requests.size());
        assertTrue(this.requests.get(0) instanceof ListIndexesReq);
    }

    @Test
    public void namedIndexDoesNotListAndUnnamedProgressAggregatesLongCounters() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("SHOW INDEX second ON books")) {
                assertTrue(result.next());
                assertEquals("second", result.getString("INDEX"));
                assertFalse(result.next());
            }
            assertEquals(1, this.requests.size());
            assertTrue(this.requests.get(0) instanceof DescribeIndexReq);
            try (ResultSet result = statement.executeQuery("SHOW PROGRESS OF INDEX ON books")) {
                assertTrue(result.next());
                assertEquals(6_000_000_000L, result.getLong("TOTAL"));
                assertEquals(4_000_000_000L, result.getLong("INDEXED"));
                assertFalse(result.next());
            }
        }
        assertEquals(4, this.requests.size());
    }

    @Test
    public void noIndexesPreservesMetadataAndZeroProgressWithoutDescribeCalls() throws SQLException {
        this.names = Collections.emptyList();
        try (Statement statement = this.connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("SHOW INDEXES FROM books")) {
                assertEquals(4, result.getMetaData().getColumnCount());
                assertFalse(result.next());
            }
            try (ResultSet result = statement.executeQuery("SHOW PROGRESS OF INDEX ON books")) {
                assertTrue(result.next());
                assertEquals(0L, result.getLong("TOTAL"));
                assertEquals(0L, result.getLong("INDEXED"));
                assertFalse(result.next());
            }
        }
        assertEquals(2, this.requests.size());
        assertTrue(this.requests.stream().allMatch(ListIndexesReq.class::isInstance));
    }

    @Test
    public void disappearingIndexDoesNotProducePartialSuccessOrRunLaterSql() throws SQLException {
        this.failedIndex = "second";
        try (Statement statement = this.connection.createStatement()) {
            SQLException failure = assertThrows(SQLException.class, () -> statement.execute("SHOW INDEXES FROM books; TRUNCATE TABLE books"));
            assertTrue(failure.getMessage().contains("index disappeared"));
            assertEquals(3, this.requests.size());
            this.failedIndex = null;
            try (ResultSet result = statement.executeQuery("SHOW INDEX first ON books")) {
                assertTrue(result.next());
            }
        }
        assertEquals(4, this.requests.size());
        assertTrue(this.requests.get(3) instanceof DescribeIndexReq);
    }
}
