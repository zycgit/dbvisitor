package net.hasor.dbvisitor.driver;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

import static org.junit.Assert.*;
import java.util.Properties;

public class JdbcResultSetTest {

    private static class MockAdapterCursor implements AdapterCursor {
        private final Object returnValue;
        
        public MockAdapterCursor(Object returnValue) {
            this.returnValue = returnValue;
        }

        @Override public List<JdbcColumn> columns() { return Collections.singletonList(new JdbcColumn("col", "ARRAY", "", "", "")); }
        @Override public boolean next() throws SQLException { return true; }
        @Override public Object column(int column) throws IOException, SQLException { return returnValue; }
        @Override public int batchSize() { return 0; }
        @Override public void close() throws IOException { }
        @Override public List<String> warnings() { return Collections.emptyList(); }
        @Override public void clearWarnings() { }
        @Override public boolean isPending() { return false; }
        @Override public boolean isClose() { return false; }
    }
    
    // Mock AdapterFactory, AdapterConnection, TypeSupport
    private static class MockAdapterFactory implements AdapterFactory {
        @Override public String getAdapterName() { return "mock"; }
        @Override public String[] getPropertyNames() { return new String[0]; }
        @Override public TypeSupport createTypeSupport(Properties properties) { return new MockTypeSupport(); }
        @Override public AdapterConnection createConnection(Connection owner, String jdbcUrl, Properties properties) throws SQLException {
            return new MockAdapterConnection(jdbcUrl);
        }
    }
    
    private static class MockTypeSupport implements TypeSupport {
        @Override public String getTypeName(int typeNumber) { return "VARCHAR"; }
        @Override public String getTypeClassName(String typeName) { return "java.lang.String"; }
        @Override public String getTypeName(Class<?> classType) { return "VARCHAR"; }
        @Override public int getTypeNumber(String typeName) { return Types.VARCHAR; }
        @Override public TypeConvert findConvert(String typeName, Class<?> toType) { return null; }
    }
    
    // Abstract class, so extend
    private static class MockAdapterConnection extends AdapterConnection {
        public MockAdapterConnection(String jdbcUrl) {
            super(jdbcUrl, "user");
        }
        
        @Override public void setCatalog(String catalog) throws SQLException {}
        @Override public String getCatalog() throws SQLException { return "catalog"; }
        @Override public void setSchema(String schema) throws SQLException {}
        @Override public String getSchema() throws SQLException { return "schema"; }
        @Override public AdapterRequest newRequest(String sql) { return null; }
        @Override public void doRequest(AdapterRequest request, AdapterReceive receive) throws SQLException {}
        @Override public void cancelRequest() {}
        @Override protected void doClose() throws IOException {}
    }

    private static Map<String, AdapterFactory> originalFactoryMap;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setup() throws Exception {
        // Inject MockAdapterFactory
        Field field = AdapterManager.class.getDeclaredField("factoryMap");
        field.setAccessible(true);
        Map<String, AdapterFactory> map = (Map<String, AdapterFactory>) field.get(null);
        originalFactoryMap = new HashMap<>(map);
        map.put("mock", new MockAdapterFactory());
    }

    @AfterClass
    @SuppressWarnings("unchecked")
    public static void teardown() throws Exception {
        Field field = AdapterManager.class.getDeclaredField("factoryMap");
        field.setAccessible(true);
        Map<String, AdapterFactory> map = (Map<String, AdapterFactory>) field.get(null);
        map.clear();
        map.putAll(originalFactoryMap);
    }

    private JdbcResultSet createResultSet(Object data) throws SQLException {
        AdapterCursor cursor = new MockAdapterCursor(data);
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        
        JdbcConnection conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);
        JdbcStatement statement = new JdbcStatement(conn);
        return new JdbcResultSet(statement, cursor);
    }

    @Test
    public void testGetArray_withList() throws SQLException {
        List<String> data = Arrays.asList("a", "b", "c");
        JdbcResultSet rs = createResultSet(data);
        rs.next();
        
        Array array = rs.getArray(1);
        assertNotNull(array);
        assertTrue(array instanceof JdbcArray);
        
        Object arrayData = array.getArray();
        assertTrue(arrayData instanceof List);
        Object[] actual = ((List<?>) arrayData).toArray();

        assertEquals(3, actual.length);
        assertEquals("a", actual[0]);
        assertEquals("b", actual[1]);
        assertEquals("c", actual[2]);
    }
    
    @Test
    public void testGetArray_withSet() throws SQLException {
        // Test generic Collection
        Set<String> data = new LinkedHashSet<>(Arrays.asList("x", "y"));
        JdbcResultSet rs = createResultSet(data);
        rs.next();
        
        Array array = rs.getArray(1);
        assertNotNull(array);
        
        Object arrayData = array.getArray();
        assertTrue(arrayData instanceof List);
        Object[] actual = ((List<?>) arrayData).toArray();

        assertEquals(2, actual.length);
        assertEquals("x", actual[0]);
        assertEquals("y", actual[1]);
    }
    
    @Test
    public void testGetArray_withArray() throws SQLException {
        String[] data = new String[]{"x", "y"};
        JdbcResultSet rs = createResultSet(data);
        rs.next();
        
        Array array = rs.getArray(1);
        assertNotNull(array);
        
        Object arrayData = array.getArray();
        assertTrue(arrayData instanceof List);
        Object[] actual = ((List<?>) arrayData).toArray();

        assertEquals(2, actual.length);
        assertEquals("x", actual[0]);
        assertEquals("y", actual[1]);
    }
    
    @Test
    public void testGetArray_withPrimitiveArray() throws SQLException {
        int[] data = new int[]{1, 2, 3};
        JdbcResultSet rs = createResultSet(data);
        rs.next();
        
        Array array = rs.getArray("col");
        assertNotNull(array);
        
        Object arrayData = array.getArray();
        assertTrue(arrayData instanceof List);
        Object[] actual = ((List<?>) arrayData).toArray();

        // Note: primitives are wrapped
        assertEquals(3, actual.length);
        assertEquals(1, actual[0]);
        assertEquals(2, actual[1]);
        assertEquals(3, actual[2]);
    }

    @Test
    public void testGetArray_withSqlArray() throws SQLException {
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        JdbcConnection conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);
        
        Array mockArray = new JdbcArray(conn, "VARCHAR", Arrays.asList("1", "2"));
        JdbcResultSet rs = createResultSet(mockArray);
        rs.next();
        
        Array array = rs.getArray(1);
        assertSame(mockArray, array);
    }

    @Test
    public void testGetArray_null() throws SQLException {
        JdbcResultSet rs = createResultSet(null);
        rs.next();
        assertNull(rs.getArray(1));
    }
}
