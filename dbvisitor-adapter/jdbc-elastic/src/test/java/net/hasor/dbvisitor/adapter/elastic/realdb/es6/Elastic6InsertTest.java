package net.hasor.dbvisitor.adapter.elastic.realdb.es6;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Elastic6InsertTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://127.0.0.1:19200?indexRefresh=true";

    @Before
    public void before() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_insert_doc");
            } catch (Exception e) {
                // ignore
            }
            try {
                s.execute("DELETE /test_insert_generic");
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @After
    public void after() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_insert_doc");
            } catch (Exception e) {
                // ignore
            }
            try {
                s.execute("DELETE /test_insert_generic");
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void testInsertDoc() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            long randomValue = new java.util.Random().nextLong();
            int count = s.executeUpdate("POST /test_insert_doc/_doc { \"name\": \"doc1\", \"value\": " + randomValue + " }");
            Assert.assertEquals("Insert failed", 1, count);

            try (ResultSet rs = s.executeQuery("POST /test_insert_doc/_search { \"query\": { \"term\": { \"value\": " + randomValue + " } } }")) {
                if (rs.next()) {
                    long val = rs.getLong("value");
                    Assert.assertEquals("Data mismatch: expected " + randomValue + ", got " + val, randomValue, val);
                } else {
                    Assert.fail("Data not found for value: " + randomValue);
                }
            }
        }
    }

    @Test
    public void testInsertDocAndReturnKeys() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            int count = s.executeUpdate("POST /test_insert_doc/_doc { \"name\": \"doc1\", \"value\": 123 }", Statement.RETURN_GENERATED_KEYS);
            Assert.assertEquals("Insert failed", 1, count);
            try (ResultSet rs = s.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println("Generated ID: " + rs.getString(1));
                } else {
                    Assert.fail("No generated key returned for POST /_doc");
                }
            }
        }
    }

    @Test
    public void testInsertDocWithId() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            long randomValue = new java.util.Random().nextLong();
            int count = s.executeUpdate("PUT /test_insert_doc/_doc/2 { \"name\": \"doc2\", \"value\": " + randomValue + " }");
            Assert.assertEquals("Insert failed", 1, count);

            try (ResultSet rs = s.executeQuery("POST /test_insert_doc/_search { \"query\": { \"term\": { \"_id\": \"2\" } } }")) {
                if (rs.next()) {
                    long val = rs.getLong("value");
                    Assert.assertEquals("Data mismatch: expected " + randomValue + ", got " + val, randomValue, val);
                } else {
                    Assert.fail("Data not found for id: 2");
                }
            }
        }
    }

    @Test
    public void testInsertDocWithIdAndReturnKeys() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            int count = s.executeUpdate("PUT /test_insert_doc/_doc/3 { \"name\": \"doc2\", \"value\": 456 }", Statement.RETURN_GENERATED_KEYS);
            Assert.assertEquals("Insert failed", 1, count);
            try (ResultSet rs = s.getGeneratedKeys()) {
                if (rs.next()) {
                    String id = rs.getString(1);
                    System.out.println("Generated ID: " + id);
                    Assert.assertEquals("Expected ID 3, but got " + id, "3", id);
                } else {
                    Assert.fail("No generated key returned for PUT /_doc/3");
                }
            }
        }
    }

    @Test
    public void testPutInsertCreateWithId() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            long randomValue = new java.util.Random().nextLong();
            String sql = "PUT /test_insert_doc/_doc/4/_create { \"name\": \"doc3\", \"value\": " + randomValue + " }";
            int count = s.executeUpdate(sql);
            Assert.assertEquals("Insert failed", 1, count);

            try (ResultSet rs = s.executeQuery("POST /test_insert_doc/_search { \"query\": { \"term\": { \"_id\": \"4\" } } }")) {
                if (rs.next()) {
                    long val = rs.getLong("value");
                    Assert.assertEquals("Data mismatch: expected " + randomValue + ", got " + val, randomValue, val);
                } else {
                    Assert.fail("Data not found for id: 4");
                }
            }
        }
    }

    @Test
    public void testPutInsertCreateWithIdAndReturnKeys() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            String sql = "PUT /test_insert_doc/_doc/5/_create { \"name\": \"doc3\", \"value\": 789 }";
            int count = s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            Assert.assertEquals("Insert failed", 1, count);
            try (ResultSet rs = s.getGeneratedKeys()) {
                if (rs.next()) {
                    String id = rs.getString(1);
                    System.out.println("Generated ID: " + id);
                    Assert.assertEquals("Expected ID 5, but got " + id, "5", id);
                } else {
                    Assert.fail("No generated key returned for PUT /_create/5");
                }
            }
        }
    }

    @Test
    public void testPostInsertCreateWithId() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            long randomValue = new java.util.Random().nextLong();
            String sql = "POST /test_insert_doc/_doc/6/_create { \"name\": \"doc4\", \"value\": " + randomValue + " }";
            int count = s.executeUpdate(sql);
            Assert.assertEquals("Insert failed", 1, count);
            try (ResultSet rs = s.executeQuery("POST /test_insert_doc/_search { \"query\": { \"term\": { \"_id\": \"6\" } } }")) {
                if (rs.next()) {
                    long val = rs.getLong("value");
                    Assert.assertEquals("Data mismatch: expected " + randomValue + ", got " + val, randomValue, val);
                } else {
                    Assert.fail("Data not found for id: 6");
                }
            }
        }
    }

    @Test
    public void testPostInsertCreateWithIdAndReturnKeys() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            String sql = "POST /test_insert_doc/_doc/7/_create { \"name\": \"doc4\", \"value\": 101112 }";
            int count = s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            Assert.assertEquals("Insert failed", 1, count);
            try (ResultSet rs = s.getGeneratedKeys()) {
                if (rs.next()) {
                    String id = rs.getString(1);
                    System.out.println("Generated ID: " + id);
                    Assert.assertEquals("Expected ID 7, but got " + id, "7", id);
                } else {
                    Assert.fail("No generated key returned for POST /_create/7");
                }
            }
        }
    }

    @Test
    public void testGenericInsertWithId() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            long randomValue = new java.util.Random().nextLong();
            // Use a custom type 'mytype' to avoid matching _doc or _create rules
            // This works on ES 6. On ES 7 it works but is deprecated.
            String sql = "PUT /test_insert_generic/mytype/1 { \"name\": \"generic\", \"value\": " + randomValue + " }";

            int count = s.executeUpdate(sql);
            Assert.assertEquals("Insert failed", 1, count);
            try (ResultSet rs = s.executeQuery("POST /test_insert_generic/_search { \"query\": { \"term\": { \"_id\": \"1\" } } }")) {
                if (rs.next()) {
                    long val = rs.getLong("value");
                    Assert.assertEquals("Data mismatch: expected " + randomValue + ", got " + val, randomValue, val);
                } else {
                    Assert.fail("Data not found for id: 1");
                }
            }
        }
    }

    @Test
    public void testGenericInsertAutoId() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            // Use a custom type 'mytype' to avoid matching _doc or _create rules
            String sql = "POST /test_insert_generic/mytype { \"name\": \"generic_auto\", \"value\": 999 }";

            int count = s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            Assert.assertEquals("Insert failed", 1, count);

            try (ResultSet rs = s.getGeneratedKeys()) {
                if (rs.next()) {
                    String id = rs.getString(1);
                    System.out.println("Generated ID: " + id);
                    Assert.assertNotNull("Expected generated ID, got null", id);
                    Assert.assertFalse("Expected generated ID, got empty", id.isEmpty());
                } else {
                    Assert.fail("No generated key returned for POST /test_insert_generic/mytype");
                }
            }
        }
    }
}
