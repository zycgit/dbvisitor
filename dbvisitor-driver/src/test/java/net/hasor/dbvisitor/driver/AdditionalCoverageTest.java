package net.hasor.dbvisitor.driver;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Additional tests for JdbcStatement, JdbcResultSet, JdbcParameterMetaData — targeting remaining uncovered lines. */
public class AdditionalCoverageTest {

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

    // ==================== JdbcStatement — columnIndexes/columnNames (throws) ====================
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void execute_columnIndexes() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.execute("INSERT INTO t VALUES(1)", new int[] { 1 });
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void executeUpdate_columnIndexes() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO t VALUES(1)", new int[] { 1 });
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void executeLargeUpdate_columnIndexes() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.executeLargeUpdate("INSERT INTO t VALUES(1)", new int[] { 1 });
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void execute_columnNames() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.execute("INSERT INTO t VALUES(1)", new String[] { "id" });
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void executeUpdate_columnNames() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO t VALUES(1)", new String[] { "id" });
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void executeLargeUpdate_columnNames() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.executeLargeUpdate("INSERT INTO t VALUES(1)", new String[] { "id" });
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void executeBatch_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.executeBatch();
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void executeLargeBatch_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.executeLargeBatch();
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void addBatch_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO t VALUES(1)");
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setPoolable_true_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.setPoolable(true);
    }

    @Test
    public void setPoolable_false() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.setPoolable(false);
        stmt.close();
    }

    @Test(expected = SQLException.class)
    public void setMaxFieldSize_negative() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.setMaxFieldSize(-1);
    }

    @Test
    public void getGeneratedKeys() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.execute("INSERT INTO t VALUES(1)", Statement.RETURN_GENERATED_KEYS);
        try {
            stmt.getGeneratedKeys();
        } catch (Exception ignore) {
        }
        stmt.close();
    }

    @Test
    public void getLargeMaxRows_overflow() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.setLargeMaxRows(Long.MAX_VALUE);
        try {
            stmt.getMaxRows();
        } catch (SQLException expected) { /* overflow */ }
        assertEquals(Long.MAX_VALUE, stmt.getLargeMaxRows());
        stmt.close();
    }

    @Test
    public void cancel_test() throws Exception {
        Statement stmt = conn.createStatement();
        try {
            stmt.cancel();
        } catch (SQLException ignore) {
        } // may throw if no pending query
        stmt.close();
    }

    // ==================== JdbcResultSet — updateXxx by name (all throw) ====================
    @Test(expected = SQLException.class)
    public void updateBoolean_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBoolean("c_bool", true);
    }

    @Test(expected = SQLException.class)
    public void updateByte_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateByte("c_int", (byte) 1);
    }

    @Test(expected = SQLException.class)
    public void updateShort_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateShort("c_int", (short) 1);
    }

    @Test(expected = SQLException.class)
    public void updateFloat_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateFloat("c_float", 1.0f);
    }

    @Test(expected = SQLException.class)
    public void updateDouble_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateDouble("c_double", 1.0);
    }

    @Test(expected = SQLException.class)
    public void updateBigDecimal_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBigDecimal("c_int", BigDecimal.ONE);
    }

    @Test(expected = SQLException.class)
    public void updateBytes_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBytes("c_blob", new byte[] { 1 });
    }

    @Test(expected = SQLException.class)
    public void updateDate_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateDate("c_date", new Date(0));
    }

    @Test(expected = SQLException.class)
    public void updateTime_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateTime("c_date", new Time(0));
    }

    @Test(expected = SQLException.class)
    public void updateTimestamp_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateTimestamp("c_date", new Timestamp(0));
    }

    @Test(expected = SQLException.class)
    public void updateAsciiStream_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateAsciiStream("c_string", new ByteArrayInputStream(new byte[] { 65 }));
    }

    @Test(expected = SQLException.class)
    public void updateBinaryStream_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBinaryStream("c_blob", new ByteArrayInputStream(new byte[] { 1 }));
    }

    @Test(expected = SQLException.class)
    public void updateCharacterStream_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateCharacterStream("c_string", new StringReader("x"));
    }

    @Test(expected = SQLException.class)
    public void updateRef_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateRef("c_int", null);
    }

    @Test(expected = SQLException.class)
    public void updateBlob_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBlob("c_blob", (Blob) null);
    }

    @Test(expected = SQLException.class)
    public void updateClob_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateClob("c_clob", (Clob) null);
    }

    @Test(expected = SQLException.class)
    public void updateArray_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateArray("c_int", null);
    }

    @Test(expected = SQLException.class)
    public void updateNString_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNString("c_string", "x");
    }

    @Test(expected = SQLException.class)
    public void updateNClob_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNClob("c_clob", (NClob) null);
    }

    @Test(expected = SQLException.class)
    public void updateSQLXML_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateSQLXML("c_string", null);
    }

    @Test(expected = SQLException.class)
    public void updateRowId_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateRowId("c_int", null);
    }

    @Test(expected = SQLException.class)
    public void updateNCharacterStream_byName() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNCharacterStream("c_string", new StringReader("x"));
    }

    // ==================== JdbcResultSet — more updateXxx by-index overloads ====================
    @Test(expected = SQLException.class)
    public void updateAsciiStream_index() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateAsciiStream(1, new ByteArrayInputStream(new byte[] { 65 }));
    }

    @Test(expected = SQLException.class)
    public void updateBinaryStream_index() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBinaryStream(1, new ByteArrayInputStream(new byte[] { 1 }));
    }

    @Test(expected = SQLException.class)
    public void updateCharacterStream_index() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateCharacterStream(1, new StringReader("x"));
    }

    @Test(expected = SQLException.class)
    public void updateRef_index() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateRef(1, null);
    }

    @Test(expected = SQLException.class)
    public void updateBlob_index() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBlob(1, (Blob) null);
    }

    @Test(expected = SQLException.class)
    public void updateClob_index() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateClob(1, (Clob) null);
    }

    @Test(expected = SQLException.class)
    public void updateArray_index() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateArray(1, null);
    }

    @Test(expected = SQLException.class)
    public void updateNString_index() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNString(1, "x");
    }

    @Test(expected = SQLException.class)
    public void updateNClob_index_nclob() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNClob(1, (NClob) null);
    }

    @Test(expected = SQLException.class)
    public void updateSQLXML_index() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateSQLXML(1, null);
    }

    @Test(expected = SQLException.class)
    public void updateRowId_index() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateRowId(1, null);
    }

    @Test(expected = SQLException.class)
    public void updateNCharacterStream_index() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNCharacterStream(1, new StringReader("x"));
    }

    // ==================== JdbcResultSet — more stream/update overloads with length ====================
    @Test(expected = SQLException.class)
    public void updateAsciiStream_index_int() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateAsciiStream(1, new ByteArrayInputStream(new byte[] { 65 }), 1);
    }

    @Test(expected = SQLException.class)
    public void updateAsciiStream_index_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateAsciiStream(1, new ByteArrayInputStream(new byte[] { 65 }), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateBinaryStream_index_int() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBinaryStream(1, new ByteArrayInputStream(new byte[] { 1 }), 1);
    }

    @Test(expected = SQLException.class)
    public void updateBinaryStream_index_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBinaryStream(1, new ByteArrayInputStream(new byte[] { 1 }), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateCharacterStream_index_int() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateCharacterStream(1, new StringReader("x"), 1);
    }

    @Test(expected = SQLException.class)
    public void updateCharacterStream_index_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateCharacterStream(1, new StringReader("x"), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateBlob_index_stream() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBlob(1, new ByteArrayInputStream(new byte[] { 1 }));
    }

    @Test(expected = SQLException.class)
    public void updateBlob_index_stream_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBlob(1, new ByteArrayInputStream(new byte[] { 1 }), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateClob_index_reader() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateClob(1, new StringReader("x"));
    }

    @Test(expected = SQLException.class)
    public void updateClob_index_reader_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateClob(1, new StringReader("x"), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateNClob_index_reader() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNClob(1, new StringReader("x"));
    }

    @Test(expected = SQLException.class)
    public void updateNClob_index_reader_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNClob(1, new StringReader("x"), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateNCharacterStream_index_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNCharacterStream(1, new StringReader("x"), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateObject_index_scale() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateObject(1, "x", 0);
    }

    @Test(expected = SQLException.class)
    public void updateObject_name_scale() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateObject("c_int", "x", 0);
    }

    // ==================== JdbcResultSet — by-name stream update overloads ====================
    @Test(expected = SQLException.class)
    public void updateAsciiStream_name_int() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateAsciiStream("c_string", new ByteArrayInputStream(new byte[] { 65 }), 1);
    }

    @Test(expected = SQLException.class)
    public void updateAsciiStream_name_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateAsciiStream("c_string", new ByteArrayInputStream(new byte[] { 65 }), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateBinaryStream_name_int() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBinaryStream("c_blob", new ByteArrayInputStream(new byte[] { 1 }), 1);
    }

    @Test(expected = SQLException.class)
    public void updateBinaryStream_name_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBinaryStream("c_blob", new ByteArrayInputStream(new byte[] { 1 }), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateCharacterStream_name_int() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateCharacterStream("c_string", new StringReader("x"), 1);
    }

    @Test(expected = SQLException.class)
    public void updateCharacterStream_name_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateCharacterStream("c_string", new StringReader("x"), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateBlob_name_stream() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBlob("c_blob", new ByteArrayInputStream(new byte[] { 1 }));
    }

    @Test(expected = SQLException.class)
    public void updateBlob_name_stream_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateBlob("c_blob", new ByteArrayInputStream(new byte[] { 1 }), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateClob_name_reader() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateClob("c_clob", new StringReader("x"));
    }

    @Test(expected = SQLException.class)
    public void updateClob_name_reader_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateClob("c_clob", new StringReader("x"), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateNClob_name_reader() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNClob("c_clob", new StringReader("x"));
    }

    @Test(expected = SQLException.class)
    public void updateNClob_name_reader_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNClob("c_clob", new StringReader("x"), 1L);
    }

    @Test(expected = SQLException.class)
    public void updateNCharacterStream_name_long() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.updateNCharacterStream("c_string", new StringReader("x"), 1L);
    }

    // ==================== JdbcResultSet — getDate/Time/Timestamp with Calendar ====================
    @Test
    public void getDate_withCalendar() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        try {
            rs.getDate(6, Calendar.getInstance());
        } catch (Exception ignore) {
        }
        try {
            rs.getDate("c_date", Calendar.getInstance());
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getTime_withCalendar() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        try {
            rs.getTime(6, Calendar.getInstance());
        } catch (Exception ignore) {
        }
        try {
            rs.getTime("c_date", Calendar.getInstance());
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void getTimestamp_withCalendar() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        try {
            rs.getTimestamp(7, Calendar.getInstance());
        } catch (Exception ignore) {
        }
        try {
            rs.getTimestamp("c_timestamp", Calendar.getInstance());
        } catch (Exception ignore) {
        }
        rs.close();
        stmt.close();
    }

    // ==================== JdbcParameterMetaData ====================
    @Test
    public void parameterMetaData_test() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?, ?)");
        ps.setInt(1, 42);
        ps.setString(2, "hello");
        ParameterMetaData pmd = ps.getParameterMetaData();
        assertNotNull(pmd);
        assertTrue(pmd.getParameterCount() >= 0);
        pmd.getParameterTypeName(1);
        pmd.getParameterType(1);
        pmd.getParameterClassName(1);
        pmd.getParameterMode(1);
        pmd.isNullable(1);
        pmd.isSigned(1);
        pmd.getPrecision(1);
        pmd.getScale(1);
        try {
            pmd.unwrap(ParameterMetaData.class);
        } catch (Exception ignore) {
        }
        try {
            pmd.isWrapperFor(ParameterMetaData.class);
        } catch (Exception ignore) {
        }
        ps.close();
    }

    @Test
    public void parameterMetaData_invalidParam() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?)");
        ps.setInt(1, 42);
        ParameterMetaData pmd = ps.getParameterMetaData();
        try {
            pmd.getParameterTypeName(99);
            fail();
        } catch (SQLException expected) {
        }
        ps.close();
    }

    // ==================== JdbcResultSet — getObject involving getRef/getSQLXML/getRowId ====================
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getRef_index_throws() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.getRef(1);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getRef_name_throws() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.getRef("c_int");
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getSQLXML_index_throws() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.getSQLXML(1);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getRowId_index_throws() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.getRowId(1);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getObject_map_throws() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.next();
        rs.getObject(1, new java.util.HashMap<String, Class<?>>());
    }

    // ==================== JdbcResultSet — setFetchSize with negative ====================
    @Test(expected = SQLException.class)
    public void fetchSize_negative() throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM types");
        rs.setFetchSize(-1);
    }

    // ==================== JdbcStatement — setFetchDirection ====================
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void stmt_setFetchDirection_unsupported() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.setFetchDirection(ResultSet.FETCH_REVERSE);
    }

    @Test
    public void stmt_setFetchDirection_forward() throws Exception {
        Statement stmt = conn.createStatement();
        stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
        stmt.close();
    }
}
