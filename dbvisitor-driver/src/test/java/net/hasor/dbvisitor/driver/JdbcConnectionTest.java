package net.hasor.dbvisitor.driver;

import java.sql.*;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Comprehensive tests for JdbcConnection — covers all creation methods, transaction, metadata, and unsupported. */
public class JdbcConnectionTest {

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

    // ==================== createStatement ====================
    @Test
    public void createStatement_default() throws Exception {
        Statement stmt = conn.createStatement();
        assertNotNull(stmt);
        stmt.close();
    }

    @Test
    public void createStatement_scrollType() throws Exception {
        Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        assertNotNull(stmt);
        stmt.close();
    }

    @Test
    public void createStatement_holdability() throws Exception {
        Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        assertNotNull(stmt);
        stmt.close();
    }

    @Test(expected = SQLException.class)
    public void createStatement_invalidType() throws Exception {
        conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    @Test(expected = SQLException.class)
    public void createStatement_invalidConcurrency() throws Exception {
        conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    }

    @Test(expected = SQLException.class)
    public void createStatement_invalidHoldability() throws Exception {
        conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
    }

    // ==================== prepareStatement ====================
    @Test
    public void prepareStatement_default() throws Exception {
        PreparedStatement ps = conn.prepareStatement("SELECT 1");
        assertNotNull(ps);
        ps.close();
    }

    @Test
    public void prepareStatement_scrollType() throws Exception {
        PreparedStatement ps = conn.prepareStatement("SELECT 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        assertNotNull(ps);
        ps.close();
    }

    @Test
    public void prepareStatement_holdability() throws Exception {
        PreparedStatement ps = conn.prepareStatement("SELECT 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        assertNotNull(ps);
        ps.close();
    }

    @Test
    public void prepareStatement_generatedKeys() throws Exception {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (1)", Statement.RETURN_GENERATED_KEYS);
        assertNotNull(ps);
        ps.close();
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepareStatement_columnIndexes_unsupported() throws Exception {
        conn.prepareStatement("INSERT INTO test VALUES (1)", new int[] { 1 });
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepareStatement_columnNames_unsupported() throws Exception {
        conn.prepareStatement("INSERT INTO test VALUES (1)", new String[] { "id" });
    }

    // ==================== prepareCall ====================
    @Test
    public void prepareCall_default() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        assertNotNull(cs);
        cs.close();
    }

    @Test
    public void prepareCall_scrollType() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        assertNotNull(cs);
        cs.close();
    }

    @Test
    public void prepareCall_holdability() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        assertNotNull(cs);
        cs.close();
    }

    // ==================== transaction ====================
    @Test
    public void autoCommit_test() throws Exception {
        assertTrue(conn.getAutoCommit());
        conn.setAutoCommit(false);
        assertFalse(conn.getAutoCommit());
        conn.setAutoCommit(true);
    }

    @Test
    public void commit_rollback() throws Exception {
        conn.setAutoCommit(false);
        conn.commit();
        conn.rollback();
        conn.setAutoCommit(true);
    }

    @Test
    public void transactionIsolation_test() throws Exception {
        int level = conn.getTransactionIsolation();
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }

    // ==================== catalog/schema ====================
    @Test
    public void catalog_test() throws Exception {
        assertNotNull(conn.getCatalog());
        conn.setCatalog("mydb");
        assertEquals("mydb", conn.getCatalog());
    }

    @Test
    public void schema_test() throws Exception {
        assertNotNull(conn.getSchema());
        conn.setSchema("myschema");
        assertEquals("myschema", conn.getSchema());
    }

    // ==================== metadata ====================
    @Test
    public void getMetaData_test() throws Exception {
        DatabaseMetaData md = conn.getMetaData();
        assertNotNull(md);
        assertSame(conn, md.getConnection());
    }

    // ==================== LOB creation ====================
    @Test
    public void createClob_test() throws Exception {
        Clob c = conn.createClob();
        assertNotNull(c);
        assertEquals(0, c.length());
    }

    @Test
    public void createNClob_test() throws Exception {
        NClob nc = conn.createNClob();
        assertNotNull(nc);
        assertEquals(0, nc.length());
    }

    @Test
    public void createBlob_test() throws Exception {
        Blob b = conn.createBlob();
        assertNotNull(b);
        assertEquals(0, b.length());
    }

    @Test
    public void createArrayOf_test() throws Exception {
        Array arr = conn.createArrayOf("VARCHAR", new Object[] { "a", "b" });
        assertNotNull(arr);
        assertEquals("VARCHAR", arr.getBaseTypeName());
    }

    // ==================== nativeSQL ====================
    @Test
    public void nativeSQL_test() throws Exception {
        assertEquals("SELECT 1", conn.nativeSQL("SELECT 1"));
    }

    // ==================== readOnly ====================
    @Test
    public void isReadOnly_test() throws Exception {
        assertFalse(conn.isReadOnly());
    }

    @Test
    public void setReadOnly_false() throws Exception {
        conn.setReadOnly(false);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setReadOnly_true_unsupported() throws Exception {
        conn.setReadOnly(true);
    }

    // ==================== holdability ====================
    @Test
    public void holdability_test() throws Exception {
        assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, conn.getHoldability());
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }

    @Test(expected = SQLException.class)
    public void setHoldability_invalid() throws Exception {
        conn.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
    }

    // ==================== warnings ====================
    @Test
    public void warnings_test() throws Exception {
        assertNull(conn.getWarnings());
        conn.clearWarnings();
    }

    // ==================== clientInfo ====================
    @Test
    public void getClientInfo_name() throws Exception {
        assertNull(conn.getClientInfo("some_key"));
    }

    @Test
    public void getClientInfo_all() throws Exception {
        assertNotNull(conn.getClientInfo());
    }

    @Test(expected = SQLClientInfoException.class)
    public void setClientInfo_name_unsupported() throws Exception {
        conn.setClientInfo("key", "value");
    }

    @Test(expected = SQLClientInfoException.class)
    public void setClientInfo_props_unsupported() throws Exception {
        conn.setClientInfo(new Properties());
    }

    // ==================== close ====================
    @Test
    public void close_and_isClosed() throws Exception {
        assertFalse(conn.isClosed());
        conn.close();
        assertTrue(conn.isClosed());
    }

    // ==================== unsupported ====================
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void typeMap_get_unsupported() throws Exception {
        conn.getTypeMap();
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void typeMap_set_unsupported() throws Exception {
        conn.setTypeMap(null);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void createSQLXML_unsupported() throws Exception {
        conn.createSQLXML();
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void createStruct_unsupported() throws Exception {
        conn.createStruct("type", null);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setSavepoint_unsupported() throws Exception {
        conn.setSavepoint();
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setSavepointName_unsupported() throws Exception {
        conn.setSavepoint("sp");
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void rollbackSavepoint_unsupported() throws Exception {
        conn.rollback(null);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void releaseSavepoint_unsupported() throws Exception {
        conn.releaseSavepoint(null);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setNetworkTimeout_unsupported() throws Exception {
        conn.setNetworkTimeout(null, 100);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getNetworkTimeout_unsupported() throws Exception {
        conn.getNetworkTimeout();
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void abort_unsupported() throws Exception {
        conn.abort(null);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void isValid_unsupported() throws Exception {
        conn.isValid(5);
    }

    @Test
    public void unwrap_test() throws Exception {
        try {
            conn.unwrap(Connection.class);
        } catch (Exception ignore) {
        }
        try {
            conn.isWrapperFor(Connection.class);
        } catch (Exception ignore) {
        }
    }
}
