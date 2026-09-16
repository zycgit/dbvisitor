/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Elastic6RefreshAndReindexTest {
    private static final String ES_URL       = "jdbc:dbvisitor:elastic://localhost:2920?indexRefresh=true";
    private static final String INDEX_SOURCE = "dbv_reindex_source";
    private static final String INDEX_DEST   = "dbv_reindex_dest";

    @Before
    public void setUp() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("DELETE /" + INDEX_SOURCE);
            } catch (Exception ignored) {
                Elastic6Cleanup.requireMissingIndex(ignored);
            }
            try {
                stmt.executeUpdate("DELETE /" + INDEX_DEST);
            } catch (Exception ignored) {
                Elastic6Cleanup.requireMissingIndex(ignored);
            }

            stmt.executeUpdate("PUT /" + INDEX_SOURCE);
            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_doc/1 { \"name\": \"doc1\" }");
            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_doc/2 { \"name\": \"doc2\" }");

            // Refresh source to make docs visible
            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_refresh");
        }
    }

    @After
    public void tearDown() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("DELETE /" + INDEX_SOURCE);
            } catch (Exception ignored) {
                Elastic6Cleanup.requireMissingIndex(ignored);
            }
            try {
                stmt.executeUpdate("DELETE /" + INDEX_DEST);
            } catch (Exception ignored) {
                Elastic6Cleanup.requireMissingIndex(ignored);
            }
        }
    }

    @Test
    public void testRefresh() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_doc/3?refresh=false {\"name\": \"doc3\"}");
            Assert.assertEquals(1, stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_refresh"));
            try (ResultSet result = stmt.executeQuery("POST /" + INDEX_SOURCE + "/_count")) {
                Assert.assertTrue(result.next());
                Assert.assertEquals(3, result.getInt("COUNT"));
                Assert.assertFalse(result.next());
            }

            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_doc/4?refresh=false {\"name\": \"doc4\"}");
            Assert.assertEquals(1, stmt.executeUpdate("POST /_refresh"));
            try (ResultSet result = stmt.executeQuery("POST /" + INDEX_SOURCE + "/_count")) {
                Assert.assertTrue(result.next());
                Assert.assertEquals(4, result.getInt("COUNT"));
                Assert.assertFalse(result.next());
            }
        }
    }

    @Test
    public void testReindex() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            // Check source count
            long sourceCount = 0;
            try (ResultSet rs = stmt.executeQuery("POST /" + INDEX_SOURCE + "/_count")) {
                Assert.assertTrue(rs.next());
                sourceCount = rs.getLong("COUNT");
                Assert.assertEquals(2, sourceCount);
            }

            String reindexBody = "{" + "  \"source\": { \"index\": \"" + INDEX_SOURCE + "\" }," + "  \"dest\": { \"index\": \"" + INDEX_DEST + "\" }" + "}";

            // Execute reindex
            int count = stmt.executeUpdate("POST /_reindex " + reindexBody);

            // Should be at least 2 docs
            Assert.assertEquals("Expected 2 reindexed docs, source count was " + sourceCount, 2, count);

            // Refresh dest
            stmt.executeUpdate("POST /" + INDEX_DEST + "/_refresh");
            try (ResultSet result = stmt.executeQuery("POST /" + INDEX_DEST + "/_count")) {
                Assert.assertTrue(result.next());
                Assert.assertEquals(sourceCount, result.getLong("COUNT"));
            }
            try (ResultSet result = stmt.executeQuery("POST /" + INDEX_DEST + "/_search {\"sort\": [\"name.keyword\"]}")) {
                Assert.assertTrue(result.next());
                Assert.assertEquals("1", result.getString("_ID"));
                Assert.assertEquals("doc1", result.getString("name"));
                Assert.assertTrue(result.next());
                Assert.assertEquals("2", result.getString("_ID"));
                Assert.assertEquals("doc2", result.getString("name"));
                Assert.assertFalse(result.next());
            }
        }
    }

    public static void main(String[] args) {
        net.hasor.dbvisitor.test.realdb.RealDbTestRunner.run(Elastic6RefreshAndReindexTest.class);
    }
}
