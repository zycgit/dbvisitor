/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Elastic7RefreshAndReindexTest {
    private static final String ES_URL       = "jdbc:dbvisitor:elastic://127.0.0.1:2921?indexRefresh=true";
    private static final String INDEX_SOURCE = "dbv_reindex_source";
    private static final String INDEX_DEST   = "dbv_reindex_dest";

    @Before
    public void setUp() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("DELETE /" + INDEX_SOURCE);
            } catch (Exception ignored) {
                Elastic7Cleanup.requireMissingIndex(ignored);
            }
            try {
                stmt.executeUpdate("DELETE /" + INDEX_DEST);
            } catch (Exception ignored) {
                Elastic7Cleanup.requireMissingIndex(ignored);
            }

            stmt.executeUpdate("PUT /" + INDEX_SOURCE);
            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_doc/1 { \"name\": \"doc1\" }");
            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_doc/2 { \"name\": \"doc2\" }");
        }
    }

    @After
    public void tearDown() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("DELETE /" + INDEX_SOURCE);
            } catch (Exception ignored) {
                Elastic7Cleanup.requireMissingIndex(ignored);
            }
            try {
                stmt.executeUpdate("DELETE /" + INDEX_DEST);
            } catch (Exception ignored) {
                Elastic7Cleanup.requireMissingIndex(ignored);
            }
        }
    }

    @Test
    public void testRefresh() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL.replace("indexRefresh=true", "indexRefresh=false")); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_doc/3 { \"name\": \"doc3\" }");
            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_refresh");
            assertCount(stmt, INDEX_SOURCE, 3);

            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_doc/4 { \"name\": \"doc4\" }");
            stmt.executeUpdate("POST /_refresh");
            assertCount(stmt, INDEX_SOURCE, 4);
        }
    }

    @Test
    public void testReindex() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            // Check source count
            long sourceCount = 0;
            try (ResultSet rs = stmt.executeQuery("POST /" + INDEX_SOURCE + "/_count")) {
                if (rs.next()) {
                    sourceCount = rs.getLong("COUNT");
                }
            }

            String reindexBody = "{" + "  \"source\": { \"index\": \"" + INDEX_SOURCE + "\" }," + "  \"dest\": { \"index\": \"" + INDEX_DEST + "\" }" + "}";

            // Execute reindex
            int count = stmt.executeUpdate("POST /_reindex " + reindexBody);

            // Should be at least 2 docs
            Assert.assertEquals("Expected 2 reindexed docs, source count was " + sourceCount, 2, count);
            Assert.assertEquals(2, sourceCount);
            stmt.executeUpdate("POST /" + INDEX_DEST + "/_refresh");
            assertCount(stmt, INDEX_DEST, 2);
            for (int id = 1; id <= 2; id++) {
                try (ResultSet rs = stmt.executeQuery("POST /" + INDEX_DEST + "/_search {\"_source\": [\"name\"],\"query\": {\"ids\": {\"values\": [\"" + id + "\"]}}}")) {
                    Assert.assertTrue(rs.next());
                    Assert.assertEquals("doc" + id, rs.getString("name"));
                    Assert.assertFalse(rs.next());
                }
            }
        }
    }

    private void assertCount(Statement stmt, String index, long expected) throws Exception {
        try (ResultSet rs = stmt.executeQuery("POST /" + index + "/_count")) {
            Assert.assertTrue(rs.next());
            Assert.assertEquals(expected, rs.getLong("COUNT"));
            Assert.assertFalse(rs.next());
        }
    }
    public static void main(String[] args) {
        net.hasor.dbvisitor.test.realdb.RealDbTestRunner.run(Elastic7RefreshAndReindexTest.class);
    }
}
