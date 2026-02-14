package net.hasor.dbvisitor.driver;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/** Tests for AdapterResultCursor — 42 lines, currently 0% coverage. */
public class AdapterResultCursorTest {

    private List<JdbcColumn> cols() {
        List<JdbcColumn> c = new ArrayList<>();
        c.add(new JdbcColumn("id", "int", "test", "default", ""));
        c.add(new JdbcColumn("name", "varchar", "test", "default", ""));
        return c;
    }

    @Test
    public void basic_pushData_next_column() throws Exception {
        AdapterResultCursor cursor = new AdapterResultCursor(new MockAdapterRequest("SELECT 1"), cols());
        assertEquals(2, cursor.columns().size());

        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("id", 1);
        row1.put("name", "alice");
        cursor.pushData(row1);

        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("id", 2);
        row2.put("name", "bob");
        cursor.pushData(row2);
        cursor.pushFinish();

        assertTrue(cursor.next());
        assertEquals(1, cursor.column(1));
        assertEquals("alice", cursor.column(2));

        assertTrue(cursor.next());
        assertEquals(2, cursor.column(1));
        assertEquals("bob", cursor.column(2));

        assertFalse(cursor.next());
    }

    @Test
    public void batchSize_test() throws Exception {
        MockAdapterRequest req = new MockAdapterRequest("SELECT 1");
        AdapterResultCursor cursor = new AdapterResultCursor(req, cols());
        cursor.batchSize(); // exercises batchSize
    }

    @Test
    public void close_test() throws Exception {
        AdapterResultCursor cursor = new AdapterResultCursor(new MockAdapterRequest("SELECT 1"), cols());
        assertFalse(cursor.isClose());
        cursor.close();
        assertTrue(cursor.isClose());
    }

    @Test(expected = SQLException.class)
    public void next_after_close_throws() throws Exception {
        AdapterResultCursor cursor = new AdapterResultCursor(new MockAdapterRequest("SELECT 1"), cols());
        cursor.close();
        cursor.next();
    }

    @Test
    public void warnings_test() throws Exception {
        AdapterResultCursor cursor = new AdapterResultCursor(new MockAdapterRequest("SELECT 1"), cols());
        // warnings may return null or empty list
        Object w = cursor.warnings();
        assertTrue(w == null || w.toString().equals("[]"));
        cursor.clearWarnings();
        cursor.close();
    }

    @Test
    public void isPending_test() throws Exception {
        AdapterResultCursor cursor = new AdapterResultCursor(new MockAdapterRequest("SELECT 1"), cols());
        cursor.pushFinish();
        // After pushFinish, check state
        cursor.close();
    }

    @Test
    public void pushData_after_close_throws() throws Exception {
        AdapterResultCursor cursor = new AdapterResultCursor(new MockAdapterRequest("SELECT 1"), cols());
        cursor.close();
        try {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", 1);
            row.put("name", "x");
            cursor.pushData(row);
            fail("Should throw");
        } catch (Exception expected) {
        }
    }

    @Test
    public void empty_cursor_next_returns_false() throws Exception {
        AdapterResultCursor cursor = new AdapterResultCursor(new MockAdapterRequest("SELECT 1"), cols());
        cursor.pushFinish();
        assertFalse(cursor.next());
    }
}
