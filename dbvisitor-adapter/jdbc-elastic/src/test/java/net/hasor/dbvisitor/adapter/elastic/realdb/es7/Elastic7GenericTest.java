package net.hasor.dbvisitor.adapter.elastic.realdb.es7;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Assert;
import org.junit.Test;

public class Elastic7GenericTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://127.0.0.1:19201?indexRefresh=true";

    @Test
    public void testGenericGet() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            boolean result = s.execute("GET /");
            Assert.assertTrue(result);
            try (ResultSet rs = s.getResultSet()) {
                Assert.assertTrue(rs.next());
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

            // Verify with standard GET
            try (ResultSet rs = s.executeQuery("GET /test_generic/_doc/1")) {
                Assert.assertTrue(rs.next());
                String source = rs.getString("_source");
                Assert.assertTrue(source.contains("generic_test"));
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
