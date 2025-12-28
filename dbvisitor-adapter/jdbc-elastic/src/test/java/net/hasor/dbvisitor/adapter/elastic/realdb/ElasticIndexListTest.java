package net.hasor.dbvisitor.adapter.elastic.realdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ElasticIndexListTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://localhost:19200";

    @Test
    public void listIndices() throws Exception {
        Properties props = new Properties();

        try (Connection conn = DriverManager.getConnection(ES_URL, props)) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DELETE /dbv_es_idx");
                } catch (Exception e) {
                    // ignore
                } finally {
                    stmt.executeUpdate("PUT /dbv_es_idx");
                }
            }

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("GET /_aliases")) {
                List<String> found = new ArrayList<>();
                while (rs.next()) {
                    found.add(rs.getString("NAME"));
                }
                assertTrue(found.contains("dbv_es_idx"));
            }

            // cleanup
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE /dbv_es_idx");
            }
        }
    }

    @Test
    public void aliasIndices() throws Exception {
        Properties props = new Properties();
        try (Connection conn = DriverManager.getConnection(ES_URL, props)) {
            // 1. Prepare Index
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DELETE /dbv_es_alias_idx");
                } catch (Exception e) {
                    // ignore
                } finally {
                    stmt.executeUpdate("PUT /dbv_es_alias_idx");
                }
            }

            // 2. Create Alias
            try (Statement stmt = conn.createStatement()) {
                String aliasCmd = "POST /_aliases\n" + "{\n" + "    \"actions\" : [\n" + "        { \"add\" : { \"index\" : \"dbv_es_alias_idx\", \"alias\" : \"dbv_es_alias_name\" } }\n" + "    ]\n" + "}";
                int res = stmt.executeUpdate(aliasCmd);
                assertEquals(1, res);
            }

            // 3. cleanup
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE /dbv_es_alias_idx");
            }
        }
    }

    @Test
    public void testIndexOpenClose() throws Exception {
        Properties props = new Properties();
        try (Connection conn = DriverManager.getConnection(ES_URL, props)) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DELETE /dbv_es_mgmt");
                } catch (Exception e) {
                    // ignore
                } finally {
                    stmt.executeUpdate("PUT /dbv_es_mgmt");
                }
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
        Properties props = new Properties();
        try (Connection conn = DriverManager.getConnection(ES_URL, props)) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DELETE /dbv_es_mgmt_args");
                } catch (Exception e) {
                    // ignore
                } finally {
                    stmt.executeUpdate("PUT /dbv_es_mgmt_args");
                }
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
