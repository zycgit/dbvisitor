package net.hasor.dbvisitor.adapter.milvus.realdb;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MilvusCmdForDBTest extends AbstractMilvusCmdForTest {
    @Before
    public void setUp() {
        if (!milvusReady) {
            return;
        }
        if (hasDatabase(TEST_DATABASE)) {
            dropDatabase(TEST_DATABASE);
        }
    }

    @After
    public void tearDown() {
        if (!milvusReady) {
            return;
        }
        if (hasDatabase(TEST_DATABASE)) {
            dropDatabase(TEST_DATABASE);
        }
    }

    @Test
    public void testCreateDatabase() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE " + TEST_DATABASE);
            assertTrue("Database should exist after CREATE", hasDatabase(TEST_DATABASE));
        }
    }

    @Test
    public void testAlterDatabase() throws Exception {
        createDatabase(TEST_DATABASE);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER DATABASE " + TEST_DATABASE + " SET PROPERTIES (\"replica.number\" = 1)");
            assertTrue("Database should still exist after ALTER", hasDatabase(TEST_DATABASE));
        }
    }

    @Test
    public void testDropDatabase() throws Exception {
        createDatabase(TEST_DATABASE);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP DATABASE " + TEST_DATABASE);
            assertFalse("Database should not exist after DROP", hasDatabase(TEST_DATABASE));
        }
    }

    @Test
    public void testCreateDatabaseIfNotExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + TEST_DATABASE);
            assertTrue(hasDatabase(TEST_DATABASE));

            // Should not fail if exists
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + TEST_DATABASE);
            assertTrue(hasDatabase(TEST_DATABASE));
        }
    }

    @Test
    public void testDropDatabaseIfExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            // Should not fail if not exists
            stmt.executeUpdate("DROP DATABASE IF EXISTS " + TEST_DATABASE);

            createDatabase(TEST_DATABASE);
            stmt.executeUpdate("DROP DATABASE IF EXISTS " + TEST_DATABASE);
            assertFalse(hasDatabase(TEST_DATABASE));
        }
    }

    @Test
    public void testShowDatabases() throws Exception {
        createDatabase(TEST_DATABASE);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            boolean found = false;
            try (ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
                ResultSetMetaData metaData = rs.getMetaData();
                Set<String> columns = new HashSet<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    columns.add(metaData.getColumnLabel(i));
                }
                assertTrue(columns.contains("DATABASE"));

                while (rs.next()) {
                    String dbName = rs.getString("DATABASE");
                    if (TEST_DATABASE.equals(dbName)) {
                        found = true;
                        break;
                    }
                }
            }

            assertTrue("Should find the test database in SHOW DATABASES", found);
        }
    }
}
