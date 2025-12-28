package net.hasor.dbvisitor.adapter.elastic.realdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ElasticIndexMappingTest {
    private static final String ES_URL       = "jdbc:dbvisitor:elastic://localhost:19200";
    private static final String INDEX_NAME_1 = "dbv_mapping_test_idx_1";
    private static final String INDEX_NAME_2 = "dbv_mapping_test_idx_2";

    @Before
    public void setUp() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL)) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DELETE /" + INDEX_NAME_1);
                } catch (Exception e) {
                    // ignore if not exists
                }
                try {
                    stmt.executeUpdate("DELETE /" + INDEX_NAME_2);
                } catch (Exception e) {
                    // ignore if not exists
                }
            }

            try (Statement stmt = conn.createStatement()) {
                String putIndex1 = "PUT /" + INDEX_NAME_1 + " {" +        //
                        "\"mappings\": {" +                               //
                        "  \"_doc\": {" +                                 //
                        "    \"properties\": {" +                         //
                        "      \"name\": {" +                             //
                        "        \"type\": \"text\"," +                   //
                        "        \"fields\": {" +                         //
                        "          \"raw\": { \"type\": \"keyword\" }," + //
                        "          \"count\": { \"type\": \"token_count\", \"analyzer\": \"standard\" }" + //
                        "        }" +                                     //
                        "      }," +                                      //
                        "      \"age\": { \"type\": \"integer\" }," +     //
                        "      \"address\": {" +                          //
                        "        \"properties\": {" +                     //
                        "          \"city\": { \"type\": \"keyword\" }," +//
                        "          \"zip\": { \"type\": \"keyword\" }" +  //
                        "        }" +                                     //
                        "      }" +                                       //
                        "    }" +                                         //
                        "  }" +                                           //
                        "}" +                                             //
                        "}";                                              //
                stmt.executeUpdate(putIndex1);

                String putIndex2 = "PUT /" + INDEX_NAME_2 + " {" + //
                        "\"mappings\": {" +                        //
                        "  \"_doc\": {" +                          //
                        "    \"properties\": {" +                  //
                        "      \"title\": { \"type\": \"text\" }" +//
                        "    }" +                                  //
                        "  }" +                                    //
                        "}" +                                      //
                        "}";
                stmt.executeUpdate(putIndex2);
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("DELETE /" + INDEX_NAME_1);
            } catch (Exception e) {
                // ignore
            }
            try {
                stmt.executeUpdate("DELETE /" + INDEX_NAME_2);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void testGetIndexMapping() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL)) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("GET /" + INDEX_NAME_1 + "/_mapping")) {

                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("NAME", rs.getString("NAME"));
                    row.put("MAPPING", rs.getString("MAPPING"));
                    row.put("FIELD", rs.getString("FIELD"));
                    row.put("TYPE", rs.getString("TYPE"));
                    row.put("NESTED", rs.getBoolean("NESTED"));
                    rows.add(row);
                }

                boolean foundName = false;
                boolean foundNameRaw = false;
                boolean foundNameCount = false;
                boolean foundAge = false;
                boolean foundAddress = false;

                for (Map<String, Object> row : rows) {
                    String mapping = (String) row.get("MAPPING");
                    String field = (String) row.get("FIELD");
                    String type = (String) row.get("TYPE");
                    boolean nested = (Boolean) row.get("NESTED");

                    if ("name".equals(mapping) && field == null) {
                        foundName = true;
                        assertEquals("text", type);
                    }
                    if ("name".equals(mapping) && "raw".equals(field)) {
                        foundNameRaw = true;
                        assertEquals("keyword", type);
                    }
                    if ("name".equals(mapping) && "count".equals(field)) {
                        foundNameCount = true;
                        assertEquals("token_count", type);
                    }
                    if ("age".equals(mapping) && field == null) {
                        foundAge = true;
                        assertEquals("integer", type);
                    }
                    if ("address".equals(mapping) && field == null) {
                        foundAddress = true;
                        assertTrue(nested); // Should be nested/object
                    }
                }

                assertTrue("Name field not found", foundName);
                assertTrue("Name.raw field not found", foundNameRaw);
                assertTrue("Name.count field not found", foundNameCount);
                assertTrue("Age field not found", foundAge);
                assertTrue("Address field not found", foundAddress);
            }
        }
    }

    @Test
    public void testMultipleGetIndexMapping() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL)) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("GET /" + INDEX_NAME_1 + "," + INDEX_NAME_2 + "/_mapping")) {
                boolean foundIndex1Name = false;
                boolean foundIndex2Title = false;

                while (rs.next()) {
                    String mapping = rs.getString("MAPPING");

                    if ("name".equals(mapping)) {
                        foundIndex1Name = true;
                    }
                    if ("title".equals(mapping)) {
                        foundIndex2Title = true;
                    }
                }

                assertTrue("Should find 'name' field from first index", foundIndex1Name);
                assertTrue("Should find 'title' field from second index", foundIndex2Title);
            }
        }
    }

    @Test
    public void testPostMapping() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            // Add new fields to existing mapping
            String postMapping = "POST /" + INDEX_NAME_1 + "/_mapping/_doc {" +//
                    "\"properties\": {" +//
                    "  \"email\": { \"type\": \"keyword\" }," +//
                    "  \"order_date\": { \"type\": \"date\", \"format\": \"yyyy-MM-dd HH:mm:ss\" }" +//
                    "}" +//
                    "}";

            stmt.executeUpdate(postMapping);

            try (ResultSet rs = stmt.executeQuery("GET /" + INDEX_NAME_1 + "/_mapping")) {
                boolean foundEmail = false;
                boolean foundOrderDate = false;
                boolean foundAge = false;

                while (rs.next()) {
                    String mapping = rs.getString("MAPPING");
                    String type = rs.getString("TYPE");

                    if ("email".equals(mapping)) {
                        assertEquals("keyword", type);
                        foundEmail = true;
                    }
                    if ("order_date".equals(mapping)) {
                        assertEquals("date", type);
                        foundOrderDate = true;
                    }
                    if ("age".equals(mapping)) {
                        assertEquals("integer", type);
                        foundAge = true;
                    }
                }
                assertTrue("Email field should be added", foundEmail);
                assertTrue("OrderDate field should be added", foundOrderDate);
                assertTrue("Age field should still exist", foundAge);
            }
        }
    }

    @Test
    public void testPutMapping() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            // Add new fields to existing mapping using PUT
            String putMapping = "PUT /" + INDEX_NAME_1 + "/_mapping/_doc {" +//
                    "\"properties\": {" +                                    //
                    "  \"phone\": { \"type\": \"keyword\" }" +               //
                    "}" +                                                    //
                    "}";

            stmt.executeUpdate(putMapping);

            try (ResultSet rs = stmt.executeQuery("GET /" + INDEX_NAME_1 + "/_mapping")) {
                boolean foundPhone = false;

                while (rs.next()) {
                    String mapping = rs.getString("MAPPING");
                    String type = rs.getString("TYPE");

                    if ("phone".equals(mapping)) {
                        assertEquals("keyword", type);
                        foundPhone = true;
                    }
                }
                assertTrue("Phone field should be added via PUT", foundPhone);
            }
        }
    }

    @Test
    public void testGetWildcardMapping() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("GET /dbv_*_test_idx_*/_mapping")) {
                boolean found1 = false;
                boolean found2 = false;
                while (rs.next()) {
                    String name = rs.getString("NAME");
                    if (INDEX_NAME_1.equals(name)) {
                        found1 = true;
                    }
                    if (INDEX_NAME_2.equals(name)) {
                        found2 = true;
                    }
                }
                assertTrue("Should find index 1", found1);
                assertTrue("Should find index 2", found2);
            }
        }
    }
}
