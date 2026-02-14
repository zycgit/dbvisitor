package net.hasor.dbvisitor.driver;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Comprehensive tests for JdbcResultSet — covers getXxx, navigation, metadata, wasNull, updateXxx unsupported, etc. */
public class JdbcResultSetComprehensiveTest {

    private JdbcConnection conn;

    @Before
    public void setUp() throws Exception {
        Class.forName("net.hasor.dbvisitor.driver.JdbcDriver");
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);
    }

    @After
    public void tearDown() throws Exception {
        if (conn != null && !conn.isClosed())
            conn.close();
    }

    // ==================== getXxx by index — types data ====================
    @Test
    public void getInt_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals(100, rs.getInt(1));
        rs.close();
        stmt.close();
    }

    @Test
    public void getString_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals("str", rs.getString(2));
        rs.close();
        stmt.close();
    }

    @Test
    public void getBoolean_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(3));
        rs.close();
        stmt.close();
    }

    @Test
    public void getFloat_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals(1.1f, rs.getFloat(4), 0.01);
        rs.close();
        stmt.close();
    }

    @Test
    public void getDouble_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals(2.2d, rs.getDouble(5), 0.01);
        rs.close();
        stmt.close();
    }

    @Test
    public void getDate_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        Date d = rs.getDate(6);
        assertNotNull(d);
        rs.close();
        stmt.close();
    }

    @Test
    public void getTimestamp_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        Timestamp ts = rs.getTimestamp(7);
        assertNotNull(ts);
        rs.close();
        stmt.close();
    }

    @Test
    public void getObject_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        Object obj = rs.getObject(1);
        assertNotNull(obj);
        rs.close();
        stmt.close();
    }

    @Test
    public void getObject_class_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        Integer val = rs.getObject(1, Integer.class);
        assertEquals(100, (int) val);
        rs.close();
        stmt.close();
    }

    @Test
    public void getByte_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals(100, rs.getByte(1));
        rs.close();
        stmt.close();
    }

    @Test
    public void getShort_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals(100, rs.getShort(1));
        rs.close();
        stmt.close();
    }

    @Test
    public void getLong_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals(100L, rs.getLong(1));
        rs.close();
        stmt.close();
    }

    @Test
    public void getBigDecimal_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        BigDecimal bd = rs.getBigDecimal(1);
        assertNotNull(bd);
        rs.close();
        stmt.close();
    }

    @Test
    public void getBytes_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        byte[] bytes = rs.getBytes(8);
        assertNotNull(bytes);
        rs.close();
        stmt.close();
    }

    @Test
    public void getNString_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals("str", rs.getNString(2));
        rs.close();
        stmt.close();
    }

    @Test
    public void getTime_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getTime(7);
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    // ==================== getXxx by name ====================
    @Test
    public void getInt_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals(100, rs.getInt("c_int"));
        rs.close();
        stmt.close();
    }

    @Test
    public void getString_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals("str", rs.getString("c_string"));
        rs.close();
        stmt.close();
    }

    @Test
    public void getBoolean_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean("c_bool"));
        rs.close();
        stmt.close();
    }

    @Test
    public void getFloat_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals(1.1f, rs.getFloat("c_float"), 0.01);
        rs.close();
        stmt.close();
    }

    @Test
    public void getDouble_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals(2.2d, rs.getDouble("c_double"), 0.01);
        rs.close();
        stmt.close();
    }

    @Test
    public void getDate_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertNotNull(rs.getDate("c_date"));
        rs.close();
        stmt.close();
    }

    @Test
    public void getDate_calendar() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertNotNull(rs.getDate(6, Calendar.getInstance()));
        rs.close();
        stmt.close();
    }

    @Test
    public void getDate_calendar_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertNotNull(rs.getDate("c_date", Calendar.getInstance()));
        rs.close();
        stmt.close();
    }

    @Test
    public void getTimestamp_calendar() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertNotNull(rs.getTimestamp(7, Calendar.getInstance()));
        rs.close();
        stmt.close();
    }

    @Test
    public void getTimestamp_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertNotNull(rs.getTimestamp("c_timestamp"));
        rs.close();
        stmt.close();
    }

    @Test
    public void getTimestamp_byName_calendar() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertNotNull(rs.getTimestamp("c_timestamp", Calendar.getInstance()));
        rs.close();
        stmt.close();
    }

    @Test
    public void getTime_calendar() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getTime(7, Calendar.getInstance());
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getTime_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getTime("c_timestamp");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getTime_byName_calendar() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getTime("c_timestamp", Calendar.getInstance());
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getByte_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        rs.getByte("c_int");
        rs.close();
        stmt.close();
    }

    @Test
    public void getShort_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        rs.getShort("c_int");
        rs.close();
        stmt.close();
    }

    @Test
    public void getLong_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        rs.getLong("c_int");
        rs.close();
        stmt.close();
    }

    @Test
    public void getBigDecimal_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertNotNull(rs.getBigDecimal("c_int"));
        rs.close();
        stmt.close();
    }

    @Test
    public void getBytes_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertNotNull(rs.getBytes("c_blob"));
        rs.close();
        stmt.close();
    }

    @Test
    public void getObject_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertNotNull(rs.getObject("c_int"));
        rs.close();
        stmt.close();
    }

    @Test
    public void getObject_byName_class() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        assertEquals(100, (int) rs.getObject("c_int", Integer.class));
        rs.close();
        stmt.close();
    }

    // ==================== wasNull ====================
    @Test
    public void wasNull_nonNull() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        rs.getInt(1);
        assertFalse(rs.wasNull());
        rs.close();
        stmt.close();
    }

    @Test
    public void wasNull_null() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM nullrow");
        assertTrue(rs.next());
        rs.getObject(1);
        assertTrue(rs.wasNull());
        rs.close();
        stmt.close();
    }

    // ==================== navigation ====================
    @Test
    public void next_and_close() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM multi");
        assertTrue(rs.next()); // row 1
        assertTrue(rs.next()); // row 2
        assertTrue(rs.next()); // row 3
        assertFalse(rs.next()); // no more
        rs.close();
        assertTrue(rs.isClosed());
        stmt.close();
    }

    @Test
    public void getRow_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM multi");
        assertEquals(0, rs.getRow());
        rs.next();
        assertEquals(1, rs.getRow());
        rs.next();
        assertEquals(2, rs.getRow());
        rs.close();
        stmt.close();
    }

    @Test
    public void isBeforeFirst_isFirst_isLast() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM multi");
        assertTrue(rs.isBeforeFirst());
        rs.next();
        assertTrue(rs.isFirst());
        rs.next();
        rs.next();
        assertFalse(rs.isLast()); // wasLast only true after next() returns false
        rs.next(); // returns false, sets wasLast
        assertTrue(rs.isLast());
        assertTrue(rs.isAfterLast());
        rs.close();
        stmt.close();
    }

    @Test(expected = SQLException.class)
    public void absolute_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM multi");
        rs.absolute(1);
    }

    @Test(expected = SQLException.class)
    public void relative_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM multi");
        rs.relative(1);
    }

    @Test(expected = SQLException.class)
    public void previous_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM multi");
        rs.previous();
    }

    @Test(expected = SQLException.class)
    public void first_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM multi");
        rs.first();
    }

    @Test(expected = SQLException.class)
    public void last_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM multi");
        rs.last();
    }

    @Test(expected = SQLException.class)
    public void beforeFirst_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM multi");
        rs.beforeFirst();
    }

    @Test(expected = SQLException.class)
    public void afterLast_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM multi");
        rs.afterLast();
    }

    // ==================== metadata ====================
    @Test
    public void getMetaData_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        ResultSetMetaData rsmd = rs.getMetaData();
        assertNotNull(rsmd);
        assertEquals(10, rsmd.getColumnCount());
        assertEquals("c_int", rsmd.getColumnName(1));
        assertEquals("int", rsmd.getColumnTypeName(1));
        assertEquals("types", rsmd.getTableName(1));
        assertEquals("default", rsmd.getCatalogName(1));
        // default value methods
        assertFalse(rsmd.isAutoIncrement(1));
        assertFalse(rsmd.isCaseSensitive(1));
        assertFalse(rsmd.isSearchable(1));
        assertFalse(rsmd.isCurrency(1));
        assertEquals(0, rsmd.isNullable(1));
        assertFalse(rsmd.isSigned(1));
        assertEquals(0, rsmd.getColumnDisplaySize(1));
        assertFalse(rsmd.isReadOnly(1));
        assertFalse(rsmd.isWritable(1));
        assertFalse(rsmd.isDefinitelyWritable(1));
        assertEquals(0, rsmd.getPrecision(1));
        assertEquals(0, rsmd.getScale(1));
        rs.close();
        stmt.close();
    }

    @Test
    public void getMetaData_columnLabel() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        ResultSetMetaData rsmd = rs.getMetaData();
        assertEquals("c_int", rsmd.getColumnLabel(1));
        assertEquals("c_string", rsmd.getColumnLabel(2));
        rs.close();
        stmt.close();
    }

    @Test
    public void getMetaData_types() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        ResultSetMetaData rsmd = rs.getMetaData();
        rsmd.getColumnType(1);
        rsmd.getColumnClassName(1);
        rs.close();
        stmt.close();
    }

    // ==================== properties ====================
    @Test
    public void getType_andConcurrency() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM test");
        assertEquals(ResultSet.TYPE_FORWARD_ONLY, rs.getType());
        assertEquals(ResultSet.CONCUR_READ_ONLY, rs.getConcurrency());
        assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, rs.getHoldability());
        rs.close();
        stmt.close();
    }

    @Test
    public void fetchDirection_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM test");
        assertEquals(ResultSet.FETCH_FORWARD, rs.getFetchDirection());
        rs.setFetchDirection(ResultSet.FETCH_FORWARD);
        rs.close();
        stmt.close();
    }

    @Test(expected = SQLException.class)
    public void setFetchDirection_reverse_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM test");
        rs.setFetchDirection(ResultSet.FETCH_REVERSE);
    }

    @Test
    public void fetchSize_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM test");
        int currentFetchSize = rs.getFetchSize();
        rs.setFetchSize(currentFetchSize); // same value should not throw
        assertEquals(currentFetchSize, rs.getFetchSize());
        try {
            rs.setFetchSize(currentFetchSize + 1);
            fail();
        } catch (SQLException expected) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getStatement_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM test");
        assertNotNull(rs.getStatement());
        rs.close();
        stmt.close();
    }

    @Test
    public void getCursorName_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM test");
        assertNull(rs.getCursorName());
        rs.close();
        stmt.close();
    }

    @Test
    public void warnings_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM test");
        assertNull(rs.getWarnings());
        rs.clearWarnings();
        rs.close();
        stmt.close();
    }

    @Test
    public void findColumn_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertEquals(1, rs.findColumn("c_int"));
        assertEquals(2, rs.findColumn("c_string"));
        rs.close();
        stmt.close();
    }

    // ==================== stream accessors ====================
    @Test
    public void getAsciiStream_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getAsciiStream(2);
        } catch (Exception ignore) {
        }
        try {
            rs.getAsciiStream("c_string");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getBinaryStream_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getBinaryStream(8);
        } catch (Exception ignore) {
        }
        try {
            rs.getBinaryStream("c_blob");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getCharacterStream_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getCharacterStream(2);
        } catch (Exception ignore) {
        }
        try {
            rs.getCharacterStream("c_string");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getNCharacterStream_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getNCharacterStream(2);
        } catch (Exception ignore) {
        }
        try {
            rs.getNCharacterStream("c_string");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getUnicodeStream_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getUnicodeStream(2);
        } catch (Exception ignore) {
        }
        try {
            rs.getUnicodeStream("c_string");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    // ==================== LOB accessors ====================
    @Test
    public void getClob_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getClob(9);
        } catch (Exception ignore) {
        }
        try {
            rs.getClob("c_clob");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getNClob_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getNClob(9);
        } catch (Exception ignore) {
        }
        try {
            rs.getNClob("c_clob");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getBlob_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getBlob(8);
        } catch (Exception ignore) {
        }
        try {
            rs.getBlob("c_blob");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getURL_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getURL(2);
        } catch (Exception ignore) {
        }
        try {
            rs.getURL("c_string");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getSQLXML_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getSQLXML(2);
        } catch (Exception ignore) {
        }
        try {
            rs.getSQLXML("c_string");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getRowId_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getRowId(1);
        } catch (Exception ignore) {
        }
        try {
            rs.getRowId("c_int");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getRef_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getRef(1);
        } catch (Exception ignore) {
        }
        try {
            rs.getRef("c_int");
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getObject_map_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        assertTrue(rs.next());
        try {
            rs.getObject(1, new java.util.HashMap<String, Class<?>>());
        } catch (Exception ignore) {
        }
        try {
            rs.getObject("c_int", new java.util.HashMap<String, Class<?>>());
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    // ==================== updateXxx — all unsupported ====================
    @Test(expected = SQLException.class)
    public void updateNull_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNull(1);
    }

    @Test(expected = SQLException.class)
    public void updateBoolean_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBoolean(1, true);
    }

    @Test(expected = SQLException.class)
    public void updateByte_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateByte(1, (byte) 1);
    }

    @Test(expected = SQLException.class)
    public void updateShort_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateShort(1, (short) 1);
    }

    @Test(expected = SQLException.class)
    public void updateInt_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateInt(1, 1);
    }

    @Test(expected = SQLException.class)
    public void updateLong_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateLong(1, 1L);
    }

    @Test(expected = SQLException.class)
    public void updateFloat_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateFloat(1, 1.0f);
    }

    @Test(expected = SQLException.class)
    public void updateDouble_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateDouble(1, 1.0d);
    }

    @Test(expected = SQLException.class)
    public void updateString_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateString(1, "x");
    }

    @Test(expected = SQLException.class)
    public void updateBytes_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBytes(1, new byte[] { 1 });
    }

    @Test(expected = SQLException.class)
    public void updateObject_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateObject(1, "x");
    }

    @Test(expected = SQLException.class)
    public void updateBigDecimal_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBigDecimal(1, BigDecimal.ONE);
    }

    @Test(expected = SQLException.class)
    public void updateDate_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateDate(1, new Date(0));
    }

    @Test(expected = SQLException.class)
    public void updateTime_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateTime(1, new Time(0));
    }

    @Test(expected = SQLException.class)
    public void updateTimestamp_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateTimestamp(1, new Timestamp(0));
    }

    @Test(expected = SQLException.class)
    public void insertRow_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.insertRow();
    }

    @Test(expected = SQLException.class)
    public void updateRow_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateRow();
    }

    @Test(expected = SQLException.class)
    public void deleteRow_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.deleteRow();
    }

    @Test(expected = SQLException.class)
    public void refreshRow_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.refreshRow();
    }

    @Test(expected = SQLException.class)
    public void cancelRowUpdates_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.cancelRowUpdates();
    }

    @Test(expected = SQLException.class)
    public void moveToInsertRow_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.moveToInsertRow();
    }

    @Test(expected = SQLException.class)
    public void moveToCurrentRow_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.moveToCurrentRow();
    }

    // ==================== updateXxx by name ====================
    @Test(expected = SQLException.class)
    public void updateNull_byName_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNull("c_int");
    }

    @Test(expected = SQLException.class)
    public void updateInt_byName_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateInt("c_int", 1);
    }

    @Test(expected = SQLException.class)
    public void updateString_byName_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateString("c_string", "x");
    }

    @Test(expected = SQLException.class)
    public void updateLong_byName_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateLong("c_int", 1L);
    }

    @Test(expected = SQLException.class)
    public void updateObject_byName_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateObject("c_int", 1);
    }

    // unwrap
    @Test
    public void unwrap_test() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        try {
            rs.unwrap(ResultSet.class);
        } catch (Exception ignore) {
        }
        try {
            rs.isWrapperFor(ResultSet.class);
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void rowInserted_rowUpdated_rowDeleted() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        try {
            rs.rowInserted();
            fail();
        } catch (SQLFeatureNotSupportedException expected) {
        }
        try {
            rs.rowUpdated();
            fail();
        } catch (SQLFeatureNotSupportedException expected) {
        }
        try {
            rs.rowDeleted();
            fail();
        } catch (SQLFeatureNotSupportedException expected) {
        }
        rs.close();
        stmt.close();
    }
}
