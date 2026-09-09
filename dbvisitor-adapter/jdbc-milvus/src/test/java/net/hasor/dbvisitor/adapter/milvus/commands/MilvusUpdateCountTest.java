package net.hasor.dbvisitor.adapter.milvus.commands;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.grpc.CollectionSchema;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.FieldSchema;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.response.QueryResultsWrapper.RowRecord;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MilvusUpdateCountTest {
    private long           deleteCount;
    private QueryIterator  queryIterator;
    private SearchIteratorV2 searchIterator;

    @Before
    public void install() {
        deleteCount = 1;
        queryIterator = Mockito.mock(QueryIterator.class);
        searchIterator = Mockito.mock(SearchIteratorV2.class);
        CollectionSchema schema = CollectionSchema.newBuilder().setName("t").addFields(FieldSchema.newBuilder().setName("id").setDataType(DataType.Int64).setIsPrimaryKey(true)).addFields(FieldSchema.newBuilder().setName("a").setDataType(DataType.Int32)).addFields(FieldSchema.newBuilder().setName("v").setDataType(DataType.FloatVector)).build();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getServerVersion":
                    return "v2.6.2";
                case "describeCollection":
                    return v2Response(method.getName(), DescribeCollectionResponse.newBuilder().setSchema(schema).build());
                case "queryIterator":
                    return queryIterator;
                case "searchIteratorV2":
                    return searchIterator;
                case "delete":
                    return DeleteResp.builder().deleteCnt(deleteCount).build();
                case "upsert":
                    return UpsertResp.builder().upsertCnt(((UpsertReq) args[0]).getData().size()).build();
                default:
                    return null;
            }
        });
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    private Connection connect() throws SQLException {
        Properties props = new Properties();
        props.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        props.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://mock:19530/db1", props);
    }

    @Test
    public void nativeDeletePreservesLongCounts() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (long count : new long[] { 0, 1, Integer.MAX_VALUE, (long) Integer.MAX_VALUE + 1, Long.MAX_VALUE }) {
                deleteCount = count;
                assertEquals(count, statement.executeLargeUpdate("DELETE FROM t WHERE id > 0"));
                assertEquals(count, statement.getLargeUpdateCount());
                assertEquals(count > Integer.MAX_VALUE ? Statement.SUCCESS_NO_INFO : (int) count, statement.getUpdateCount());
            }
        }
    }

    @Test
    public void preparedDeletePreservesLongCounts() throws Exception {
        deleteCount = (long) Integer.MAX_VALUE + 1;
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("DELETE FROM t WHERE id > ?")) {
            statement.setLong(1, 0);
            assertEquals(deleteCount, statement.executeLargeUpdate());
            assertEquals(deleteCount, statement.getLargeUpdateCount());
            assertEquals(Statement.SUCCESS_NO_INFO, statement.executeUpdate());
        }
    }

    @Test
    public void ordinaryUpdateUsesTheCommonJdbcOverflowPolicy() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            deleteCount = Integer.MAX_VALUE;
            assertEquals(Integer.MAX_VALUE, statement.executeUpdate("DELETE FROM t WHERE id > 0"));
            deleteCount++;
            assertEquals(Statement.SUCCESS_NO_INFO, statement.executeUpdate("DELETE FROM t WHERE id > 0"));
            assertEquals(deleteCount, statement.getLargeUpdateCount());
        }
    }

    @Test
    public void largeCountDoesNotLookLikeEndOfResults() throws Exception {
        deleteCount = 0xFFFFFFFFL;
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertFalse(statement.execute("DELETE FROM t WHERE id > 0; DELETE FROM t WHERE id < 0"));
            assertEquals(deleteCount, statement.getLargeUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(deleteCount, statement.getLargeUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getLargeUpdateCount());
        }
    }

    private static List<RowRecord> page(long id) {
        RowRecord row = new RowRecord();
        row.put("id", id);
        return Collections.singletonList(row);
    }

    @Test
    public void everyPagedDmlPathAccumulatesCountsAndClosesItsIterator() throws Exception {
        List<String> statements = Arrays.asList("UPDATE t SET a = 7 WHERE id > 0", "UPDATE t SET a = 7 ORDER BY v <-> [1,2]", "UPDATE t SET a = 7 WHERE v <-> [1,2] < 2", "DELETE FROM t WHERE id > 0 LIMIT 3", "DELETE FROM t ORDER BY v <-> [1,2]", "DELETE FROM t WHERE v <-> [1,2] < 2");
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                Mockito.reset(queryIterator, searchIterator);
                Mockito.when(queryIterator.next()).thenReturn(page(1)).thenReturn(page(2)).thenReturn(page(3)).thenReturn(Collections.emptyList());
                Mockito.when(searchIterator.next()).thenReturn(net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(1))).thenReturn(net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(2))).thenReturn(net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(3))).thenReturn(Collections.emptyList());
                assertEquals(sql, 3L, statement.executeLargeUpdate(sql));
                assertEquals(sql, 3L, statement.getLargeUpdateCount());
                if (sql.contains("v <->")) {
                    Mockito.verify(searchIterator).close();
                } else {
                    Mockito.verify(queryIterator).close();
                }
            }
        }
    }

    @Test
    public void queryPageAccumulatorExceedsIntegerRange() throws Exception {
        Mockito.when(queryIterator.next()).thenReturn(page(1)).thenReturn(page(2)).thenReturn(Collections.emptyList());
        assertLargePageTotal(QueryIterator.class, queryIterator);
        Mockito.verify(queryIterator).close();
    }

    @Test
    public void searchPageAccumulatorExceedsIntegerRange() throws Exception {
        Mockito.when(searchIterator.next()).thenReturn(net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(1))).thenReturn(net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("id", page(2))).thenReturn(Collections.emptyList());
        assertLargePageTotal(SearchIteratorV2.class, searchIterator);
        Mockito.verify(searchIterator).close();
    }

    private static void assertLargePageTotal(Class<?> iteratorType, Object iterator) throws Exception {
        // Exercise the existing page accumulator without allocating billions of records or exposing a test-only API.
        Class<?> commandsType = Class.forName("net.hasor.dbvisitor.adapter.milvus.commands.write.MilvusCommandsForData");
        Class<?> writerType = Class.forName(commandsType.getName() + "$PageWriter");
        AtomicInteger pages = new AtomicInteger();
        Object writer = Proxy.newProxyInstance(writerType.getClassLoader(), new Class<?>[] { writerType }, (proxy, method, args) -> pages.getAndIncrement() == 0 ? (long) Integer.MAX_VALUE : 1L);
        Method writePages = commandsType.getDeclaredMethod("writePages", AdapterRequest.class, String.class, iteratorType, writerType);
        writePages.setAccessible(true);
        Number total = (Number) writePages.invoke(null, null, "UPDATE", iterator, writer);
        assertEquals((long) Integer.MAX_VALUE + 1, total.longValue());
        assertEquals(2, pages.get());
    }
}
