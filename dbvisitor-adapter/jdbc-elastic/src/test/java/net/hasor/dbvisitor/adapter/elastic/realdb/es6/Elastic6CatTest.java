package net.hasor.dbvisitor.adapter.elastic.realdb.es6;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Elastic6CatTest {
    private static final String ES_URL     = "jdbc:dbvisitor:elastic://localhost:19200?indexRefresh=true";
    private static final String INDEX_NAME = "dbv_test_idx";

    @Before
    public void setUp() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL)) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DELETE /" + INDEX_NAME);
                } catch (Exception e) {
                    // ignore if not exists
                }
                stmt.executeUpdate("PUT /" + INDEX_NAME);
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("DELETE /" + INDEX_NAME);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    //

    @Test
    public void testCatIndices() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("GET /_cat/indices")) {
                boolean found = false;
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                assertTrue(columnCount > 0);

                // Check if columns include 'index', 'health', 'status' etc.
                Set<String> columns = new HashSet<>();
                for (int i = 1; i <= columnCount; i++) {
                    columns.add(metaData.getColumnLabel(i));
                }
                assertTrue(columns.contains("INDEX"));
                assertTrue(columns.contains("HEALTH"));
                assertTrue(columns.contains("STATUS"));

                while (rs.next()) {
                    String index = rs.getString("INDEX");
                    if (INDEX_NAME.equals(index)) {
                        found = true;
                        assertNotNull(rs.getString("HEALTH"));
                        assertNotNull(rs.getString("STATUS"));
                    }
                }
                assertTrue("Should find the test index in _cat/indices", found);
            }
        }
    }

    @Test
    public void testCatNodes() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("GET /_cat/nodes")) {
                assertTrue(rs.next());
                ResultSetMetaData metaData = rs.getMetaData();
                Set<String> columns = new HashSet<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    columns.add(metaData.getColumnLabel(i));
                }

                assertTrue(columns.contains("IP"));
                assertTrue(columns.contains("HEAP.PERCENT"));
                assertTrue(columns.contains("RAM.PERCENT"));
                assertTrue(columns.contains("CPU"));
                assertTrue(columns.contains("NAME"));

                assertNotNull(rs.getString("IP"));
                assertNotNull(rs.getString("NAME"));
            }
        }
    }

    @Test
    public void testCatHealth() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("GET /_cat/health")) {
                assertTrue(rs.next());
                ResultSetMetaData metaData = rs.getMetaData();
                Set<String> columns = new HashSet<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    columns.add(metaData.getColumnLabel(i));
                }
                assertTrue(columns.contains("STATUS"));
                assertTrue(columns.contains("NODE.TOTAL"));
                assertTrue(columns.contains("CLUSTER"));
                assertTrue(columns.contains("SHARDS"));

                assertNotNull(rs.getString("STATUS"));
                assertNotNull(rs.getString("CLUSTER"));
            }
        }
    }
}
