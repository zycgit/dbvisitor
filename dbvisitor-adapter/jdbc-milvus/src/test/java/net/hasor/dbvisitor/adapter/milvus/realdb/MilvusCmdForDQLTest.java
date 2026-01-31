/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.adapter.milvus.realdb;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusCmdForDQLTest extends AbstractMilvusCmdForTest {
    private void setupData() throws Exception {
        if (!hasCollection(TEST_COLLECTION)) {
            createCollection(TEST_COLLECTION);
        }

        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            // 1. Insert Data via JDBC
            stmt.executeUpdate("INSERT INTO " + TEST_COLLECTION + " (book_id, word_count, book_intro) VALUES (1, 1000, [0.1, 0.2])");
            stmt.executeUpdate("INSERT INTO " + TEST_COLLECTION + " (book_id, word_count, book_intro) VALUES (2, 2000, [0.3, 0.4])");
            stmt.executeUpdate("INSERT INTO " + TEST_COLLECTION + " (book_id, word_count, book_intro) VALUES (3, 3000, [0.5, 0.6])");

            // 2. Create Index via JDBC
            stmt.executeUpdate("CREATE INDEX idx_book_intro ON " + TEST_COLLECTION + " (book_intro) USING \"IVF_FLAT\" WITH (nlist = 1024, metric_type = \"L2\")");

            // 3. Load Table via JDBC
            stmt.executeUpdate("LOAD TABLE " + TEST_COLLECTION);
        }
    }

    @Test
    public void testSelectAllAndPagination() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            setupData(); // Inserts 3 rows with book_id 1, 2, 3

            try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
                // 1. Test SELECT *
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + TEST_COLLECTION + " WHERE book_id = 1");
                assertTrue(rs.next());
                assertNotNull(rs.getString("book_id"));
                assertNotNull(rs.getString("word_count"));
                // Vector field might or might not be returned depending on Milvus version/config for wildcards.
                // We just ensure we got the row and scalar fields.

                // 2. Test Select Specific Columns
                rs = stmt.executeQuery("SELECT word_count FROM " + TEST_COLLECTION + " WHERE book_id = 1");
                assertTrue(rs.next());
                assertEquals("1000", rs.getString("word_count"));
                // improve: Verify metadata to ensure only searched columns are returned
                // Note: Milvus might implicitly return the Primary Key (book_id). 
                ResultSetMetaData metaData = rs.getMetaData();
                boolean hasWordCount = false;
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    if ("word_count".equalsIgnoreCase(metaData.getColumnName(i))) {
                        hasWordCount = true;
                    }
                }
                assertTrue("Result should contain 'word_count'", hasWordCount);

                // 3. Test Pagination (Limit + Offset)
                // Limit 2 (Should get 2 rows)
                rs = stmt.executeQuery("SELECT book_id FROM " + TEST_COLLECTION + " WHERE book_id > 0 LIMIT 2");
                int count = 0;
                while (rs.next()) {
                    count++;
                }
                assertTrue("Limit 2 should return 2 rows", count == 2);

                // Limit 1
                rs = stmt.executeQuery("SELECT book_id FROM " + TEST_COLLECTION + " WHERE book_id > 0 LIMIT 1");
                count = 0;
                while (rs.next()) {
                    count++;
                }
                assertEquals("Limit 1 should return 1 row", 1, count);

                // Try Offset 2 Limit 1 (Should be the 3rd item ideally)
                rs = stmt.executeQuery("SELECT book_id FROM " + TEST_COLLECTION + " WHERE book_id > 0 LIMIT 1 OFFSET 2");
                count = 0;
                while (rs.next()) {
                    count++;
                }
                assertEquals("Limit 1 Offset 2 should return 1 row", 1, count);
            }

        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testSelect() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            // 1. Prepare Data using SDK
            setupData();

            // 2. Test JDBC Select
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {

                // Pure Select
                ResultSet rs = stmt.executeQuery("SELECT book_id, word_count FROM " + TEST_COLLECTION + " WHERE book_id = 1");
                assertTrue(rs.next());
                assertEquals("1", rs.getString("book_id"));
                assertEquals("1000", rs.getString("word_count"));
                assertFalse(rs.next());
            }

        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testSearchByFloatList() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            setupData();
            String querySQL = "SELECT book_id FROM " + TEST_COLLECTION + " ORDER BY book_intro <-> ? LIMIT 1";
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); //
                 PreparedStatement ps = conn.prepareStatement(querySQL)) {
                ps.setObject(1, Arrays.asList(0.1f, 0.2f));

                ResultSet searchRs = ps.executeQuery();

                assertTrue(searchRs.next());
                assertEquals("1", searchRs.getString("book_id"));
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testSearchByDoubleList() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            setupData();

            String querySQL = "SELECT book_id FROM " + TEST_COLLECTION + " ORDER BY book_intro <-> ? LIMIT 1";
            try (Connection conn = DriverManager.getConnection(MILVUS_URL);//
                 PreparedStatement ps = conn.prepareStatement(querySQL)) {
                ps.setObject(1, Arrays.asList(0.1d, 0.2d));

                ResultSet searchRs = ps.executeQuery();

                assertTrue(searchRs.next());
                assertEquals("1", searchRs.getString("book_id"));
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testSearchByVectorSearch() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            setupData();

            String querySQL = "SELECT book_id FROM " + TEST_COLLECTION + " ORDER BY book_intro <-> [0.1, 0.2] LIMIT 1";
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); //
                 Statement stmt = conn.createStatement()) {
                ResultSet searchRs = stmt.executeQuery(querySQL);

                assertTrue(searchRs.next());
                assertEquals("1", searchRs.getString("book_id"));
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testSearchByBatch() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            setupData();
            String query = "SELECT book_id FROM " + TEST_COLLECTION + " ORDER BY book_intro <-> ? LIMIT 2";
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); //
                 PreparedStatement ps = conn.prepareStatement(query)) {
                List<List<Float>> batchVectors = Arrays.asList(//
                        Arrays.asList(0.1f, 0.2f), //
                        Arrays.asList(0.11f, 0.21f));

                ps.setObject(1, batchVectors);

                ResultSet searchRs = ps.executeQuery();
                assertTrue(searchRs.next());
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testRangeSearch() throws Exception {
        if (!milvusReady) {
            return;
        }
        try {
            setupData();
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); //
                 Statement stmt = conn.createStatement()) {

                ResultSet rs = stmt.executeQuery("SELECT book_id FROM " + TEST_COLLECTION + " WHERE book_intro <-> [0.1, 0.2] < 0.05");
                assertTrue(rs.next());
                assertEquals("1", rs.getString("book_id"));
                assertFalse(rs.next());

                // Vector distance < 0.001 (should NOT match for distinct vector)
                // Vec in DB: [0.1, 0.2]. Query: [0.0, 0.0]. Dist: ~0.22 > 0.001.
                ResultSet rs2 = stmt.executeQuery("SELECT book_id FROM " + TEST_COLLECTION + " WHERE book_intro <-> [0.0, 0.0] < 0.001");
                assertFalse(rs2.next());
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testMixedSearch() throws Exception {
        if (!milvusReady) {
            return;
        }
        try {
            setupData();
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
                // Scalar Filter + Vector Search
                // Data:
                // 1: wc=1000, vec=[0.1, 0.2]
                // 2: wc=2000, vec=[0.3, 0.4]
                // 3: wc=3000, vec=[0.5, 0.6]

                // Filter excludes the best vector match (ID 1), closest remaining is ID 2
                ResultSet rs = stmt.executeQuery("SELECT book_id FROM " + TEST_COLLECTION + " WHERE word_count > 1500 ORDER BY book_intro <-> [0.1, 0.2] LIMIT 1");
                assertTrue(rs.next());
                assertEquals("2", rs.getString("book_id"));

                // Filter keeps only ID 1, even though we search for vector of ID 3
                ResultSet rs2 = stmt.executeQuery("SELECT book_id FROM " + TEST_COLLECTION + " WHERE word_count = 1000 ORDER BY book_intro <-> [0.5, 0.6] LIMIT 1");
                assertTrue(rs2.next());
                assertEquals("1", rs2.getString("book_id"));
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testComplexNestedQuery() throws Exception {
        if (!milvusReady) {
            return;
        }
        try {
            setupData();

            // Setup:
            // 1: wc=1000, vec=[0.1, 0.2]
            // 2: wc=2000, vec=[0.3, 0.4]
            // 3: wc=3000, vec=[0.5, 0.6]

            try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
                // Query A: Nested Boolean + Vector Range (AND at end)
                // Filter: ( (word_count > 1000 && word_count < 3000) OR book_id = 3 )
                // ID 2: wc=2000. (T && T) -> T. OR .. -> T.
                // ID 3: wc=3000. (T && F) -> F. OR T -> T.
                // Vector: [0.3, 0.4] (Target is ID 2).
                // Distance to ID 2 ([0.3, 0.4]): 0.
                // Distance to ID 3 ([0.5, 0.6]): (0.2^2 + 0.2^2) = 0.08.
                // We use radius 0.05 to exclude ID 3.
                String sql1 = "SELECT book_id FROM " + TEST_COLLECTION +//
                        " WHERE ( (word_count > 1000 AND word_count < 3000) OR book_id = 3 ) " +//
                        " AND book_intro <-> [0.3, 0.4] < 0.05";
                ResultSet rs1 = stmt.executeQuery(sql1);
                assertTrue(rs1.next());
                assertEquals("2", rs1.getString("book_id"));
                assertFalse(rs1.next());

                // Query B: Nested Logical Scalar AND Nested Vector
                // Filter: (book_id = 1 OR book_id = 99)
                // Vector: [0.1, 0.2] (Target ID 1). Radius 0.1.
                // Row 1 matches both.
                String sql2 = "SELECT book_id FROM " + TEST_COLLECTION + //
                        " WHERE (book_id = 1 OR book_id = 99) AND (book_intro <-> [0.1, 0.2] < 0.1)";
                ResultSet rs2 = stmt.executeQuery(sql2);
                assertTrue(rs2.next());
                assertEquals("1", rs2.getString("book_id"));
                assertFalse(rs2.next());
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testCount() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            setupData(); // Inserts 3 rows with book_id 1, 2, 3

            try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
                // 1. Test Count All
                ResultSet rs = stmt.executeQuery("count from " + TEST_COLLECTION);
                assertTrue(rs.next());
                assertEquals(3, rs.getLong(1));  // By Index
                assertEquals(3, rs.getLong("COUNT")); // By Name

                // 2. Test Count with WHERE
                rs = stmt.executeQuery("count from " + TEST_COLLECTION + " where book_id > 1");
                assertTrue(rs.next());
                assertEquals(2, rs.getLong(1));
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }
}
