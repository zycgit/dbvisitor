package net.hasor.dbvisitor.adapter.elastic.realdb.es7;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Elastic7RefreshAndReindexTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://localhost:19201";
    private static final String INDEX_SOURCE = "dbv_reindex_source";
    private static final String INDEX_DEST = "dbv_reindex_dest";

    @Before
    public void setUp() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try { stmt.executeUpdate("DELETE /" + INDEX_SOURCE); } catch (Exception ignored) {}
            try { stmt.executeUpdate("DELETE /" + INDEX_DEST); } catch (Exception ignored) {}

            stmt.executeUpdate("PUT /" + INDEX_SOURCE);
            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_doc/1 { \"name\": \"doc1\" }");
            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_doc/2 { \"name\": \"doc2\" }");
            
            // Refresh source to make docs visible
            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_refresh");
            Thread.sleep(1000);
        }
    }

    @After
    public void tearDown() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try { stmt.executeUpdate("DELETE /" + INDEX_SOURCE); } catch (Exception ignored) {}
            try { stmt.executeUpdate("DELETE /" + INDEX_DEST); } catch (Exception ignored) {}
        }
    }

    @Test
    public void testRefresh() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            // Refresh specific index
            stmt.executeUpdate("POST /" + INDEX_SOURCE + "/_refresh");
            
            // Refresh all (generic)
            stmt.executeUpdate("POST /_refresh");
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

            String reindexBody = "{" +
                    "  \"source\": { \"index\": \"" + INDEX_SOURCE + "\" }," +
                    "  \"dest\": { \"index\": \"" + INDEX_DEST + "\" }" +
                    "}";
            
            // Execute reindex
            int count = stmt.executeUpdate("POST /_reindex " + reindexBody);
            
            // Should be at least 2 docs
            if (count != 2) {
                throw new Exception("Expected 2 reindexed docs, got " + count + ", source count was " + sourceCount);
            }
            
            // Refresh dest
            stmt.executeUpdate("POST /" + INDEX_DEST + "/_refresh");
        }
    }
}
