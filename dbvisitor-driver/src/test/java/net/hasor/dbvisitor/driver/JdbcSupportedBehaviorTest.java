package net.hasor.dbvisitor.driver;

import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;
import net.hasor.dbvisitor.driver.lob.JdbcCob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Regression tests for implemented JDBC operations, not optional capabilities. */
public class JdbcSupportedBehaviorTest {
    private JdbcConnection conn;

    @Before
    public void open() throws Exception {
        Class.forName("net.hasor.dbvisitor.driver.JdbcDriver");
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props);
    }

    @After
    public void close() {
        conn.close();
    }

    private JdbcStatement statementWith(AdapterCursor cursor, AdapterCursor keys) throws Exception {
        JdbcStatement s = (JdbcStatement) conn.createStatement();
        MockAdapterRequest req = new MockAdapterRequest("results");
        s.container.prepareReceive(req);
        if (cursor != null)
            s.container.responseResult(req, cursor, keys);
        else
            s.container.responseUpdateCount(req, 1, keys);
        s.container.responseFinish(req);
        return s;
    }

    private AdapterMemoryCursor cursor(Object... values) {
        Object[][] rows = new Object[values.length][];
        for (int i = 0; i < values.length; i++)
            rows[i] = new Object[] { values[i] };
        return new AdapterMemoryCursor(Collections.singletonList(new JdbcColumn("id", "int", "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array)), rows);
    }

    @Test
    public void preparedBindingsSurviveExecutionUntilCleared() throws Exception {
        try (JdbcPreparedStatement s = (JdbcPreparedStatement) conn.prepareStatement("UPDATE t SET a=?, b=?")) {
            s.setInt(1, 10);
            s.setString(2, "fixed");
            s.executeUpdate();
            AdapterRequest previous = s.container.getRequest();
            s.setInt(1, 20);
            assertEquals(10, previous.getArgMap().get("arg1").getValue());
            s.executeLargeUpdate();
            assertEquals(2, s.container.getRequest().getArgMap().size());
            assertEquals("fixed", s.container.getRequest().getArgMap().get("arg2").getValue());
            s.clearParameters();
            s.execute();
            assertTrue(s.container.getRequest().getArgMap().isEmpty());
        }
    }

    @Test
    public void sqlTypeOverloadsAcceptJdbcTypeAndRejectNull() throws Exception {
        try (JdbcPreparedStatement s = (JdbcPreparedStatement) conn.prepareStatement("SELECT ?")) {
            s.setObject(1, 12, JDBCType.INTEGER);
            s.execute();
            assertEquals(12, s.container.getRequest().getArgMap().get("arg1").getValue());
            assertThrows(SQLException.class, () -> s.setObject(1, 12, null));
        }
        try (CallableStatement s = conn.prepareCall("CALL p")) {
            s.registerOutParameter(1, JDBCType.INTEGER);
            s.execute();
            assertEquals(42, s.getInt(1));
            assertThrows(SQLException.class, () -> s.registerOutParameter(1, null));
        }
    }

    @Test
    public void outValuesAccessibleAndReusable() throws Exception {
        try (CallableStatement s = conn.prepareCall("CALL p")) {
            assertThrows(SQLException.class, () -> s.getInt(1));
            s.registerOutParameter(1, Types.INTEGER);
            s.registerOutParameter(2, Types.VARCHAR);
            s.execute();
            assertEquals(42, s.getInt(1));
            assertEquals("out_value", s.getString(2));
            assertFalse(s.wasNull());
            s.execute();
            assertEquals(42, s.getInt("arg1"));
        }
    }

    @Test
    public void statementMetadataWithoutCurrentResultIsNull() throws Exception {
        try (PreparedStatement s = conn.prepareStatement("SELECT id FROM test")) {
            assertNull(s.getMetaData());
            try (ResultSet r = s.executeQuery()) {
                assertEquals(2, s.getMetaData().getColumnCount());
            }
            assertFalse(s.getMoreResults());
            assertNull(s.getMetaData());
        }
        try (PreparedStatement s = conn.prepareStatement("UPDATE t SET x=1")) {
            s.executeUpdate();
            assertNull(s.getMetaData());
        }
    }

    @Test
    public void differentGetterTypesAndCaseInsensitiveLabels() throws Exception {
        try (Statement s = conn.createStatement(); ResultSet r = s.executeQuery("SELECT id FROM test")) {
            assertTrue(r.next());
            assertEquals(1, r.getInt(1));
            assertEquals("1", r.getString("ID"));
            assertEquals(1L, r.getLong("Id"));
            assertEquals(1, r.findColumn("ID"));
        }
    }

    @Test
    public void duplicateLabelsUseFirstColumnWithoutChangingOrdinalTypes() throws Exception {
        List<JdbcColumn> cols = Arrays.asList(new JdbcColumn("id", "int", "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array), new JdbcColumn("ID", "string", "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array));
        try (JdbcStatement s = statementWith(new AdapterMemoryCursor(cols, new Object[][] { { 1, "two" } }), null); ResultSet r = s.getResultSet()) {
            r.next();
            assertEquals(1, r.getInt("ID"));
            assertEquals("two", r.getString(2));
            assertEquals(1, r.findColumn("id"));
        }
    }

    @Test
    public void lookaheadPreservesValuesAndPositions() throws Exception {
        try (JdbcStatement s = statementWith(cursor(1, 2), null); ResultSet r = s.getResultSet()) {
            assertTrue(r.isBeforeFirst());
            assertEquals(0, r.getRow());
            assertTrue(r.next());
            assertTrue(r.isFirst());
            assertFalse(r.isLast());
            assertEquals(1, r.getInt(1));
            assertTrue(r.next());
            assertTrue(r.isLast());
            assertEquals(2, r.getInt(1));
            assertEquals(2, r.getRow());
            assertFalse(r.next());
            assertEquals(0, r.getRow());
            assertTrue(r.isAfterLast());
            assertFalse(r.isLast());
            assertFalse(r.isFirst());
            assertFalse(r.next());
            assertThrows(SQLException.class, () -> r.getInt(1));
        }
    }

    @Test
    public void emptyCursorHasNoPosition() throws Exception {
        try (JdbcStatement s = statementWith(cursor(), null); ResultSet r = s.getResultSet()) {
            assertFalse(r.isBeforeFirst());
            assertFalse(r.isLast());
            assertFalse(r.next());
            assertFalse(r.isBeforeFirst());
            assertFalse(r.isAfterLast());
            assertEquals(0, r.getRow());
        }
    }

    @Test
    public void namedCalendarZoneWorksForOffsetDateTime() throws Exception {
        OffsetDateTime value = OffsetDateTime.parse("2026-01-01T01:00:00Z");
        List<JdbcColumn> cols = Collections.singletonList(new JdbcColumn("time", AdapterType.OffsetDateTime, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array));
        try (JdbcStatement s = statementWith(new AdapterMemoryCursor(cols, new Object[][] { { value } }), null); ResultSet r = s.getResultSet()) {
            r.next();
            Timestamp timestamp = r.getTimestamp(1, Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai")));
            assertEquals(value.toInstant(), timestamp.toInstant());
        }
    }

    @Test
    public void closeOnCompletionBeforeAndAfterExecute() throws Exception {
        for (boolean before : new boolean[] { true, false }) {
            Statement s = conn.createStatement();
            if (before)
                s.closeOnCompletion();
            ResultSet r = s.executeQuery("SELECT id FROM test");
            if (!before)
                s.closeOnCompletion();
            assertFalse(s.isClosed());
            assertTrue(r.next());
            r.close();
            assertTrue(s.isClosed());
        }
    }

    @Test
    public void closeOnCompletionWaitsForAllKeptResults() throws Exception {
        JdbcStatement s = (JdbcStatement) conn.createStatement();
        MockAdapterRequest req = new MockAdapterRequest("multi");
        s.container.prepareReceive(req);
        s.container.responseResult(req, cursor(1));
        s.container.responseResult(req, cursor(2));
        s.container.responseFinish(req);
        s.closeOnCompletion();
        ResultSet first = s.getResultSet();
        assertTrue(s.getMoreResults(Statement.KEEP_CURRENT_RESULT));
        ResultSet second = s.getResultSet();
        second.close();
        assertFalse(s.isClosed());
        first.close();
        assertTrue(s.isClosed());
    }

    @Test
    public void reexecutionDoesNotPrematurelyCloseStatement() throws Exception {
        try (Statement s = conn.createStatement()) {
            s.closeOnCompletion();
            ResultSet first = s.executeQuery("SELECT id FROM test");
            ResultSet second = s.executeQuery("SELECT id FROM test");
            assertTrue(first.isClosed());
            assertFalse(s.isClosed());
            second.close();
            assertTrue(s.isClosed());
        }
    }

    @Test
    public void connectionClosesCurrentAndKeptCursors() throws Exception {
        AdapterMemoryCursor cursor = cursor(1), keys = cursor(99);
        JdbcStatement s = statementWith(cursor, keys);
        ResultSet r = s.getResultSet();
        ResultSet k = s.getGeneratedKeys();
        s.getMoreResults(Statement.KEEP_CURRENT_RESULT);
        conn.close();
        assertTrue(r.isClosed());
        assertTrue(k.isClosed());
        assertTrue(cursor.isClose());
        assertTrue(keys.isClose());
    }

    @Test
    public void generatedKeysUseStableWrapperAndEmptyResult() throws Exception {
        try (JdbcStatement s = statementWith(null, cursor(99))) {
            ResultSet keys = s.getGeneratedKeys();
            assertSame(keys, s.getGeneratedKeys());
            assertTrue(keys.next());
            assertEquals(99, keys.getInt(1));
            assertFalse(s.getMoreResults());
            assertTrue(keys.isClosed());
            ResultSet empty = s.getGeneratedKeys();
            assertNotNull(empty);
            assertFalse(empty.next());
        }
    }

    @Test
    public void failedResultCannotBeReadAsSuccessfulUpdateOrGeneratedKeys() throws Exception {
        try (JdbcStatement s = (JdbcStatement) conn.createStatement()) {
            MockAdapterRequest req = new MockAdapterRequest("failed");
            s.container.prepareReceive(req);
            s.container.responseFailed(req, new SQLException("failed"));
            s.container.responseFinish(req);
            assertThrows(SQLException.class, s::getLargeUpdateCount);
            assertThrows(SQLException.class, s::getGeneratedKeys);
        }
    }

    @Test
    public void arrayContractAndFree() throws Exception {
        java.sql.Array a = conn.createArrayOf("int", new Object[] { 10, 20, 30 });
        assertArrayEquals(new Object[] { 10, 20, 30 }, (Object[]) a.getArray());
        assertArrayEquals(new Object[] { 10 }, (Object[]) a.getArray(1, 1));
        assertArrayEquals(new Object[] { 30 }, (Object[]) a.getArray(3, 1));
        assertThrows(SQLException.class, () -> a.getArray(0, 1));
        assertThrows(SQLException.class, () -> a.getArray(Long.MAX_VALUE, 1));
        a.free();
        a.free();
        assertThrows(SQLException.class, a::getArray);
        assertThrows(SQLException.class, a::getBaseType);
    }

    @Test
    public void arrayTypeInferenceAndDecimalZero() {
        assertEquals(AdapterType.Array, conn.typeSupport().getTypeName(int[].class));
        assertEquals(AdapterType.Array, conn.typeSupport().getTypeName(String[].class));
        assertEquals(AdapterType.Bytes, conn.typeSupport().getTypeName(byte[].class));
        assertFalse(ConvertUtils.toBoolean(new BigDecimal("0.00"), false));
        assertTrue(ConvertUtils.toBoolean(new BigDecimal("0.01"), false));
    }

    @Test
    public void clobFreeInvalidatesAccessAndPreviouslyOpenedWriter() throws Exception {
        JdbcCob c = new JdbcCob("hello");
        java.io.Writer writer = c.setCharacterStream(1);
        c.free();
        writer.write("late");
        writer.close();
        assertThrows(SQLException.class, c::length);
        assertThrows(SQLException.class, c::getCharacterStream);
        assertThrows(SQLException.class, () -> c.getSubString(1, 1));
        assertThrows(SQLException.class, () -> c.setString(1, "x"));
    }

    @Test
    public void resultMetadataRejectsZeroIndexAndDoesNotInventNullability() throws Exception {
        try (Statement s = conn.createStatement(); ResultSet r = s.executeQuery("SELECT id FROM test")) {
            assertThrows(SQLException.class, () -> r.getMetaData().getColumnName(0));
            assertEquals(ResultSetMetaData.columnNullableUnknown, r.getMetaData().isNullable(1));
        }
    }
}
