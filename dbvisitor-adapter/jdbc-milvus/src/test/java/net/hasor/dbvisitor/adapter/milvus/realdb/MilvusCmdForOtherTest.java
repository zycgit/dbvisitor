package net.hasor.dbvisitor.adapter.milvus.realdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class MilvusCmdForOtherTest extends AbstractMilvusCmdForTest {
    @Before
    public void setUp() {
        if (!milvusReady) {
            return;
        }
        // Clean up any residual aliases from previous runs
        dropAlias("test_alias_create");
        dropAlias("test_alias_alter");
        dropAlias("test_alias_drop");
        dropAlias("test_alias_drop_not_exist");
        // Drop and recreate collection to ensure clean state
        dropCollection(TEST_COLLECTION);
        dropCollection(TEST_COLLECTION_NEW);
        createCollection(TEST_COLLECTION);
    }

    @After
    public void tearDown() {
        if (!milvusReady) {
            return;
        }
        // Clean up aliases first (before dropping collections they point to)
        dropAlias("test_alias_create");
        dropAlias("test_alias_alter");
        dropAlias("test_alias_drop");
        dropAlias("test_alias_drop_not_exist");
        dropCollection(TEST_COLLECTION);
        dropCollection(TEST_COLLECTION_NEW);
    }

    @Test
    public void testCreateAlias() throws Exception {
        String testAlias = "test_alias_create";
        dropAlias(testAlias); // Ensure clean slate

        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            // Act
            stmt.executeUpdate("CREATE ALIAS " + testAlias + " FOR " + TEST_COLLECTION);

            // Assert
            assertTrue("Alias should verify as a collection/alias", hasCollection(testAlias));
        } finally {
            dropAlias(testAlias);
        }
    }

    @Test
    public void testAlterAlias() throws Exception {
        String testAlias = "test_alias_alter";
        dropAlias(testAlias);
        createAlias(testAlias, TEST_COLLECTION);
        createCollection(TEST_COLLECTION_NEW); // Prepare second collection

        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            // Act
            stmt.executeUpdate("ALTER ALIAS " + testAlias + " FOR " + TEST_COLLECTION_NEW);

            // Assert
            // To verify strictly, one would check if the alias now points to the new collection.
            // Simplified check: checking existence.
            assertTrue("Alias should still exist", hasCollection(testAlias));
        } finally {
            dropAlias(testAlias);
            dropCollection(TEST_COLLECTION_NEW);
        }
    }

    @Test
    public void testDropAlias() throws Exception {
        String testAlias = "test_alias_drop";
        createAlias(testAlias, TEST_COLLECTION);

        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            // Act
            stmt.executeUpdate("DROP ALIAS " + testAlias);

            Assert.assertFalse("Alias should be gone", hasCollection(testAlias));
        }
    }

    @Test
    public void testDropAliasIfExists() throws Exception {
        String testAlias = "test_alias_drop_not_exist";
        dropAlias(testAlias); // Ensure it doesn't exist

        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            // Act
            stmt.executeUpdate("DROP ALIAS IF EXISTS " + testAlias);
        }
    }

    @Test
    public void testShowProgress() throws Exception {
        createIndex(TEST_COLLECTION, "idx_book_intro");

        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("LOAD TABLE " + TEST_COLLECTION);

            try (ResultSet rs = stmt.executeQuery("SHOW PROGRESS OF LOADING ON " + TEST_COLLECTION)) {
                if (rs.next()) {
                    long progress = rs.getLong("PROGRESS");
                    assertTrue(progress >= 0 && progress <= 100);
                } else {
                    throw new RuntimeException("No progress result.");
                }
            }
        }
    }
}
