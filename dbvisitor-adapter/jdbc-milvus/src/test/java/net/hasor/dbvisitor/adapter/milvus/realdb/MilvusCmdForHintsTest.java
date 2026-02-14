package net.hasor.dbvisitor.adapter.milvus.realdb;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusCmdForHintsTest extends AbstractMilvusCmdForTest {
    @Before
    public void setUp() {
        if (!milvusReady) {
            return;
        }
        dropCollection(TEST_COLLECTION);
    }

    @After
    public void tearDown() {
        if (!milvusReady) {
            return;
        }
        dropCollection(TEST_COLLECTION);
    }

    private void setupData() throws Exception {
        // Always start with a clean collection
        dropCollection(TEST_COLLECTION);
        createCollection(TEST_COLLECTION);

        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            // 1. Insert Data via JDBC
            stmt.executeUpdate("INSERT INTO " + TEST_COLLECTION + " (book_id, word_count, book_intro) VALUES (1, 1000, [0.1, 0.2])");
            stmt.executeUpdate("INSERT INTO " + TEST_COLLECTION + " (book_id, word_count, book_intro) VALUES (2, 2000, [0.3, 0.4])");
            stmt.executeUpdate("INSERT INTO " + TEST_COLLECTION + " (book_id, word_count, book_intro) VALUES (3, 3000, [0.5, 0.6])");

            // 2. Create Index via JDBC
            stmt.executeUpdate("CREATE INDEX idx_book_intro ON " + TEST_COLLECTION + " (book_intro) USING \"IVF_FLAT\" WITH (nlist = 1024, metric_type = \"L2\")");

            // 3. Load Table via JDBC
            try {
                stmt.executeUpdate("LOAD TABLE " + TEST_COLLECTION);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void testHints_overwrite_find_limit() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            setupData();

            try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
                // 1. Test overwrite_find_limit
                // Original SQL says nothing or default limit, Hint forces limit 1
                ResultSet rs = stmt.executeQuery("/*+ overwrite_find_limit=1 */ SELECT * FROM " + TEST_COLLECTION);
                int count = 0;
                while (rs.next()) {
                    count++;
                }
                assertEquals(1, count);
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testHints_overwrite_find_skip() throws Exception {
        if (!milvusReady) {
            return;
        }
        try {
            setupData(); // Inserts: ID 1, 2, 3
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
                // 2. Test overwrite_find_skip
                // book_id > 0 matches all 3. Skip 1 means we should skip matching row (ID 1), get ID 2, 3.
                // Combined with Limit 1 -> Should get 2nd row?
                // Note: Milvus query order without vector search is not guaranteed to be insertion order, 
                // but for simple cases usually works. Here we just rely on count <= 2 (actually 1 because usually only 3 rows total)

                // Let's just query with skip 1, verify we get 2 results (total 3)
                // limit is required in milvus, if not specified adapter uses default.
                // NOTE: default limit is 10, without explicit LIMIT clause in SQL, Milvus adapter will set limit=10 unless changed.
                // It seems Milvus (or our adapter logic) doesn't respect offset correctly if Limit is not explicitly combined in some versions or configs?
                // Let's add limit 10 explicitly in SQL to be sure, although adapter defaults should work.
                ResultSet rs = stmt.executeQuery("/*+ overwrite_find_skip=1 */ SELECT * FROM " + TEST_COLLECTION + " WHERE book_id > 0 LIMIT 10");
                int count = 0;
                while (rs.next()) {
                    count++;
                }
                // With limit defaulting to 10, skip 1 on 3 items returns 2 items [2, 3]
                assertEquals(2, count);
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testHints_overwrite_find_as_count() throws Exception {
        if (!milvusReady) {
            return;
        }
        try {
            setupData();
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
                // 3. Test overwrite_find_as_count
                // Regular Select transformed to Count
                // WHERE book_id > 1 matches ID 2 and 3 (Total 2)
                ResultSet rs = stmt.executeQuery("/*+ overwrite_find_as_count=true */ SELECT * FROM " + TEST_COLLECTION + " WHERE book_id > 1");
                assertTrue(rs.next());
                assertEquals(2, rs.getLong(1)); // Should be count
                assertFalse("Column name should be count or similar", rs.getMetaData().getColumnName(1).isEmpty());
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }
}
