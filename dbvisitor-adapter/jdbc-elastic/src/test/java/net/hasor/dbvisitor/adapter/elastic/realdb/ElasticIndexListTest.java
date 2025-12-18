package net.hasor.dbvisitor.adapter.elastic.realdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElasticIndexListTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://localhost:19200";

    @Test
    public void list_indices_via_aliases() throws Exception {
        Properties props = new Properties();

        try (Connection conn = DriverManager.getConnection(ES_URL, props)) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DELETE /dbv_es_idx");
                } catch (Exception e) {
                    // ignore
                }
                // ensure test index exists
                stmt.executeUpdate("PUT /dbv_es_idx");
            }

            Set<String> names = new HashSet<>();
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("GET /_aliases")) {
                while (rs.next()) {
                    names.add(rs.getString("NAME"));
                }
            }

            assertFalse("index list should not be empty", names.isEmpty());
            assertTrue("should contain created index", names.contains("dbv_es_idx"));

            // cleanup
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE /dbv_es_idx");
            }
        }
    }
}
