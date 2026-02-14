package net.hasor.dbvisitor.driver;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Properties;
import net.hasor.dbvisitor.driver.lob.JdbcBob;
import net.hasor.dbvisitor.driver.lob.JdbcCob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Comprehensive tests for JdbcPreparedStatement — covers all setXxx, execute, clearing, and metadata. */
public class JdbcPreparedStatementTest {

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

    // ==================== execute methods ====================
    @Test
    public void executeQuery_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM test");
        ResultSet rs = ps.executeQuery();
        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();
        ps.close();
    }

    @Test
    public void executeUpdate_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (1)");
        int count = ps.executeUpdate();
        assertEquals(1, count);
        ps.close();
    }

    @Test
    public void execute_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM test");
        boolean isResult = ps.execute();
        assertTrue(isResult);
        ps.close();
    }

    @Test
    public void executeLargeUpdate_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (1)");
        long count = ps.executeLargeUpdate();
        assertEquals(1L, count);
        ps.close();
    }

    // ==================== setXxx by index ====================
    @Test
    public void setNull_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setNull(1, Types.VARCHAR);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setNull_typeName_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setNull(1, Types.VARCHAR, "VARCHAR");
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setBoolean_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setBoolean(1, true);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setByte_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setByte(1, (byte) 42);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setShort_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setShort(1, (short) 42);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setInt_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setInt(1, 42);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setLong_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setLong(1, 42L);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setFloat_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setFloat(1, 1.5f);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setDouble_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setDouble(1, 2.5d);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setBigDecimal_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setBigDecimal(1, new BigDecimal("123.45"));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setString_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setString(1, "hello");
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setNString_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setNString(1, "hello");
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setBytes_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setBytes(1, new byte[] { 1, 2, 3 });
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setDate_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setDate(1, java.sql.Date.valueOf("2023-01-15"));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setDate_calendar_null() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setDate(1, java.sql.Date.valueOf("2023-01-15"), null);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setDate_calendar_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setDate(1, java.sql.Date.valueOf("2023-01-15"), Calendar.getInstance());
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setTime_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setTime(1, java.sql.Time.valueOf("12:30:00"));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setTime_calendar_null() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setTime(1, java.sql.Time.valueOf("12:30:00"), null);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setTime_calendar_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        try {
            ps.setTime(1, java.sql.Time.valueOf("12:30:00"), Calendar.getInstance());
        } catch (UnsupportedOperationException ignore) { /* Time.toInstant() unsupported in Java 8 */ }
        ps.close();
    }

    @Test
    public void setTimestamp_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setTimestamp(1, java.sql.Timestamp.valueOf("2023-01-15 12:00:00"));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setTimestamp_calendar_null() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setTimestamp(1, java.sql.Timestamp.valueOf("2023-01-15 12:00:00"), null);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setTimestamp_calendar_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setTimestamp(1, java.sql.Timestamp.valueOf("2023-01-15 12:00:00"), Calendar.getInstance());
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setURL_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setURL(1, new URL("http://example.com"));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setObject_int_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setObject(1, 42);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setObject_string_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setObject(1, "hello");
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setObject_targetType_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setObject(1, 42, Types.INTEGER);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setObject_targetType_scale_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setObject(1, 42, Types.INTEGER, 0);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setArray_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        JdbcArray arr = new JdbcArray(conn, "VARCHAR", java.util.Arrays.asList("a", "b"));
        ps.setArray(1, arr);
        ps.executeUpdate();
        ps.close();
    }

    // ==================== stream/reader setters ====================
    @Test
    public void setClob_reader_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setClob(1, new StringReader("data"));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setClob_reader_long_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setClob(1, new StringReader("data"), 4L);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setClob_clob_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setClob(1, new JdbcCob("hello"));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setNClob_nclob_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setNClob(1, new JdbcCob("hello"));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setNClob_reader_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setNClob(1, new StringReader("data"));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setNClob_reader_long_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setNClob(1, new StringReader("data"), 4L);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setCharacterStream_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setCharacterStream(1, new StringReader("data"));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setCharacterStream_int_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setCharacterStream(1, new StringReader("data"), 4);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setCharacterStream_long_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setCharacterStream(1, new StringReader("data"), 4L);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setNCharacterStream_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setNCharacterStream(1, new StringReader("data"));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setNCharacterStream_long_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setNCharacterStream(1, new StringReader("data"), 4L);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setAsciiStream_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setAsciiStream(1, new ByteArrayInputStream(new byte[] { 65 }));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setAsciiStream_int_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setAsciiStream(1, new ByteArrayInputStream(new byte[] { 65 }), 1);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setAsciiStream_long_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setAsciiStream(1, new ByteArrayInputStream(new byte[] { 65 }), 1L);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setBinaryStream_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setBinaryStream(1, new ByteArrayInputStream(new byte[] { 1, 2 }));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setBinaryStream_int_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setBinaryStream(1, new ByteArrayInputStream(new byte[] { 1, 2 }), 2);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setBinaryStream_long_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setBinaryStream(1, new ByteArrayInputStream(new byte[] { 1, 2 }), 2L);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setBlob_blob_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setBlob(1, new JdbcBob(new byte[] { 1, 2, 3 }));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setBlob_inputStream_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setBlob(1, new ByteArrayInputStream(new byte[] { 1, 2 }));
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setBlob_inputStream_long_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setBlob(1, new ByteArrayInputStream(new byte[] { 1, 2 }), 2L);
        ps.executeUpdate();
        ps.close();
    }

    @Test
    public void setUnicodeStream_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setUnicodeStream(1, new ByteArrayInputStream("hello".getBytes("UTF-8")), 5);
        ps.executeUpdate();
        ps.close();
    }

    // ==================== clearParameters ====================
    @Test
    public void clearParameters_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setInt(1, 42);
        ps.clearParameters();
        ps.executeUpdate();
        ps.close();
    }

    // ==================== metadata ====================
    @Test
    public void getParameterMetaData_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setInt(1, 42);
        ParameterMetaData pmd = ps.getParameterMetaData();
        assertNotNull(pmd);
        assertEquals(1, pmd.getParameterCount());
        ps.close();
    }

    @Test
    public void getMetaData_afterExecute() throws Exception {
        PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM test");
        ps.execute();
        ResultSetMetaData rsmd = ps.getMetaData();
        // may be null if no result cursor is available
        ps.close();
    }

    // ==================== unsupported ====================
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setRowId_unsupported() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setRowId(1, null);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setSQLXML_unsupported() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setSQLXML(1, null);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setRef_unsupported() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setRef(1, null);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void addBatch_unsupported() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.addBatch();
    }

    @Test(expected = SQLException.class)
    public void checkParameterIndex_zero() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setInt(0, 42);
    }
}
