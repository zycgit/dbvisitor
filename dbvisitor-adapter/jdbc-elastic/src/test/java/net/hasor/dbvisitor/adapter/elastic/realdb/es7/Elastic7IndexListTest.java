package net.hasor.dbvisitor.adapter.elastic.realdb.es7;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Elastic7IndexListTest {
    private static final String ES_URL     = "jdbc:dbvisitor:elastic://127.0.0.1:19201?indexRefresh=true";
    private static final String INDEX_NAME = "dbv_mapping_test";

    @Before
    public void setUp() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL)) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DELETE /" + INDEX_NAME);
                } catch (Exception e) {
                    // ignore if not exists
                }
            }
            try (Statement stmt = conn.createStatement()) {
                // Create index with a dummy mapping so that GET /_mapping returns rows
                String mapping = "{\"mappings\": {\"properties\": {\"dummy\": {\"type\": \"keyword\"}}}}";
                stmt.executeUpdate("PUT /" + INDEX_NAME + " " + mapping);
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

    //

    @Test
    public void listIndices() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL)) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("GET /_aliases")) {
                List<String> found = new ArrayList<>();
                while (rs.next()) {
                    found.add(rs.getString("NAME"));
                }
                assertTrue(found.contains(INDEX_NAME));
            }
        }
    }

    @Test
    public void aliasIndices() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL)) {
            try (Statement stmt = conn.createStatement()) {
                // 0. Prepare Data
                stmt.executeUpdate("POST /" + INDEX_NAME + "/_doc {\"name\": \"test_alias_data\"}");

                // 1. Add Alias
                String aliasCmd = "POST /_aliases {\"actions\" : [{ \"add\" : { \"index\" : \"" + INDEX_NAME + "\", \"alias\" : \"dbv_es_alias_name\" } }]}";
                int res = stmt.executeUpdate(aliasCmd);
                assertEquals(1, res);

                // 2. Check Alias in List
                try (ResultSet rs = stmt.executeQuery("GET /_aliases")) {
                    List<String> found = new ArrayList<>();
                    while (rs.next()) {
                        String name = rs.getString("NAME");
                        String source = rs.getString("SOURCE");
                        boolean isAlias = rs.getBoolean("ALIASES");

                        if (INDEX_NAME.equals(name)) {
                            // It's the index itself
                            assertTrue(source == null || source.isEmpty());
                            // assertFalse(isAlias); // Depending on implementation, might be false or null->false
                            found.add(name);
                        } else if ("dbv_es_alias_name".equals(name)) {
                            // It's the alias
                            assertEquals(INDEX_NAME, source);
                            assertTrue(isAlias);
                            found.add(name);
                        }
                    }
                    assertTrue(found.contains(INDEX_NAME));
                    assertTrue(found.contains("dbv_es_alias_name"));
                }
            }
        }
    }

    @Test
    public void testIndexOpenClose() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL)) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DELETE /dbv_es_mgmt");
                } catch (Exception e) {
                    // ignore
                }
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("PUT /dbv_es_mgmt");
            }

            // Close Index
            try (Statement stmt = conn.createStatement()) {
                int res = stmt.executeUpdate("POST /dbv_es_mgmt/_close");
                assertEquals(1, res);
            }

            // Open Index
            try (Statement stmt = conn.createStatement()) {
                int res = stmt.executeUpdate("POST /dbv_es_mgmt/_open");
                assertEquals(1, res);
            }

            // cleanup
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE /dbv_es_mgmt");
            }
        }
    }

    @Test
    public void testIndexOpenCloseWithArgs() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL)) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DELETE /dbv_es_mgmt_args");
                } catch (Exception e) {
                    // ignore
                }
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("PUT /dbv_es_mgmt_args");
            }

            // Close Index with placeholder
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement("POST /{?}/_close")) {
                pstmt.setString(1, "dbv_es_mgmt_args");
                int res = pstmt.executeUpdate();
                assertEquals(1, res);
            }

            // Open Index with placeholder
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement("POST /{?}/_open")) {
                pstmt.setString(1, "dbv_es_mgmt_args");
                int res = pstmt.executeUpdate();
                assertEquals(1, res);
            }

            // cleanup
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE /dbv_es_mgmt_args");
            }
        }
    }
}
