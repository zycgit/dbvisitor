package net.hasor.dbvisitor.adapter.milvus.realdb;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MilvusCmdForPartitionTest extends AbstractMilvusCmdForTest {
    private static final String PARTITION = "dbv_partition_p1";

    @Before
    public void setUp() {
        if (!milvusReady) {
            return;
        }
        if (hasCollection(TEST_COLLECTION)) {
            dropCollection(TEST_COLLECTION);
        }
        createCollection(TEST_COLLECTION);
    }

    @After
    public void tearDown() {
        if (!milvusReady) {
            return;
        }
        dropCollection(TEST_COLLECTION);
    }

    @Test
    public void testCreatePartition() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE PARTITION " + PARTITION + " ON TABLE " + TEST_COLLECTION);

            assertTrue("Partition should exist", hasPartition(TEST_COLLECTION, PARTITION));
        }
    }

    @Test
    public void testCreatePartitionIfNotExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE PARTITION IF NOT EXISTS " + PARTITION + " ON TABLE " + TEST_COLLECTION);
            assertTrue(hasPartition(TEST_COLLECTION, PARTITION));

            // Should not fail
            stmt.executeUpdate("CREATE PARTITION IF NOT EXISTS " + PARTITION + " ON TABLE " + TEST_COLLECTION);
            assertTrue(hasPartition(TEST_COLLECTION, PARTITION));
        }
    }

    @Test
    public void testDropPartition() throws Exception {
        createPartition(PARTITION);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            assertTrue(hasPartition(TEST_COLLECTION, PARTITION));

            stmt.executeUpdate("DROP PARTITION " + PARTITION + " ON TABLE " + TEST_COLLECTION);
            assertFalse("Partition should be dropped", hasPartition(TEST_COLLECTION, PARTITION));
        }
    }

    @Test
    public void testDropPartitionIfExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            // Should not fail if not exists
            stmt.executeUpdate("DROP PARTITION IF EXISTS " + PARTITION + " ON TABLE " + TEST_COLLECTION);

            createPartition(PARTITION);
            stmt.executeUpdate("DROP PARTITION IF EXISTS " + PARTITION + " ON TABLE " + TEST_COLLECTION);
            assertFalse(hasPartition(TEST_COLLECTION, PARTITION));
        }
    }

    @Test
    public void testShowPartitions() throws Exception {
        createPartition(PARTITION);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            boolean found = false;
            try (ResultSet rs = stmt.executeQuery("SHOW PARTITIONS FROM " + TEST_COLLECTION)) {
                ResultSetMetaData metaData = rs.getMetaData();
                Set<String> columns = new HashSet<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    columns.add(metaData.getColumnLabel(i));
                }
                assertTrue(columns.contains("PARTITION"));

                while (rs.next()) {
                    if (PARTITION.equals(rs.getString("PARTITION"))) {
                        found = true;
                        break;
                    }
                }
            }
            assertTrue("Should find the test partition in SHOW PARTITIONS", found);
        }
    }

    @Test
    public void testShowSinglePartition() throws Exception {
        createPartition(PARTITION);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            boolean foundSingle = false;
            try (ResultSet rs = stmt.executeQuery("SHOW PARTITION " + PARTITION + " ON TABLE " + TEST_COLLECTION)) {
                while (rs.next()) {
                    if (PARTITION.equals(rs.getString("PARTITION"))) {
                        foundSingle = true;
                        break;
                    }
                }
            }
            assertTrue("Should find the test partition in SHOW PARTITION command", foundSingle);
        }
    }

    private void createPartition(String partitionName) throws Exception {
        if (!hasPartition(TEST_COLLECTION, partitionName)) {
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE PARTITION " + partitionName + " ON TABLE " + TEST_COLLECTION);
            }
        }
    }
}
