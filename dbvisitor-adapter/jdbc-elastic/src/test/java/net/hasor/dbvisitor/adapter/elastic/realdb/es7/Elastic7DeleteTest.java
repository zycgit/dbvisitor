package net.hasor.dbvisitor.adapter.elastic.realdb.es7;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Elastic7DeleteTest {
    private static final String ES_URL     = "jdbc:dbvisitor:elastic://localhost:19201";
    private static final String INDEX_NAME = "dbv_delete_test_idx";

    @Before
    public void setUp() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("DELETE /" + INDEX_NAME);
            } catch (Exception e) {
                // ignore
            }

            // Create index
            String putIndex = "PUT /" + INDEX_NAME + " {" +       //
                    "\"mappings\": {" +                           //
                    "    \"properties\": {" +                     //
                    "      \"name\": { \"type\": \"keyword\" }," +//
                    "      \"age\": { \"type\": \"integer\" }" +  //
                    "    }" +                                     //
                    "}" +                                         //
                    "}";
            stmt.executeUpdate(putIndex);
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

    @Test
    public void testDeleteDoc() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            // Insert a document
            stmt.executeUpdate("POST /" + INDEX_NAME + "/_doc/1 { \"name\": \"John\", \"age\": 30 }");
            stmt.executeUpdate("POST /" + INDEX_NAME + "/_refresh");

            // Delete the document
            int count = stmt.executeUpdate("DELETE /" + INDEX_NAME + "/_doc/1");
            assertEquals(1, count);

            // Verify delete
            stmt.executeUpdate("POST /" + INDEX_NAME + "/_refresh");
            try (ResultSet rs = stmt.executeQuery("POST /" + INDEX_NAME + "/_search { \"query\": { \"ids\": { \"values\": [\"1\"] } } }")) {
                assertFalse(rs.next());
            }
        }
    }

    @Test
    public void testDeleteByQuery() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            // Insert documents
            stmt.executeUpdate("POST /" + INDEX_NAME + "/_doc/1 { \"name\": \"John\", \"age\": 30 }");
            stmt.executeUpdate("POST /" + INDEX_NAME + "/_doc/2 { \"name\": \"Jane\", \"age\": 25 }");
            stmt.executeUpdate("POST /" + INDEX_NAME + "/_doc/3 { \"name\": \"Bob\", \"age\": 30 }");

            // Refresh
            stmt.executeUpdate("POST /" + INDEX_NAME + "/_refresh");

            // Delete by query
            String deleteByQuery = "POST /" + INDEX_NAME + "/_delete_by_query {" + "\"query\": {" + "  \"term\": {" + "    \"age\": 30" + "  }" + "}" + "}";

            int count = stmt.executeUpdate(deleteByQuery);
            assertEquals(2, count); // John and Bob should be deleted

            // Refresh
            stmt.executeUpdate("POST /" + INDEX_NAME + "/_refresh");

            // Verify
            try (ResultSet rs = stmt.executeQuery("POST /" + INDEX_NAME + "/_search { \"query\": { \"ids\": { \"values\": [\"1\"] } } }")) {
                assertFalse(rs.next());
            }
            try (ResultSet rs = stmt.executeQuery("POST /" + INDEX_NAME + "/_search { \"query\": { \"ids\": { \"values\": [\"3\"] } } }")) {
                assertFalse(rs.next());
            }
            try (ResultSet rs = stmt.executeQuery("POST /" + INDEX_NAME + "/_search { \"query\": { \"ids\": { \"values\": [\"2\"] } } }")) {
                assertTrue(rs.next());
            }
        }
    }
}
