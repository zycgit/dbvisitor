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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Elastic6IndexSettingsTest {
    private static final String ES_URL     = "jdbc:dbvisitor:elastic://localhost:2920?indexRefresh=true";
    private static final String INDEX_NAME = "dbv_settings_test_idx";

    @BeforeEach
    public void setUp() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL)) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DELETE /" + INDEX_NAME);
                } catch (Exception e) {
                    Elastic6Cleanup.requireMissingIndex(e);
                }
                stmt.executeUpdate("PUT /" + INDEX_NAME);
            }
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("DELETE /" + INDEX_NAME);
            } catch (Exception e) {
                Elastic6Cleanup.requireMissingIndex(e);
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
                assertTrue(foundReplicas, "Should find index.number_of_replicas setting");
                assertTrue(foundShards, "Should find index.number_of_shards setting");
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
                assertTrue(found, "Should find index.number_of_replicas setting before update");
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
                assertTrue(verified, "Should verify updated replica count to 0");
            }
        }
    }

    public static void main(String[] args) {
        net.hasor.dbvisitor.test.realdb.RealDbTestRunner.run(Elastic6IndexSettingsTest.class);
    }
}
