package net.hasor.dbvisitor.adapter.elastic.realdb.es7;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class Elastic7GlobalMappingTest {
    private static final String ES_URL       = "jdbc:dbvisitor:elastic://127.0.0.1:19201?indexRefresh=true";
    private static final String INDEX_NAME_1 = "dbv_global_mapping_test_idx_1";
    private static final String INDEX_NAME_2 = "dbv_global_mapping_test_idx_2";

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
                String putIndex1 = "PUT /" + INDEX_NAME_1 + " {" + //
                        "\"mappings\": {" +                        //
                        "    \"properties\": {" +                  //
                        "      \"name\": { \"type\": \"text\" }" + //
                        "    }" +                                  //
                        "}" +                                      //
                        "}";                                       //
                stmt.executeUpdate(putIndex1);

                String putIndex2 = "PUT /" + INDEX_NAME_2 + " {" + //
                        "\"mappings\": {" +                        //
                        "    \"properties\": {" +                  //
                        "      \"title\": { \"type\": \"text\" }" +//
                        "    }" +                                  //
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
    public void testGetGlobalMapping() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            // GET /_mapping should return mappings for all indices
            try (ResultSet rs = stmt.executeQuery("GET /_mapping")) {
                Set<String> foundIndices = new HashSet<>();
                while (rs.next()) {
                    foundIndices.add(rs.getString("NAME"));
                }

                assertTrue("Should contain index 1", foundIndices.contains(INDEX_NAME_1));
                assertTrue("Should contain index 2", foundIndices.contains(INDEX_NAME_2));
            }
        }
    }

    @Test
    public void testGetAllMapping() throws Exception {
        try (Connection conn = DriverManager.getConnection(ES_URL); Statement stmt = conn.createStatement()) {
            // GET /_all/_mapping is equivalent to GET /_mapping
            try (ResultSet rs = stmt.executeQuery("GET /_all/_mapping")) {
                Set<String> foundIndices = new HashSet<>();
                while (rs.next()) {
                    foundIndices.add(rs.getString("NAME"));
                }

                assertTrue("Should contain index 1", foundIndices.contains(INDEX_NAME_1));
                assertTrue("Should contain index 2", foundIndices.contains(INDEX_NAME_2));
            }
        }
    }
}
