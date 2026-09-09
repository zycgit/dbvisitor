package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.grpc.CollectionSchema;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.FieldSchema;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.param.Constant;
import io.milvus.response.QueryResultsWrapper.RowRecord;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchIteratorReqV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import net.hasor.dbvisitor.driver.JdbcErrorCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits;
import static org.junit.Assert.*;

/** Exercise lazy reads through public JDBC methods and intercepted official SDK iterators. */
public class MilvusStreamingTest {
    private static final List<String> SELECTS   = Arrays.asList("SELECT id FROM t", "SELECT id FROM t ORDER BY v <-> [1,2]", "SELECT id FROM t WHERE v <-> [1,2] < 10");
    private final        List<Object> calls     = new ArrayList<>();
    private final        List<Pages>  iterators = new ArrayList<>();
    private final        List<String> events    = new ArrayList<>();
    private              int          totalRows = 7;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            String name = method.getName();
            if ("describeCollection".equals(name)) {
                CollectionSchema schema = CollectionSchema.newBuilder().setName("t").addFields(FieldSchema.newBuilder().setName("id").setDataType(DataType.Int64).setIsPrimaryKey(true)).addFields(FieldSchema.newBuilder().setName("tiny").setDataType(DataType.Int8)).addFields(FieldSchema.newBuilder().setName("small").setDataType(DataType.Int16)).addFields(FieldSchema.newBuilder().setName("v").setDataType(DataType.FloatVector)).build();
                return v2Response(name, DescribeCollectionResponse.newBuilder().setSchema(schema).build());
            }
            if ("queryIterator".equals(name) || "searchIteratorV2".equals(name)) {
                calls.add(args[0]);
                events.add(name);
                long batch = args[0] instanceof QueryIteratorReq ? ((QueryIteratorReq) args[0]).getBatchSize() : ((SearchIteratorReqV2) args[0]).getBatchSize();
                long limit = args[0] instanceof QueryIteratorReq ? ((QueryIteratorReq) args[0]).getLimit() : ((SearchIteratorReqV2) args[0]).getLimit();
                Pages pages = new Pages((int) batch, limit < 0 ? totalRows : (int) Math.min(totalRows, limit));
                iterators.add(pages);
                return "queryIterator".equals(name) ? pages.query : pages.search;
            }
            if ("query".equals(name)) {
                calls.add(args[0]);
                List<QueryResp.QueryResult> rows = new ArrayList<>();
                for (int i = 1; i <= Math.min(totalRows, ((QueryReq) args[0]).getLimit()); i++) {
                    rows.add(QueryResp.QueryResult.builder().entity(row(i).getFieldValues()).build());
                }
                return QueryResp.builder().queryResults(rows).build();
            }
            if ("search".equals(name)) {
                calls.add(args[0]);
                List<SearchResp.SearchResult> rows = new ArrayList<>();
                for (int i = 1; i <= Math.min(totalRows, ((SearchReq) args[0]).getTopK()); i++) {
                    rows.add(SearchResp.SearchResult.builder().primaryKey("id").id((long) i).score(i / 10F).entity(row(i).getFieldValues()).build());
                }
                return SearchResp.builder().searchResults(Collections.singletonList(rows)).build();
            }
            if ("delete".equals(name)) {
                events.add(name);
                return DeleteResp.builder().deleteCnt(1).build();
            }
            return null;
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
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://mock:19530?" + MilvusKeys.MAX_RETRY + "=2", props);
    }

    private static RowRecord row(long id) {
        RowRecord row = new RowRecord();
        row.put("id", id);
        row.put("tiny", 7);
        row.put("small", 8);
        row.put("v", Arrays.asList(1F, 2F));
        row.put("score", id / 10F);
        return row;
    }

    private static final class Pages {
        private final QueryIterator    query  = Mockito.mock(QueryIterator.class);
        private final SearchIteratorV2   search = Mockito.mock(SearchIteratorV2.class);
        private final AtomicInteger    reads  = new AtomicInteger();
        private final AtomicInteger    closes = new AtomicInteger();
        private final int              batch;
        private final int              total;
        private       int              position;
        private       RuntimeException readFailure;
        private       RuntimeException closeFailure;
        private       long             readDelayMillis;
        private       CountDownLatch   entered;
        private       CountDownLatch   release;

        private Pages(int batch, int total) {
            this.batch = batch;
            this.total = total;
            Mockito.when(query.next()).thenAnswer(invocation -> read());
            Mockito.when(search.next()).thenAnswer(invocation -> searchHits("id", read()));
            Mockito.doAnswer(invocation -> {
                close();
                return null;
            }).when(query).close();
            Mockito.doAnswer(invocation -> {
                close();
                return null;
            }).when(search).close();
        }

        private List<RowRecord> read() throws InterruptedException {
            reads.incrementAndGet();
            if (readDelayMillis > 0)
                Thread.sleep(readDelayMillis);
            if (entered != null) {
                entered.countDown();
                if (!release.await(5, TimeUnit.SECONDS))
                    throw new AssertionError("Blocked read was not released");
            }
            if (readFailure != null)
                throw readFailure;
            List<RowRecord> rows = new ArrayList<>();
            while (rows.size() < batch && position < total)
                rows.add(row(++position));
            return rows;
        }

        private void close() {
            closes.incrementAndGet();
            if (closeFailure != null)
                throw closeFailure;
        }
    }

    @Test
    public void allSelectPathsPullOnlyThePagesConsumedByJdbc() throws Exception {
        for (String sql : SELECTS) {
            try (Connection conn = connect(); Statement statement = conn.createStatement()) {
                statement.setFetchSize(2);
                try (ResultSet result = statement.executeQuery(sql)) {
                    Pages pages = iterators.get(iterators.size() - 1);
                    assertEquals(0, pages.reads.get());
                    assertEquals(Types.BIGINT, result.getMetaData().getColumnType(1));
                    assertEquals(0, pages.reads.get());
                    assertTrue(result.next());
                    assertEquals(1L, result.getLong(1));
                    assertTrue(result.next());
                    assertEquals(2L, result.getLong(1));
                    assertEquals(1, pages.reads.get());
                    assertTrue(result.next());
                    assertEquals(3L, result.getLong(1));
                    assertEquals(2, pages.reads.get());
                }
                Pages pages = iterators.get(iterators.size() - 1);
                assertEquals(2, pages.reads.get());
                assertEquals(1, pages.closes.get());
            }
        }
    }

    @Test
    public void boundedResultsFitInOneRequestOtherwiseUseAnIterator() throws Exception {
        for (String sql : SELECTS) {
            try (Connection conn = connect(); Statement statement = conn.createStatement()) {
                statement.setFetchSize(2);
                int opened = iterators.size();
                try (ResultSet result = statement.executeQuery(sql + " LIMIT 2")) {
                    assertEquals(opened, iterators.size());
                    assertEquals(Arrays.asList(1L, 2L), ids(result));
                }
                try (ResultSet result = statement.executeQuery(sql + " LIMIT 3")) {
                    assertEquals(opened + 1, iterators.size());
                    assertEquals(Arrays.asList(1L, 2L, 3L), ids(result));
                    Pages pages = iterators.get(opened);
                    assertEquals(2, pages.reads.get());
                    assertEquals(1, pages.closes.get());
                }
            }
        }
    }

    @Test
    public void limitOffsetHintsAndMaxRowsApplyAcrossPages() throws Exception {
        for (String sql : SELECTS) {
            try (Connection conn = connect(); PreparedStatement statement = conn.prepareStatement("/*+ overwrite_find_limit=5, overwrite_find_skip=3 */ " + sql + " LIMIT ? OFFSET ?")) {
                statement.setInt(1, 6);
                statement.setInt(2, 1);
                statement.setFetchSize(2);
                statement.setMaxRows(2);
                try (ResultSet result = statement.executeQuery()) {
                    assertEquals(Arrays.asList(4L, 5L), ids(result));
                    Pages pages = iterators.get(iterators.size() - 1);
                    assertEquals(3, pages.reads.get());
                    assertEquals(1, pages.closes.get());
                    assertFalse(result.isClosed());
                }
                Object call = calls.get(calls.size() - 1);
                if (call instanceof QueryIteratorReq) {
                    assertEquals(5, ((QueryIteratorReq) call).getLimit());
                    assertEquals(0, ((QueryIteratorReq) call).getOffset());
                } else {
                    assertEquals(5, ((SearchIteratorReqV2) call).getLimit());
                }
            }
        }
    }

    @Test
    public void defaultsAndLargeFetchSizesRespectSdkPageBounds() throws Exception {
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            try (ResultSet ignored = statement.executeQuery("SELECT id FROM t")) {
                assertEquals(QueryIteratorReq.builder().build().getBatchSize(), ((QueryIteratorReq) calls.get(0)).getBatchSize());
                assertEquals(Constant.UNLIMITED_L, ((QueryIteratorReq) calls.get(0)).getLimit());
            }
            statement.setFetchSize(Integer.MAX_VALUE);
            try (ResultSet ignored = statement.executeQuery("SELECT id FROM t ORDER BY v <-> [1,2]")) {
                assertEquals(Constant.MAX_BATCH_SIZE, ((SearchIteratorReqV2) calls.get(1)).getBatchSize());
                assertEquals(Constant.UNLIMITED_L, ((SearchIteratorReqV2) calls.get(1)).getLimit());
            }
        }
    }

    @Test
    public void offsetOverflowFailsBeforeOpeningAnyIterator() throws Exception {
        for (String sql : SELECTS) {
            try (Connection conn = connect(); Statement statement = conn.createStatement()) {
                SQLException failure = assertThrows(SQLException.class, () -> statement.executeQuery(sql + " LIMIT 2 OFFSET 9223372036854775807"));
                assertTrue(failure.getMessage().contains("OFFSET"));
            }
        }
        assertTrue(iterators.isEmpty());
        assertTrue(calls.isEmpty());
    }

    @Test
    public void eofAndExplicitCloseReleaseExactlyOnce() throws Exception {
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            statement.setFetchSize(2);
            ResultSet result = statement.executeQuery("SELECT id FROM t");
            Pages pages = iterators.get(0);
            assertEquals(7, ids(result).size());
            assertEquals(5, pages.reads.get());
            assertEquals(1, pages.closes.get());
            assertFalse(result.next());
            result.close();
            statement.close();
            assertEquals(1, pages.closes.get());
        }
    }

    @Test
    public void emptyResultHasMetadataBeforeFetchingAndClosesAtEof() throws Exception {
        totalRows = 0;
        try (Connection conn = connect(); Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery("SELECT small,id,tiny,score FROM t ORDER BY v <-> [1,2]")) {
            ResultSetMetaData metadata = result.getMetaData();
            assertEquals(4, metadata.getColumnCount());
            assertEquals("small", metadata.getColumnLabel(1));
            assertEquals(Types.SMALLINT, metadata.getColumnType(1));
            assertEquals(Types.FLOAT, metadata.getColumnType(4));
            assertEquals(0, iterators.get(0).reads.get());
            assertFalse(result.next());
            assertEquals(1, iterators.get(0).closes.get());
        }
    }

    @Test
    public void lastLimitedRowRetainsProjectionTypesAndScore() throws Exception {
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            statement.setFetchSize(1);
            try (ResultSet result = statement.executeQuery("SELECT small,id,tiny,score FROM t ORDER BY v <-> [1,2] LIMIT 2")) {
                assertTrue(result.next());
                assertTrue(result.next());
                assertEquals(1, iterators.get(0).closes.get());
                assertEquals(Short.valueOf((short) 8), result.getObject(1));
                assertEquals(2L, result.getLong(2));
                assertEquals(Byte.valueOf((byte) 7), result.getObject(3));
                assertEquals(0.2F, result.getFloat(4), 0);
                assertFalse(result.next());
            }
        }
    }

    @Test
    public void reexecutionStatementCloseAndConnectionCloseReleaseUnreadResults() throws Exception {
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            ResultSet first = statement.executeQuery("SELECT id FROM t");
            ResultSet second = statement.executeQuery("SELECT id FROM t");
            assertTrue(first.isClosed());
            assertEquals(1, iterators.get(0).closes.get());
            statement.close();
            assertTrue(second.isClosed());
            assertEquals(1, iterators.get(1).closes.get());
            Statement another = conn.createStatement();
            ResultSet third = another.executeQuery("SELECT id FROM t");
            conn.close();
            assertTrue(third.isClosed());
            assertEquals(1, iterators.get(2).closes.get());
        }
        for (Pages pages : iterators)
            assertEquals(0, pages.reads.get());
    }

    @Test
    public void cancellingOneStatementDoesNotCancelAnotherOnTheSameConnection() throws Exception {
        try (Connection conn = connect(); Statement first = conn.createStatement(); Statement second = conn.createStatement(); ResultSet left = first.executeQuery("SELECT id FROM t"); ResultSet right = second.executeQuery("SELECT id FROM t")) {
            first.cancel();
            assertEquals(JdbcErrorCode.SQL_STATE_IS_CANCELLED, assertThrows(SQLException.class, left::next).getSQLState());
            assertEquals(0, iterators.get(0).reads.get());
            assertEquals(1, iterators.get(0).closes.get());
            assertEquals(7, ids(right).size());
            assertEquals(1, iterators.get(1).closes.get());
        }
    }

    @Test
    public void cancellationDuringFetchDoesNotWaitForTheBlockedSdkCall() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch release = new CountDownLatch(1);
        try (Connection conn = connect(); Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery("SELECT id FROM t")) {
            Pages pages = iterators.get(0);
            pages.entered = new CountDownLatch(1);
            pages.release = release;
            java.util.concurrent.Future<SQLException> read = executor.submit(() -> assertThrows(SQLException.class, result::next));
            assertTrue(pages.entered.await(3, TimeUnit.SECONDS));
            statement.cancel();
            assertEquals(0, pages.closes.get());
            release.countDown();
            assertEquals(JdbcErrorCode.SQL_STATE_IS_CANCELLED, read.get(3, TimeUnit.SECONDS).getSQLState());
            assertEquals(1, pages.reads.get());
            assertEquals(1, pages.closes.get());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    public void timeoutStillAppliesWhenReadingAfterExecuteHasReturned() throws Exception {
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            statement.setQueryTimeout(1);
            try (ResultSet result = statement.executeQuery("SELECT id FROM t")) {
                Thread.sleep(1100);
                assertThrows(SQLTimeoutException.class, result::next);
                assertEquals(0, iterators.get(0).reads.get());
                assertEquals(1, iterators.get(0).closes.get());
            }
        }
    }

    @Test
    public void readFailureIsNotRetriedAndCloseFailureIsSuppressed() throws Exception {
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            statement.setFetchSize(1);
            try (ResultSet result = statement.executeQuery("SELECT id FROM t")) {
                assertTrue(result.next());
                Pages pages = iterators.get(0);
                pages.readFailure = new IllegalStateException("read failed");
                pages.closeFailure = new IllegalStateException("close failed");
                SQLException failure = assertThrows(SQLException.class, result::next);
                assertSame(pages.readFailure, failure.getCause());
                assertArrayEquals(new Throwable[] { pages.closeFailure }, failure.getSuppressed());
                assertSame(failure, assertThrows(SQLException.class, result::next));
                assertEquals(2, pages.reads.get());
                assertEquals(1, pages.closes.get());
            }
        }
    }

    @Test
    public void multiResultsOpenInSqlOrderAndKeepOrCloseIndependentCursors() throws Exception {
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            assertTrue(statement.execute("SELECT id FROM t; DELETE FROM t WHERE id = 99; SELECT id FROM t"));
            assertEquals(Arrays.asList("queryIterator", "delete", "queryIterator"), events);
            ResultSet first = statement.getResultSet();
            assertFalse(statement.getMoreResults(Statement.KEEP_CURRENT_RESULT));
            assertEquals(1, statement.getUpdateCount());
            assertTrue(statement.getMoreResults(Statement.KEEP_CURRENT_RESULT));
            ResultSet second = statement.getResultSet();
            assertTrue(first.next());
            assertTrue(second.next());
            assertEquals(1L, first.getLong(1));
            assertEquals(1L, second.getLong(1));
            assertFalse(statement.getMoreResults(Statement.CLOSE_ALL_RESULTS));
            assertTrue(first.isClosed());
            assertTrue(second.isClosed());
            for (Pages pages : iterators)
                assertEquals(1, pages.closes.get());
        }
    }

    @Test
    public void defaultMoreResultsClosesUnreadCursor() throws Exception {
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            assertTrue(statement.execute("SELECT id FROM t; SELECT id FROM t"));
            ResultSet first = statement.getResultSet();
            assertTrue(statement.getMoreResults());
            assertTrue(first.isClosed());
            assertEquals(0, iterators.get(0).reads.get());
            assertEquals(1, iterators.get(0).closes.get());
            assertEquals(7, ids(statement.getResultSet()).size());
        }
    }

    @Test
    public void closeOnCompletionClosesStatementAfterResultClose() throws Exception {
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            statement.closeOnCompletion();
            ResultSet result = statement.executeQuery("SELECT id FROM t");
            result.close();
            assertTrue(statement.isClosed());
            assertEquals(1, iterators.get(0).closes.get());
        }
    }

    @Test
    public void unlimitedReadsContinueBeyondTheOldTotalCapOnEverySelectPath() throws Exception {
        totalRows = 17000;
        for (String sql : SELECTS) {
            try (Connection conn = connect(); Statement statement = conn.createStatement()) {
                statement.setFetchSize(257);
                try (ResultSet result = statement.executeQuery(sql)) {
                    long count = 0;
                    while (result.next())
                        assertEquals(++count, result.getLong(1));
                    assertEquals(totalRows, count);
                    Pages pages = iterators.get(iterators.size() - 1);
                    assertEquals((totalRows + 256) / 257 + 1, pages.reads.get());
                    assertEquals(1, pages.closes.get());
                }
            }
        }
    }

    @Test
    public void offsetsPastEofAndMaxRowsWithoutLimitAreHandledOnEverySelectPath() throws Exception {
        for (String sql : SELECTS) {
            try (Connection conn = connect(); Statement statement = conn.createStatement()) {
                statement.setFetchSize(2);
                statement.setMaxRows(3);
                try (ResultSet result = statement.executeQuery(sql)) {
                    assertEquals(Arrays.asList(1L, 2L, 3L), ids(result));
                }
                try (ResultSet result = statement.executeQuery(sql + " OFFSET 9")) {
                    assertFalse(result.next());
                    assertEquals(1, iterators.get(iterators.size() - 1).closes.get());
                }
            }
        }
    }

    @Test
    public void timeoutDuringAnSdkReadDiscardsTheReturnedPageAndCloses() throws Exception {
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            statement.setQueryTimeout(1);
            try (ResultSet result = statement.executeQuery("SELECT id FROM t")) {
                Pages pages = iterators.get(0);
                pages.readDelayMillis = 1100;
                assertThrows(SQLTimeoutException.class, result::next);
                assertEquals(1, pages.reads.get());
                assertEquals(1, pages.closes.get());
            }
        }
    }

    @Test
    public void closeDuringFetchReleasesTheIteratorWhenTheSdkCallReturns() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch release = new CountDownLatch(1);
        try (Connection conn = connect(); Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery("SELECT id FROM t")) {
            Pages pages = iterators.get(0);
            pages.entered = new CountDownLatch(1);
            pages.release = release;
            java.util.concurrent.Future<SQLException> read = executor.submit(() -> assertThrows(SQLException.class, result::next));
            assertTrue(pages.entered.await(3, TimeUnit.SECONDS));
            result.close();
            assertTrue(result.isClosed());
            release.countDown();
            assertTrue(read.get(3, TimeUnit.SECONDS).getMessage().contains("closed"));
            assertEquals(1, pages.closes.get());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    public void eofCloseFailureIsReportedWithoutReadingAgain() throws Exception {
        totalRows = 0;
        try (Connection conn = connect(); Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery("SELECT id FROM t")) {
            Pages pages = iterators.get(0);
            pages.closeFailure = new IllegalStateException("close failed");
            SQLException failure = assertThrows(SQLException.class, result::next);
            assertSame(pages.closeFailure, failure.getCause());
            assertSame(failure, assertThrows(SQLException.class, result::next));
            assertEquals(1, pages.reads.get());
            assertEquals(1, pages.closes.get());
        }
    }

    private static List<Long> ids(ResultSet result) throws SQLException {
        List<Long> ids = new ArrayList<>();
        while (result.next())
            ids.add(result.getLong("id"));
        return ids;
    }
}
