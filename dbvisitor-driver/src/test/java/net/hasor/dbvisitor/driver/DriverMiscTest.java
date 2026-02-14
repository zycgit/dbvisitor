package net.hasor.dbvisitor.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Tests for JdbcDriver, AdapterManager, JdbcArray, and other small utility classes. */
public class DriverMiscTest {

    @Before
    public void loadDriver() throws Exception {
        Class.forName("net.hasor.dbvisitor.driver.JdbcDriver");
    }

    // ==================== JdbcDriver ====================
    @Test
    public void driver_acceptsURL() throws Exception {
        JdbcDriver driver = new JdbcDriver();
        assertTrue(driver.acceptsURL("jdbc:dbvisitor:mock://host"));
        assertFalse(driver.acceptsURL("jdbc:mysql://host"));
    }

    @Test
    public void driver_connect_valid() throws Exception {
        JdbcDriver driver = new JdbcDriver();
        Properties props = new Properties();
        Connection conn = driver.connect("jdbc:dbvisitor:mock://localhost", props);
        assertNotNull(conn);
        conn.close();
    }

    @Test
    public void driver_connect_invalidUrl() throws Exception {
        JdbcDriver driver = new JdbcDriver();
        Connection conn = driver.connect("jdbc:mysql://localhost", new Properties());
        assertNull(conn);
    }

    @Test
    public void driver_version() throws Exception {
        JdbcDriver driver = new JdbcDriver();
        assertEquals(JdbcDriver.VERSION_MAJOR, driver.getMajorVersion());
        assertEquals(JdbcDriver.VERSION_MINOR, driver.getMinorVersion());
        assertFalse(driver.jdbcCompliant());
        assertNotNull(driver.getParentLogger());
    }

    @Test
    public void driver_getPropertyInfo() throws Exception {
        JdbcDriver driver = new JdbcDriver();
        DriverPropertyInfo[] info = driver.getPropertyInfo("jdbc:dbvisitor:mock://localhost", new Properties());
        assertNotNull(info);
    }

    @Test
    public void driver_parseURL() {
        Properties props = JdbcDriver.parseURL("jdbc:dbvisitor:mock://localhost:1234/db?user=admin&password=pass", new Properties());
        assertNotNull(props);
        assertEquals("mock", props.getProperty(JdbcDriver.P_ADAPTER_NAME));
    }

    @Test
    public void driver_parseURL_withExistingProps() {
        Properties input = new Properties();
        input.setProperty("extra", "val");
        Properties props = JdbcDriver.parseURL("jdbc:dbvisitor:mock://localhost", input);
        assertNotNull(props);
        assertEquals("val", props.getProperty("extra"));
    }

    @Test
    public void driver_constants() {
        assertEquals("jdbc:dbvisitor:", JdbcDriver.START_URL);
        assertEquals("dbVisitor JDBC Adapter", JdbcDriver.NAME);
        assertEquals(4, JdbcDriver.JDBC_MAJOR);
        assertEquals(2, JdbcDriver.JDBC_MINOR);
    }

    // ==================== JdbcArray ====================
    @Test
    public void array_getBaseTypeName() throws Exception {
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        JdbcConnection conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);

        JdbcArray arr = new JdbcArray(conn, "VARCHAR", Arrays.asList("a", "b", "c"));
        assertEquals("VARCHAR", arr.getBaseTypeName());
        arr.getBaseType(); // exercises TypeSupport

        Object data = arr.getArray();
        assertNotNull(data);

        // subList
        Object sub = arr.getArray(1, 2);
        assertNotNull(sub);

        arr.free();
        conn.close();
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void array_getArray_map_unsupported() throws Exception {
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        JdbcConnection conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);
        JdbcArray arr = new JdbcArray(conn, "VARCHAR", Arrays.asList("a"));
        arr.getArray(new java.util.HashMap<String, Class<?>>());
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void array_getArray_subMap_unsupported() throws Exception {
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        JdbcConnection conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);
        JdbcArray arr = new JdbcArray(conn, "VARCHAR", Arrays.asList("a"));
        arr.getArray(1, 1, new java.util.HashMap<String, Class<?>>());
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void array_getResultSet_unsupported() throws Exception {
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        JdbcConnection conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);
        JdbcArray arr = new JdbcArray(conn, "VARCHAR", Arrays.asList("a"));
        arr.getResultSet();
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void array_getResultSet_range_unsupported() throws Exception {
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        JdbcConnection conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);
        JdbcArray arr = new JdbcArray(conn, "VARCHAR", Arrays.asList("a"));
        arr.getResultSet(1, 1);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void array_getResultSet_map_unsupported() throws Exception {
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        JdbcConnection conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);
        JdbcArray arr = new JdbcArray(conn, "VARCHAR", Arrays.asList("a"));
        arr.getResultSet(new java.util.HashMap<String, Class<?>>());
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void array_getResultSet_rangeMap_unsupported() throws Exception {
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        JdbcConnection conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);
        JdbcArray arr = new JdbcArray(conn, "VARCHAR", Arrays.asList("a"));
        arr.getResultSet(1, 1, new java.util.HashMap<String, Class<?>>());
    }

    // ==================== DriverManager integration ====================
    @Test
    public void driverManager_getConnection() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:dbvisitor:mock://localhost");
        assertNotNull(conn);
        assertFalse(conn.isClosed());
        conn.close();
        assertTrue(conn.isClosed());
    }

    // ==================== AdapterManager ====================
    @Test
    public void adapterManager_lookup() throws Exception {
        AdapterFactory factory = AdapterManager.lookup("mock", null);
        assertNotNull(factory);
        assertEquals("mock", factory.getAdapterName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void adapterManager_lookup_unknown() throws Exception {
        AdapterManager.lookup("nonexistent_adapter_xyz", null);
    }
}
