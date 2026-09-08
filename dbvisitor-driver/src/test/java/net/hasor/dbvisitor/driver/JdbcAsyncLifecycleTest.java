package net.hasor.dbvisitor.driver;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class JdbcAsyncLifecycleTest {
    private JdbcConnection conn;
    private ExecutorService pool;

    @Before
    public void open() throws Exception {
        Class.forName("net.hasor.dbvisitor.driver.JdbcDriver");
        Properties props = new Properties(); props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props);
        pool = Executors.newFixedThreadPool(2);
    }

    @After
    public void close() throws Exception {
        conn.close(); pool.shutdownNow(); assertTrue(pool.awaitTermination(3, TimeUnit.SECONDS));
    }

    private AdapterMemoryCursor cursor() {
        return new AdapterMemoryCursor(Collections.singletonList(new JdbcColumn("id", "int", "", "", "")), new Object[][]{{1}});
    }

    @Test(timeout = 10000)
    public void delayedNextResultWaitsInsteadOfReportingTimeout() throws Exception {
        try (JdbcStatement s = (JdbcStatement) conn.createStatement()) {
            MockAdapterRequest req = new MockAdapterRequest("async");
            s.container.prepareReceive(req); s.container.responseUpdateCount(req, 1);
            Future<Boolean> next = pool.submit(() -> s.getMoreResults());
            assertThrows(TimeoutException.class, () -> next.get(100, TimeUnit.MILLISECONDS));
            s.container.responseResult(req, cursor()); s.container.responseFinish(req);
            assertTrue(next.get(2, TimeUnit.SECONDS)); assertTrue(s.getResultSet().next());
        }
    }

    @Test(timeout = 10000)
    public void delayedFinishReportsExhaustion() throws Exception {
        try (JdbcStatement s = (JdbcStatement) conn.createStatement()) {
            MockAdapterRequest req = new MockAdapterRequest("async");
            s.container.prepareReceive(req); s.container.responseUpdateCount(req, 1);
            Future<Boolean> next = pool.submit(() -> s.getMoreResults());
            assertThrows(TimeoutException.class, () -> next.get(100, TimeUnit.MILLISECONDS));
            s.container.responseFinish(req); assertFalse(next.get(2, TimeUnit.SECONDS)); assertEquals(-1, s.getUpdateCount());
        }
    }

    @Test(timeout = 10000)
    public void asyncErrorIsPropagated() throws Exception {
        try (JdbcStatement s = (JdbcStatement) conn.createStatement()) {
            MockAdapterRequest req = new MockAdapterRequest("async");
            s.container.prepareReceive(req); s.container.responseUpdateCount(req, 1);
            Future<Boolean> next = pool.submit(() -> s.getMoreResults());
            assertThrows(TimeoutException.class, () -> next.get(100, TimeUnit.MILLISECONDS));
            s.container.responseFailed(req, new SQLException("second failed")); s.container.responseFinish(req);
            ExecutionException error = assertThrows(ExecutionException.class, () -> next.get(2, TimeUnit.SECONDS));
            assertEquals("second failed", error.getCause().getMessage());
        }
    }

    @Test(timeout = 10000)
    public void closeWakesWaiterAndRejectsLateCursors() throws Exception {
        JdbcStatement s = (JdbcStatement) conn.createStatement(); MockAdapterRequest req = new MockAdapterRequest("async");
        s.container.prepareReceive(req);
        Future<?> wait = pool.submit(() -> { s.container.waitFor(0, TimeUnit.SECONDS); return null; });
        assertThrows(TimeoutException.class, () -> wait.get(100, TimeUnit.MILLISECONDS));
        s.close();
        assertTrue(assertThrows(ExecutionException.class, () -> wait.get(2, TimeUnit.SECONDS)).getCause() instanceof SQLException);
        AdapterMemoryCursor late = cursor(), keys = cursor();
        assertFalse(s.container.responseResult(req, late, keys)); assertTrue(late.isClose()); assertTrue(keys.isClose());
    }

    @Test(timeout = 10000)
    public void cancelDoesNotWaitForExecuteMonitorAndStatementCanBeReused() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        try (JdbcStatement s = new JdbcStatement(conn) {
            protected void beforeExecute(AdapterRequest req, AdapterContainer container) throws SQLException {
                entered.countDown();
                try { if (!release.await(3,TimeUnit.SECONDS)) throw new SQLException("test barrier timeout"); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new SQLException(e); }
            }
        }) {
            Future<Boolean> execution = pool.submit(() -> s.execute("SELECT id FROM test"));
            assertTrue(entered.await(2,TimeUnit.SECONDS));
            Future<?> cancellation = pool.submit(() -> { s.cancel(); return null; });
            try { cancellation.get(1,TimeUnit.SECONDS); } finally { release.countDown(); }
            ExecutionException error = assertThrows(ExecutionException.class, () -> execution.get(2,TimeUnit.SECONDS));
            assertTrue(error.getCause() instanceof JdbcCancelledSQLException);
            assertTrue(s.execute("SELECT id FROM test"));
        } finally { release.countDown(); }
    }

    @Test(timeout = 10000)
    public void streamWaitsForRowsAndFinishIsTerminal() throws Exception {
        AdapterResultCursor cursor = new AdapterResultCursor(new MockAdapterRequest("stream"), cursor().columns());
        try {
            Future<Boolean> next = pool.submit(cursor::next);
            assertThrows(TimeoutException.class, () -> next.get(100,TimeUnit.MILLISECONDS));
            cursor.pushData(Collections.singletonMap("id",1)); assertTrue(next.get(2,TimeUnit.SECONDS)); assertEquals(1,cursor.column(1));
            Future<Boolean> end = pool.submit(cursor::next);
            assertThrows(TimeoutException.class, () -> end.get(100,TimeUnit.MILLISECONDS));
            cursor.pushFinish(); assertFalse(end.get(2,TimeUnit.SECONDS)); assertFalse(cursor.isPending());
            assertThrows(SQLException.class, () -> cursor.pushData(Collections.singletonMap("id",2)));
        } finally { cursor.close(); }
    }

    @Test(timeout = 10000)
    public void streamCloseWakesReader() throws Exception {
        AdapterResultCursor cursor = new AdapterResultCursor(new MockAdapterRequest("stream"), cursor().columns());
        Future<Boolean> next = pool.submit(cursor::next);
        assertThrows(TimeoutException.class, () -> next.get(100,TimeUnit.MILLISECONDS)); cursor.close();
        assertTrue(assertThrows(ExecutionException.class, () -> next.get(2,TimeUnit.SECONDS)).getCause() instanceof SQLException);
    }

    @Test(timeout = 10000)
    public void containerTimeoutAndInterruptAreNotSwallowed() throws Exception {
        AdapterContainer container = new AdapterContainer(conn); container.prepareReceive(new MockAdapterRequest("timeout"));
        assertThrows(SQLTimeoutException.class, () -> container.waitFor(30,TimeUnit.MILLISECONDS));
        Future<Boolean> interrupted = pool.submit(() -> {
            Thread.currentThread().interrupt();
            try { container.waitFor(0,TimeUnit.SECONDS); return false; }
            catch (SQLException e) { return Thread.currentThread().isInterrupted(); }
            finally { Thread.interrupted(); }
        });
        assertTrue(interrupted.get(2,TimeUnit.SECONDS)); container.close();
    }
}
