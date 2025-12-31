package net.hasor.dbvisitor.adapter.elastic.realdb.es6;

import java.sql.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Elastic6CrudPlaceholderTest {
    private static final String ES_URL     = "jdbc:dbvisitor:elastic://127.0.0.1:19200?indexRefresh=true";
    private static final String INDEX_NAME = "test_crud_placeholder";

    @Before
    public void before() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /" + INDEX_NAME);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @After
    public void after() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /" + INDEX_NAME);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void testInsert() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL)) {
            // Test INSERT with placeholder in URL and Body
            String sql = "POST /{?}/_doc { \"name\": ?, \"age\": ? }";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, INDEX_NAME);
                ps.setString(2, "Alice");
                ps.setInt(3, 30);

                int count = ps.executeUpdate();
                assertEquals(1, count);
            }

            // Verify
            // Use match query to avoid case sensitivity issues for text fields
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("POST /" + INDEX_NAME + "/_search { \"query\": { \"match\": { \"name\": \"Alice\" } } }")) {
                assertTrue("Data not found", rs.next());
                assertEquals("Alice", rs.getString("name"));
                assertEquals(30, rs.getInt("age"));
            }
        }
    }

    @Test
    public void testGet() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL)) {
            // Prepare Data
            try (Statement s = c.createStatement()) {
                s.executeUpdate("POST /" + INDEX_NAME + "/_doc/1 { \"name\": \"Bob\", \"age\": 25 }");
            }

            // Test GET with placeholder in URL
            // GET /{index}/_doc/{id}/_source (ES 6 compatible) to get flattened fields
            String sql = "GET /{?}/_doc/{?}/_source";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, INDEX_NAME);
                ps.setString(2, "1");

                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue("Data not found", rs.next());
                    assertEquals("Bob", rs.getString("name"));
                    assertEquals(25, rs.getInt("age"));
                }
            }
        }
    }

    @Test
    public void testSearch() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL)) {
            // Prepare Data
            try (Statement s = c.createStatement()) {
                s.executeUpdate("POST /" + INDEX_NAME + "/_doc/1 { \"name\": \"Charlie\", \"age\": 35 }");
                s.executeUpdate("POST /" + INDEX_NAME + "/_doc/2 { \"name\": \"Dave\", \"age\": 40 }");
            }

            // Test SEARCH with placeholder in Body
            String sql = "POST /{?}/_search { \"query\": { \"term\": { \"age\": ? } } }";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, INDEX_NAME);
                ps.setInt(2, 35);

                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue("Data not found", rs.next());
                    assertEquals("Charlie", rs.getString("name"));
                }
            }
        }
    }

    @Test
    public void testUpdate() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL)) {
            // Prepare Data
            try (Statement s = c.createStatement()) {
                s.executeUpdate("POST /" + INDEX_NAME + "/_doc/1 { \"name\": \"Eve\", \"age\": 20 }");
            }

            // Test UPDATE with placeholder
            // POST /{index}/_doc/{id}/_update
            String sql = "POST /{?}/_doc/{?}/_update { \"doc\": { \"age\": ? } }";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, INDEX_NAME);
                ps.setString(2, "1");
                ps.setInt(3, 21);

                int count = ps.executeUpdate();
                assertEquals(1, count);
            }

            // Verify using _search to ensure flattened result
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("POST /" + INDEX_NAME + "/_search { \"query\": { \"term\": { \"_id\": \"1\" } } }")) {
                assertTrue("Data not found", rs.next());
                assertEquals(21, rs.getInt("age"));
            }
        }
    }

    @Test
    public void testDelete() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL)) {
            // Prepare Data
            try (Statement s = c.createStatement()) {
                s.executeUpdate("POST /" + INDEX_NAME + "/_doc/1 { \"name\": \"Frank\", \"age\": 50 }");
            }

            // Test DELETE with placeholder
            // DELETE /{index}/_doc/{id}
            String sql = "DELETE /{?}/_doc/{?}";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, INDEX_NAME);
                ps.setString(2, "1");

                int count = ps.executeUpdate();
                assertEquals(1, count);
            }

            // Verify using search
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("POST /" + INDEX_NAME + "/_search { \"query\": { \"term\": { \"_id\": \"1\" } } }")) {
                assertFalse("Data should be deleted", rs.next());
            }
        }
    }
}
