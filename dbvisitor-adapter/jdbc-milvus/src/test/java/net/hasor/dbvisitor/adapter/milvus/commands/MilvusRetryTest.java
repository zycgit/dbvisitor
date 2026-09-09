package net.hasor.dbvisitor.adapter.milvus.commands;

import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.*;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.milvus.grpc.CollectionSchema;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.FieldSchema;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.response.QueryResultsWrapper.RowRecord;
import io.milvus.shaded.io.grpc.Status;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.client.RetryConfig;
import io.milvus.v2.exception.ErrorCode;
import io.milvus.v2.exception.MilvusClientException;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.utils.RpcUtils;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;

public class MilvusRetryTest {
    private final AtomicInteger writes   = new AtomicInteger();
    private final List<Long>    attempts = new ArrayList<>();
    private QueryIterator       iterator;
    private Exception           writeFailure;
    private int                 failuresRemaining;
    private CountDownLatch      firstAttempt;

    @Before
    public void install() {
        iterator = Mockito.mock(QueryIterator.class);
        CollectionSchema schema = CollectionSchema.newBuilder().setName("t").addFields(FieldSchema.newBuilder().setName("id").setDataType(DataType.Int64).setIsPrimaryKey(true)).build();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getServerVersion":
                    return "v2.6.2";
                case "describeCollection":
                    return v2Response(method.getName(), DescribeCollectionResponse.newBuilder().setSchema(schema).build());
                case "queryIterator":
                    return iterator;
                case "delete":
                case "insert":
                    writes.incrementAndGet();
                    attempts.add(System.nanoTime());
                    if (firstAttempt != null) {
                        firstAttempt.countDown();
                    }
                    if (failuresRemaining-- > 0) {
                        throw writeFailure;
                    }
                    return DeleteResp.builder().deleteCnt(1).build();
                default:
                    return null;
            }
        });
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    private Connection connect(int retry) throws SQLException {
        Properties props = new Properties();
        props.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        props.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://mock:19530/db1?" + MilvusKeys.MAX_RETRY + "=" + retry, props);
    }

    private SQLException failed(Statement statement, String sql) throws SQLException {
        try {
            statement.executeUpdate(sql);
            fail("Expected a failure");
            return null;
        } catch (SQLException expected) {
            return expected;
        }
    }

    @Test
    public void permanentAndUnknownErrorsAreNotRetried() throws Exception {
        // @formatter:off
        List<Exception> errors = Arrays.asList(
            new SQLSyntaxErrorException("syntax", "42000", 17),
            new SQLInvalidAuthorizationSpecException("denied", "28000"),
            new IllegalArgumentException("bad parameter"),
            new SQLException("unknown error"),
            new SQLTransientConnectionException("rejected", "08004"),
            new NullPointerException("programming error"),
            new MilvusClientException(ErrorCode.INVALID_PARAMS, "invalid"),
            new MilvusClientException(ErrorCode.COLLECTION_NOT_FOUND, "missing"),
            Status.PERMISSION_DENIED.asRuntimeException(),
            Status.INVALID_ARGUMENT.asRuntimeException(),
            new MilvusClientException(ErrorCode.RPC_ERROR, Status.PERMISSION_DENIED.asRuntimeException())
        );
        // @formatter:on
        try (Connection connection = connect(5); Statement statement = connection.createStatement()) {
            for (Exception error : errors) {
                writes.set(0);
                writeFailure = error;
                failuresRemaining = 10;
                SQLException result = failed(statement, "DELETE FROM t WHERE id > 0");
                assertEquals(error.toString(), 1, writes.get());
                assertTrue(result.getMessage(), result.getMessage().contains("1 attempt(s)"));
                if (error instanceof SQLException) {
                    assertEquals(((SQLException) error).getSQLState(), result.getSQLState());
                    assertEquals(((SQLException) error).getErrorCode(), result.getErrorCode());
                }
            }
        }
    }

    @Test
    public void transientGrpcErrorsUseBoundedBackoff() throws Exception {
        RpcUtils sdk = new RpcUtils();
        sdk.retryConfig(RetryConfig.builder().maxRetryTimes(1).build());
        writeFailure = assertThrows(MilvusClientException.class, () -> sdk.retry(() -> {
            throw Status.UNAVAILABLE.asRuntimeException();
        }));
        failuresRemaining = 2;
        try (Connection connection = connect(2); Statement statement = connection.createStatement()) {
            assertEquals(1, statement.executeUpdate("DELETE FROM t WHERE id > 0"));
        }
        assertEquals(3, writes.get());
        assertTrue(TimeUnit.NANOSECONDS.toMillis(attempts.get(1) - attempts.get(0)) >= 90);
        assertTrue(TimeUnit.NANOSECONDS.toMillis(attempts.get(2) - attempts.get(1)) >= 190);
    }

    @Test
    public void retryLimitCountsOnlyAttemptsAfterTheFirst() throws Exception {
        writeFailure = new SQLTransientConnectionException("offline", "08006", 8);
        try (Connection connection = connect(1); Statement statement = connection.createStatement()) {
            failuresRemaining = 10;
            SQLException result = failed(statement, "DELETE FROM t WHERE id > 0");
            assertEquals(2, writes.get());
            assertEquals("08006", result.getSQLState());
            assertEquals(8, result.getErrorCode());
        }
        writes.set(0);
        try (Connection connection = connect(0); Statement statement = connection.createStatement()) {
            failed(statement, "DELETE FROM t WHERE id > 0");
            assertEquals(1, writes.get());
        }
    }

    @Test
    public void sdkRateLimitCanRecover() throws Exception {
        for (io.milvus.grpc.Status status : Arrays.asList(io.milvus.grpc.Status.newBuilder().setErrorCode(io.milvus.grpc.ErrorCode.RateLimit).setReason("rate limited").build(), io.milvus.grpc.Status.newBuilder().setCode(8).setReason("rate limited").build())) {
            writeFailure = assertThrows(MilvusClientException.class, () -> new RpcUtils().handleResponse("delete", status));
            failuresRemaining = 1;
            writes.set(0);
            try (Connection connection = connect(1); Statement statement = connection.createStatement()) {
                assertEquals(1, statement.executeUpdate("DELETE FROM t WHERE id > 0"));
            }
            assertEquals(2, writes.get());
        }
    }

    private static List<RowRecord> page() {
        RowRecord row = new RowRecord();
        row.put("id", 1L);
        return Collections.singletonList(row);
    }

    @Test
    public void readFailureKeepsProgressAndCloseFailureIsSuppressed() throws Exception {
        IllegalStateException readFailure = new IllegalStateException("read failed");
        IllegalStateException closeFailure = new IllegalStateException("close failed");
        Mockito.when(iterator.next()).thenReturn(page()).thenThrow(readFailure);
        Mockito.doThrow(closeFailure).when(iterator).close();
        try (Connection connection = connect(3); Statement statement = connection.createStatement()) {
            SQLException result = failed(statement, "DELETE FROM t WHERE id > 0 LIMIT 3");
            assertTrue(result.getMessage(), result.getMessage().contains("phase=read"));
            assertTrue(result.getMessage(), result.getMessage().contains("page=2"));
            assertTrue(result.getMessage(), result.getMessage().contains("confirmedPages=1"));
            assertTrue(result.getMessage(), result.getMessage().contains("confirmedRows=1"));
            assertSame(readFailure, result.getCause().getCause());
            assertArrayEquals(new Throwable[] { closeFailure }, result.getSuppressed());
        }
        assertEquals(1, writes.get());
        Mockito.verify(iterator, Mockito.times(2)).next();
        Mockito.verify(iterator).close();
    }

    @Test
    public void closeFailureReportsCompletedWrites() throws Exception {
        Mockito.when(iterator.next()).thenReturn(page()).thenReturn(Collections.emptyList());
        Mockito.doThrow(new IllegalStateException("close failed")).when(iterator).close();
        try (Connection connection = connect(1); Statement statement = connection.createStatement()) {
            SQLException result = failed(statement, "DELETE FROM t WHERE id > 0 LIMIT 3");
            assertTrue(result.getMessage(), result.getMessage().contains("phase=close"));
            assertTrue(result.getMessage(), result.getMessage().contains("confirmedRows=1"));
            assertTrue(result.getMessage(), result.getMessage().contains("confirmedPages=1"));
        }
        assertEquals(1, writes.get());
    }

    @Test
    public void cancellationStopsBackoffBeforeAnotherWrite() throws Exception {
        writeFailure = new SQLTransientConnectionException("offline");
        failuresRemaining = 100;
        firstAttempt = new CountDownLatch(1);
        ExecutorService worker = Executors.newSingleThreadExecutor();
        try (Connection connection = connect(100); Statement statement = connection.createStatement()) {
            Future<SQLException> result = worker.submit(() -> failed(statement, "DELETE FROM t WHERE id > 0"));
            assertTrue(firstAttempt.await(5, TimeUnit.SECONDS));
            statement.cancel();
            assertTrue(result.get(5, TimeUnit.SECONDS).getMessage().toLowerCase(Locale.ROOT).contains("cancel"));
            assertEquals(1, writes.get());
        } finally {
            worker.shutdownNow();
        }
    }

    @Test
    public void queryTimeoutBoundsTheWholeRetryLoop() throws Exception {
        writeFailure = new SQLTransientConnectionException("offline");
        failuresRemaining = 100;
        try (Connection connection = connect(100); Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(1);
            SQLException result = failed(statement, "DELETE FROM t WHERE id > 0");
            assertTrue(result.toString(), result instanceof SQLTimeoutException);
            assertTrue(writes.get() < 10);
        }
    }

    @Test
    public void insertIsNotAutomaticallyRetried() throws Exception {
        writeFailure = new SQLTransientConnectionException("offline");
        failuresRemaining = 10;
        try (Connection connection = connect(5); Statement statement = connection.createStatement()) {
            failed(statement, "INSERT INTO t (id) VALUES (1)");
        }
        assertEquals(1, writes.get());
    }
}
