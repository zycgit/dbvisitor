package net.hasor.dbvisitor.driver;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

public class JdbcMultipleResultsTest {
    private JdbcConnection connection() throws Exception {
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        return new JdbcConnection("jdbc:dbvisitor:mock://localhost", props);
    }

    private AdapterMemoryCursor cursor() {
        return new AdapterMemoryCursor(Collections.singletonList(new JdbcColumn("id", "int", "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array)), new Object[][] { { 1 } });
    }

    private void results(JdbcStatement stmt) throws Exception {
        MockAdapterRequest req = new MockAdapterRequest("multi");
        stmt.container.prepareReceive(req);
        stmt.container.responseResult(req, cursor());
        stmt.container.responseUpdateCount(req, 7);
        stmt.container.responseResult(req, cursor());
        stmt.container.responseFinish(req);
    }

    @Test
    public void mixedResultsAndExhaustion() throws Exception {
        try (JdbcConnection conn = connection(); JdbcStatement stmt = (JdbcStatement) conn.createStatement()) {
            assertNull(stmt.getResultSet());
            results(stmt);
            ResultSet first = stmt.getResultSet();
            assertSame(first, stmt.getResultSet());
            assertFalse(stmt.getMoreResults());
            assertTrue(first.isClosed());
            assertNull(stmt.getResultSet());
            assertEquals(7, stmt.getUpdateCount());
            assertTrue(stmt.getMoreResults());
            ResultSet last = stmt.getResultSet();
            assertEquals(-1, stmt.getUpdateCount());
            assertFalse(stmt.getMoreResults());
            assertTrue(last.isClosed());
            assertNull(stmt.getResultSet());
            assertEquals(-1, stmt.getUpdateCount());
            assertFalse(stmt.getMoreResults());
        }
    }

    @Test
    public void keepAndCloseAll() throws Exception {
        try (JdbcConnection conn = connection(); JdbcStatement stmt = (JdbcStatement) conn.createStatement()) {
            results(stmt);
            ResultSet first = stmt.getResultSet();
            assertFalse(stmt.getMoreResults(Statement.KEEP_CURRENT_RESULT));
            assertTrue(stmt.getMoreResults());
            assertFalse(first.isClosed());
            assertTrue(first.next());
            ResultSet last = stmt.getResultSet();
            assertFalse(stmt.getMoreResults(Statement.CLOSE_ALL_RESULTS));
            assertTrue(first.isClosed());
            assertTrue(last.isClosed());
        }
    }

    @Test
    public void invalidFlagDoesNotAdvance() throws Exception {
        try (JdbcConnection conn = connection(); JdbcStatement stmt = (JdbcStatement) conn.createStatement()) {
            results(stmt);
            ResultSet first = stmt.getResultSet();
            try {
                stmt.getMoreResults(-123);
                fail("invalid flag accepted");
            } catch (SQLException expected) {
                assertSame(first, stmt.getResultSet());
                assertFalse(first.isClosed());
            }
        }
    }

    @Test
    public void closeAndReexecuteReleaseKeptResults() throws Exception {
        try (JdbcConnection conn = connection(); JdbcStatement stmt = (JdbcStatement) conn.createStatement()) {
            results(stmt);
            ResultSet first = stmt.getResultSet();
            stmt.getMoreResults(Statement.KEEP_CURRENT_RESULT);
            results(stmt);
            assertTrue(first.isClosed());
            ResultSet second = stmt.getResultSet();
            stmt.getMoreResults(Statement.KEEP_CURRENT_RESULT);
            stmt.close();
            assertTrue(second.isClosed());
        }
    }

    @Test
    public void laterErrorIsNotAnUpdateCount() throws Exception {
        try (JdbcConnection conn = connection(); JdbcStatement stmt = (JdbcStatement) conn.createStatement()) {
            MockAdapterRequest req = new MockAdapterRequest("error");
            stmt.container.prepareReceive(req);
            stmt.container.responseUpdateCount(req, 1);
            stmt.container.responseFailed(req, new SQLException("later failure"));
            stmt.container.responseFinish(req);
            try {
                stmt.getMoreResults();
                fail("error not propagated");
            } catch (SQLException expected) {
                assertEquals("later failure", expected.getMessage());
            }
        }
    }
}
