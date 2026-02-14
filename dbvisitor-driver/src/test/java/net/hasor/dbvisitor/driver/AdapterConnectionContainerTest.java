package net.hasor.dbvisitor.driver;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Tests targeting AdapterConnection (unwrap, timer, features) and AdapterContainer (package-private). */
public class AdapterConnectionContainerTest {

    private JdbcConnection        conn;
    private MockAdapterConnection mockConn;

    @Before
    public void setUp() throws Exception {
        Class.forName("net.hasor.dbvisitor.driver.JdbcDriver");
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);
        mockConn = (MockAdapterConnection) conn.adapterConnection();
    }

    @After
    public void tearDown() throws Exception {
        if (conn != null && !conn.isClosed())
            conn.close();
    }

    // ==================== AdapterConnection: basic getters ====================
    @Test
    public void getObjectId_notNull() {
        String id = mockConn.getObjectId();
        assertNotNull(id);
        assertEquals(32, id.length()); // UUID without dashes
    }

    @Test
    public void getFeatures_notNull() {
        AdapterFeatures f = mockConn.getFeatures();
        assertNotNull(f);
    }

    @Test
    public void getDefaultGeneratedKeys_default() {
        int val = mockConn.getDefaultGeneratedKeys();
        // Default features: ReturnGeneratedKeys is false → NO_GENERATED_KEYS
        assertEquals(Statement.NO_GENERATED_KEYS, val);
    }

    @Test
    public void getDefaultGeneratedKeys_enabled() {
        mockConn.getFeatures().addFeature(AdapterFeatureKey.ReturnGeneratedKeys, true);
        assertEquals(Statement.RETURN_GENERATED_KEYS, mockConn.getDefaultGeneratedKeys());
    }

    @Test
    public void getInfo() {
        AdapterInfo info = mockConn.getInfo();
        assertNotNull(info);
        assertEquals("jdbc:dbvisitor:mock://localhost", info.getUrl());
    }

    // ==================== AdapterConnection: startTimer / stopTimer ====================
    @Test
    public void startTimer_andStop() throws Exception {
        AtomicBoolean fired = new AtomicBoolean(false);
        mockConn.startTimer("t1", 5000, t -> fired.set(true));
        // Stop before it fires
        mockConn.stopTimer("t1");
        Thread.sleep(100);
        assertFalse(fired.get());
    }

    @Test
    public void stopTimer_nonExistent() {
        // Should be safe / idempotent
        mockConn.stopTimer("nonexistent");
    }

    // ==================== AdapterConnection: unwrap with various target types ====================
    @Test
    public void unwrap_iface_isInstance() throws Exception {
        // target itself implements iface
        Object result = mockConn.unwrap(MockAdapterConnection.class, mockConn);
        assertSame(mockConn, result);
    }

    @Test
    public void unwrap_connection_from_JdbcConnection() throws Exception {
        Connection c = mockConn.unwrap(Connection.class, conn);
        assertSame(conn, c);
    }

    @Test
    public void unwrap_transactionSupport_from_JdbcConnection() throws Exception {
        TransactionSupport ts = mockConn.unwrap(TransactionSupport.class, conn);
        assertNotNull(ts);
    }

    @Test
    public void unwrap_from_JdbcDatabaseMetaData() throws Exception {
        DatabaseMetaData dbmd = conn.getMetaData();
        Connection c = mockConn.unwrap(Connection.class, dbmd);
        assertSame(conn, c);
    }

    @Test
    public void unwrap_from_JdbcStatement() throws Exception {
        Statement stmt = conn.createStatement();
        Connection c = mockConn.unwrap(Connection.class, stmt);
        assertSame(conn, c);
        // Also try unwrapping as Statement
        Statement s = mockConn.unwrap(Statement.class, stmt);
        assertSame(stmt, s);
        stmt.close();
    }

    @Test
    public void unwrap_from_JdbcResultSet() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM multi");
        // Unwrap as ResultSet
        ResultSet r = mockConn.unwrap(ResultSet.class, rs);
        assertSame(rs, r);
        // Unwrap as Statement
        Statement s = mockConn.unwrap(Statement.class, rs);
        assertSame(stmt, s);
        // Unwrap as Connection
        Connection c = mockConn.unwrap(Connection.class, rs);
        assertSame(conn, c);
        rs.close();
        stmt.close();
    }

    @Test
    public void unwrap_from_JdbcResultSetMetaData() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM multi");
        ResultSetMetaData rsmd = rs.getMetaData();
        // Unwrap Connection from ResultSetMetaData
        Connection c = mockConn.unwrap(Connection.class, rsmd);
        assertSame(conn, c);
        rs.close();
        stmt.close();
    }

    @Test
    public void unwrap_from_JdbcParameterMetaData() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO t VALUES (?)");
        ps.setInt(1, 1);
        ParameterMetaData pmd = ps.getParameterMetaData();
        Connection c = mockConn.unwrap(Connection.class, pmd);
        assertSame(conn, c);
        Statement s = mockConn.unwrap(Statement.class, pmd);
        assertSame(ps, s);
        ps.close();
    }

    @Test
    public void unwrap_unknown_target() throws Exception {
        // target is not a JDBC type — falls through to `unwrap(iface)` which returns null
        Object result = mockConn.unwrap(Runnable.class, "some string");
        assertNull(result);
    }

    @Test
    public void unwrap_unknown_iface() throws Exception {
        // iface not matching any branch → falls through to `unwrap(iface)` which returns null
        Object result = mockConn.unwrap(Runnable.class, conn);
        assertNull(result);
    }

    // ==================== AdapterContainer — direct tests (package-private class) ====================
    @Test
    public void container_lifecycle() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        assertEquals(AdapterReceiveState.Ready, container.getState());

        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req);
        assertEquals(AdapterReceiveState.Pending, container.getState());
        assertSame(req, container.getRequest());
    }

    @Test(expected = SQLException.class)
    public void container_prepareReceive_whenPending() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req1 = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req1);
        // Should throw — already pending
        container.prepareReceive(new MockAdapterRequest("SELECT 2"));
    }

    @Test
    public void container_responseResult_simple() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req);

        List<JdbcColumn> cols = new ArrayList<>();
        cols.add(new JdbcColumn("id", "int", "t", "c", ""));
        AdapterMemoryCursor cursor = new AdapterMemoryCursor(cols, new Object[][] { { 1 } });
        boolean ok = container.responseResult(req, cursor);
        assertTrue(ok);
        assertEquals(AdapterReceiveState.Receive, container.getState());
    }

    @Test
    public void container_responseResult_withGeneratedKeys() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("INSERT INTO t");
        container.prepareReceive(req);

        List<JdbcColumn> cols = new ArrayList<>();
        cols.add(new JdbcColumn("id", "int", "t", "c", ""));
        AdapterMemoryCursor cursor = new AdapterMemoryCursor(cols, new Object[][] { { 1 } });
        AdapterMemoryCursor keys = new AdapterMemoryCursor(cols, new Object[][] { { 99 } });
        boolean ok = container.responseResult(req, cursor, keys);
        assertTrue(ok);
    }

    @Test
    public void container_responseUpdateCount_simple() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("INSERT INTO t");
        container.prepareReceive(req);
        boolean ok = container.responseUpdateCount(req, 5);
        assertTrue(ok);
    }

    @Test
    public void container_responseUpdateCount_withGeneratedKeys() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("INSERT INTO t");
        container.prepareReceive(req);
        List<JdbcColumn> cols = new ArrayList<>();
        cols.add(new JdbcColumn("id", "int", "t", "c", ""));
        AdapterMemoryCursor keys = new AdapterMemoryCursor(cols, new Object[][] { { 99 } });
        boolean ok = container.responseUpdateCount(req, 1, keys);
        assertTrue(ok);
    }

    @Test
    public void container_responseParameter() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("{call proc}");
        container.prepareReceive(req);
        boolean ok = container.responseParameter(req, "p1", "int", 42);
        assertTrue(ok);
        container.responseFinish(req);
        // Now get out parameters
        AdapterCursor outCursor = container.getOutParameters();
        assertNotNull(outCursor);
        assertTrue(outCursor.next());
        assertEquals(42, outCursor.column(1));
    }

    @Test(expected = NullPointerException.class)
    public void container_responseParameter_blankName() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("{call proc}");
        container.prepareReceive(req);
        container.responseParameter(req, "", "int", 42);
    }

    @Test
    public void container_responseFinish() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req);

        List<JdbcColumn> cols = new ArrayList<>();
        cols.add(new JdbcColumn("id", "int", "t", "c", ""));
        AdapterMemoryCursor cursor = new AdapterMemoryCursor(cols, new Object[][] { { 1 } });
        container.responseResult(req, cursor);
        boolean ok = container.responseFinish(req);
        assertTrue(ok);
        assertEquals(AdapterReceiveState.Ready, container.getState());
    }

    @Test
    public void container_responseFailed() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req);
        boolean ok = container.responseFailed(req, new RuntimeException("test error"));
        assertTrue(ok);

        // waitFor should throw the error
        try {
            container.waitFor(0, TimeUnit.MILLISECONDS);
            fail("expected SQLException");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("test error"));
        }
    }

    @Test
    public void container_response_unrelatedRequest() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req1 = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req1);

        MockAdapterRequest req2 = new MockAdapterRequest("SELECT 2");
        // Different traceId — should return false
        boolean ok = container.responseResult(req2, new AdapterMemoryCursor(new ArrayList<JdbcColumn>(), new Object[0][]));
        assertFalse(ok);
    }

    @Test
    public void container_response_whenReady() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        // No prepareReceive — state is Ready
        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        boolean ok = container.responseUpdateCount(req, 1);
        assertFalse(ok);
    }

    @Test
    public void container_nextResult() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req);

        List<JdbcColumn> cols = new ArrayList<>();
        cols.add(new JdbcColumn("id", "int", "t", "c", ""));
        container.responseResult(req, new AdapterMemoryCursor(cols, new Object[][] { { 1 } }));
        container.responseFinish(req);

        AdapterResponse first = container.firstResult();
        assertNotNull(first);
        assertTrue(first.isResult());

        boolean hasMore = container.nextResult(0, TimeUnit.MILLISECONDS);
        assertFalse(hasMore);
    }

    @Test(expected = SQLException.class)
    public void container_nextResult_whenPending() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req);
        // State is Pending — should throw
        container.nextResult(0, TimeUnit.MILLISECONDS);
    }

    @Test(expected = SQLException.class)
    public void container_getOutParameters_whenPending() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("{call proc}");
        container.prepareReceive(req);
        // State is Pending — should throw
        container.getOutParameters();
    }

    @Test
    public void container_firstResult_whenReady() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        // No prepareReceive, state is Ready, response list is empty
        assertNull(container.firstResult());
    }

    @Test(expected = SQLException.class)
    public void container_firstResult_whenPending() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        container.prepareReceive(new MockAdapterRequest("SELECT 1"));
        container.firstResult();
    }

    @Test
    public void container_waitFor_timeout() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req);

        // waitFor with timeout — response is empty, state is Pending → will wait then throw
        try {
            container.waitFor(50, TimeUnit.MILLISECONDS);
            fail("expected SQLException");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("no data received"));
        }
    }

    @Test
    public void container_responseFailed_unrelated() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req);
        MockAdapterRequest other = new MockAdapterRequest("SELECT 2");
        boolean ok = container.responseFailed(other, new RuntimeException("err"));
        assertFalse(ok);
    }

    @Test
    public void container_responseFinish_unrelated() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req);
        MockAdapterRequest other = new MockAdapterRequest("SELECT 2");
        boolean ok = container.responseFinish(other);
        assertFalse(ok);
    }

    @Test
    public void container_responseParameter_unrelated() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req);
        MockAdapterRequest other = new MockAdapterRequest("SELECT 2");
        boolean ok = container.responseParameter(other, "p1", "int", 42);
        assertFalse(ok);
    }

    // ==================== AdapterResponse coverage ====================
    @Test
    public void adapterResponse_ofError() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        container.prepareReceive(req);
        container.responseFailed(req, new SQLException("boom"));
        container.responseFinish(req);

        AdapterResponse first = container.firstResult();
        assertNotNull(first);
        assertTrue(first.isError());
        assertFalse(first.isResult());
        assertFalse(first.isPending());
        assertNotNull(first.toError());
    }

    @Test
    public void adapterResponse_ofUpdateCount() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("INSERT INTO t");
        container.prepareReceive(req);
        container.responseUpdateCount(req, 7);
        container.responseFinish(req);

        AdapterResponse first = container.firstResult();
        assertNotNull(first);
        assertFalse(first.isResult());
        assertFalse(first.isError());
        assertEquals(7, first.getUpdateCount());
        assertNull(first.toGeneratedKeys());
    }

    @Test
    public void adapterResponse_ofCursor_withGenKeys() throws Exception {
        AdapterContainer container = new AdapterContainer(conn);
        MockAdapterRequest req = new MockAdapterRequest("INSERT INTO t");
        container.prepareReceive(req);

        List<JdbcColumn> cols = new ArrayList<>();
        cols.add(new JdbcColumn("id", "int", "t", "c", ""));
        AdapterMemoryCursor cursor = new AdapterMemoryCursor(cols, new Object[][] { { 1 } });
        AdapterMemoryCursor keys = new AdapterMemoryCursor(cols, new Object[][] { { 99 } });
        container.responseResult(req, cursor, keys);
        container.responseFinish(req);

        AdapterResponse first = container.firstResult();
        assertNotNull(first);
        assertTrue(first.isResult());
        assertNotNull(first.toCursor());
        assertNotNull(first.toGeneratedKeys());
    }
}
