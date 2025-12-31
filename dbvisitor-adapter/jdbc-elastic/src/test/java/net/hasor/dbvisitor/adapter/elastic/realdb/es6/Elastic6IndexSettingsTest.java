package net.hasor.dbvisitor.adapter.elastic.realdb.es6;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Elastic6IndexSettingsTest {
    private static final String ES_URL     = "jdbc:dbvisitor:elastic://localhost:19200?indexRefresh=true";
    private static final String INDEX_NAME = "dbv_settings_test_idx";

    @Before
    public void setUp() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL)) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DELETE /" + INDEX_NAME);
                } catch (Exception e) {
                    // ignore if not exists
                }
                stmt.executeUpdate("PUT /" + INDEX_NAME);
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
    public void testGetSettings() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("GET /" + INDEX_NAME + "/_settings")) {
                boolean foundReplicas = false;
                boolean foundShards = false;
                while (rs.next()) {
                    String name = rs.getString("NAME");
                    String setting = rs.getString("SETTING");
                    String value = rs.getString("VALUE");

                    assertEquals(INDEX_NAME, name);
                    if ("index.number_of_replicas".equals(setting)) {
                        foundReplicas = true;
                    }
                    if ("index.number_of_shards".equals(setting)) {
                        foundShards = true;
                    }
                }
                assertTrue("Should find index.number_of_replicas setting", foundReplicas);
                assertTrue("Should find index.number_of_shards setting", foundShards);
            }
        }
    }

    @Test
    public void testSetSettings() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            // 1. Verify before update
            try (ResultSet rs = stmt.executeQuery("GET /" + INDEX_NAME + "/_settings")) {
                boolean found = false;
                while (rs.next()) {
                    if ("index.number_of_replicas".equals(rs.getString("SETTING"))) {
                        found = true;
                    }
                }
                assertTrue("Should find index.number_of_replicas setting before update", found);
            }

            // 2. Update number of replicas
            String updateCmd = "PUT /" + INDEX_NAME + "/_settings {\"index\" : {\"number_of_replicas\" : 0}}";
            int res = stmt.executeUpdate(updateCmd);
            assertEquals(1, res);

            // 3. Verify after update
            try (ResultSet rs = stmt.executeQuery("GET /" + INDEX_NAME + "/_settings")) {
                boolean verified = false;
                while (rs.next()) {
                    String setting = rs.getString("SETTING");
                    String value = rs.getString("VALUE");
                    if ("index.number_of_replicas".equals(setting)) {
                        assertEquals("0", value);
                        verified = true;
                    }
                }
                assertTrue("Should verify updated replica count to 0", verified);
            }
        }
    }
}
