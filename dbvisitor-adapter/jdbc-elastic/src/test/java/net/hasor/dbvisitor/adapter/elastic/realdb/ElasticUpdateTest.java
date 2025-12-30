package net.hasor.dbvisitor.adapter.elastic.realdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ElasticUpdateTest {
    private static final String ES_URL     = "jdbc:dbvisitor:elastic://localhost:19200";
    private static final String INDEX_NAME = "dbv_crud_test_idx";

    @Before
    public void setUp() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("DELETE /" + INDEX_NAME);
            } catch (Exception e) {
                // ignore
            }

            // Create index with mapping
            String putIndex_v6 = "PUT /" + INDEX_NAME + " {" +    //
                    "\"mappings\": {" +                           //
                    "  \"_doc\": {" +                             //
                    "    \"properties\": {" +                     //
                    "      \"name\": { \"type\": \"keyword\" }," +//
                    "      \"age\": { \"type\": \"integer\" }" +  //
                    "    }" +                                     //
                    "  }" +                                       //
                    "}" +                                         //
                    "}";
            String putIndex_v7 = "PUT /" + INDEX_NAME + " {" +    //
                    "\"mappings\": {" +                           //
                    "    \"properties\": {" +                     //
                    "      \"name\": { \"type\": \"keyword\" }," +//
                    "      \"age\": { \"type\": \"integer\" }" +  //
                    "    }" +                                     //
                    "}" +                                         //
                    "}";
            try {
                stmt.executeUpdate(putIndex_v6);
            } catch (Exception e) {
                stmt.executeUpdate(putIndex_v7);
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

    @Test
    public void testUpdateDoc() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            // Insert a document
            stmt.executeUpdate("POST /" + INDEX_NAME + "/_doc/1 { \"name\": \"John\", \"age\": 30 }");

            // Update the document
            int count = stmt.executeUpdate("POST /" + INDEX_NAME + "/_update/1 { \"doc\": { \"age\": 31 } }");
            assertEquals(1, count);

            // Verify update
            stmt.executeUpdate("POST /" + INDEX_NAME + "/_refresh");
            try (ResultSet rs = stmt.executeQuery("POST /" + INDEX_NAME + "/_search { \"query\": { \"ids\": { \"values\": [\"1\"] } } }")) {
                assertTrue(rs.next());
                assertEquals(31, rs.getInt("age"));
            }

            // Noop update
            count = stmt.executeUpdate("POST /" + INDEX_NAME + "/_update/1 { \"doc\": { \"age\": 31 } }");
            // In some ES versions/configs, noop might still return result='updated' if not detected?
            // But usually it returns 'noop'.
            // If it fails here, it means count is 1.
            if (count != 0) {
                System.err.println("Noop update returned count: " + count);
            }
            assertEquals(0, count);
        }
    }

    @Test
    public void testUpdateByQuery() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            // Insert documents
            assertEquals(1, stmt.executeUpdate("POST /" + INDEX_NAME + "/_doc/1 { \"name\": \"John\", \"age\": 30 }"));
            assertEquals(1, stmt.executeUpdate("POST /" + INDEX_NAME + "/_doc/2 { \"name\": \"Jane\", \"age\": 25 }"));
            assertEquals(1, stmt.executeUpdate("POST /" + INDEX_NAME + "/_doc/3 { \"name\": \"Bob\", \"age\": 30 }"));

            // Refresh to make documents searchable
            stmt.executeUpdate("POST /" + INDEX_NAME + "/_refresh");

            // Update by query
            String updateByQuery = "POST /" + INDEX_NAME + "/_update_by_query {" + "\"script\": {" + "  \"source\": \"ctx._source.age++\"" + "}," + "\"query\": {" + "  \"term\": {" + "    \"age\": 30" + "  }" + "}" + "}";

            int count = stmt.executeUpdate(updateByQuery);
            if (count != 2) {
                System.err.println("UpdateByQuery returned count: " + count);
            }
            assertEquals(2, count); // John and Bob should be updated

            // Refresh
            stmt.executeUpdate("POST /" + INDEX_NAME + "/_refresh");

            // Verify
            try (ResultSet rs = stmt.executeQuery("POST /" + INDEX_NAME + "/_search { \"query\": { \"ids\": { \"values\": [\"1\"] } } }")) {
                assertTrue(rs.next());
                assertEquals(31, rs.getInt("age"));
            }
            try (ResultSet rs = stmt.executeQuery("POST /" + INDEX_NAME + "/_search { \"query\": { \"ids\": { \"values\": [\"3\"] } } }")) {
                assertTrue(rs.next());
                assertEquals(31, rs.getInt("age"));
            }
        }
    }
}
