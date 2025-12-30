package net.hasor.dbvisitor.adapter.elastic.realdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Test;

public class ElasticGenericTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://127.0.0.1:19200";

    @Test
    public void testGenericGet() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            boolean result = s.execute("GET /");
            assert result;
            try (ResultSet rs = s.getResultSet()) {
                assert rs.next();
                // The root response of ES usually contains "name", "cluster_name", "version", etc.
                // We just check if we got a result.
                System.out.println("Generic GET / result: " + rs.getObject(1));
            }
        }
    }

    @Test
    public void testGenericPost() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_generic");
            } catch (Exception e) {
                // ignore
            }
            
            // Create index/doc using GENERIC
            s.execute("POST /test_generic/_doc/1 { \"name\": \"generic_test\" }");
            
            Thread.sleep(1000);

            // Verify with standard GET
            try (ResultSet rs = s.executeQuery("GET /test_generic/_doc/1")) {
                assert rs.next();
                String source = rs.getString("_source");
                assert source.contains("generic_test");
            }
            
            // Clean up
             try {
                s.execute("DELETE /test_generic");
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
