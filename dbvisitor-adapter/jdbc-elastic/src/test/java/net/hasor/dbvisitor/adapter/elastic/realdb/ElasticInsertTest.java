package net.hasor.dbvisitor.adapter.elastic.realdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Before;
import org.junit.Test;

public class ElasticInsertTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://127.0.0.1:19200";

    @Before
    public void before() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_insert_doc");
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
            if (count != 1) {
                throw new Exception("Insert failed");
            }

            Thread.sleep(1000); // wait for refresh

            try (ResultSet rs = s.executeQuery("POST /test_insert_doc/_search { \"query\": { \"term\": { \"value\": " + randomValue + " } } }")) {
                if (rs.next()) {
                    long val = rs.getLong("value");
                    if (val != randomValue) {
                        throw new Exception("Data mismatch: expected " + randomValue + ", got " + val);
                    }
                } else {
                    throw new Exception("Data not found for value: " + randomValue);
                }
            }
        }
    }

    @Test
    public void testInsertDocAndReturnKeys() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            int count = s.executeUpdate("POST /test_insert_doc/_doc { \"name\": \"doc1\", \"value\": 123 }", Statement.RETURN_GENERATED_KEYS);
            if (count != 1) {
                throw new Exception("Insert failed");
            }
            try (ResultSet rs = s.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println("Generated ID: " + rs.getString(1));
                } else {
                    throw new Exception("No generated key returned for POST /_doc");
                }
            }
        }
    }

    @Test
    public void testInsertDocWithId() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            long randomValue = new java.util.Random().nextLong();
            int count = s.executeUpdate("PUT /test_insert_doc/_doc/2 { \"name\": \"doc2\", \"value\": " + randomValue + " }");
            if (count != 1) {
                throw new Exception("Insert failed");
            }

            Thread.sleep(1000); // wait for refresh

            try (ResultSet rs = s.executeQuery("POST /test_insert_doc/_search { \"query\": { \"term\": { \"_id\": \"2\" } } }")) {
                if (rs.next()) {
                    long val = rs.getLong("value");
                    if (val != randomValue) {
                        throw new Exception("Data mismatch: expected " + randomValue + ", got " + val);
                    }
                } else {
                    throw new Exception("Data not found for id: 2");
                }
            }
        }
    }

    @Test
    public void testInsertDocWithIdAndReturnKeys() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            int count = s.executeUpdate("PUT /test_insert_doc/_doc/3 { \"name\": \"doc2\", \"value\": 456 }", Statement.RETURN_GENERATED_KEYS);
            if (count != 1) {
                throw new Exception("Insert failed");
            }
            try (ResultSet rs = s.getGeneratedKeys()) {
                if (rs.next()) {
                    String id = rs.getString(1);
                    System.out.println("Generated ID: " + id);
                    if (!"3".equals(id)) {
                        throw new Exception("Expected ID 3, but got " + id);
                    }
                } else {
                    throw new Exception("No generated key returned for PUT /_doc/3");
                }
            }
        }
    }

    private boolean isEs7OrLater() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("GET /_cat/nodes?h=version")) {
                if (rs.next()) {
                    String version = rs.getString("VERSION");
                    return version != null && (version.startsWith("7.") || version.startsWith("8."));
                }
            }
        }
        return false;
    }

    @Test
    public void testPutInsertCreateWithId() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            long randomValue = new java.util.Random().nextLong();
            String sql;
            if (isEs7OrLater()) {
                sql = "PUT /test_insert_doc/_create/4 { \"name\": \"doc3\", \"value\": " + randomValue + " }";
            } else {
                sql = "PUT /test_insert_doc/_doc/4/_create { \"name\": \"doc3\", \"value\": " + randomValue + " }";
            }
            int count = s.executeUpdate(sql);
            if (count != 1) {
                throw new Exception("Insert failed");
            }

            Thread.sleep(1000); // wait for refresh

            try (ResultSet rs = s.executeQuery("POST /test_insert_doc/_search { \"query\": { \"term\": { \"_id\": \"4\" } } }")) {
                if (rs.next()) {
                    long val = rs.getLong("value");
                    if (val != randomValue) {
                        throw new Exception("Data mismatch: expected " + randomValue + ", got " + val);
                    }
                } else {
                    throw new Exception("Data not found for id: 4");
                }
            }
        }
    }

    @Test
    public void testPutInsertCreateWithIdAndReturnKeys() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            String sql;
            if (isEs7OrLater()) {
                sql = "PUT /test_insert_doc/_create/5 { \"name\": \"doc3\", \"value\": 789 }";
            } else {
                sql = "PUT /test_insert_doc/_doc/5/_create { \"name\": \"doc3\", \"value\": 789 }";
            }
            int count = s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            if (count != 1) {
                throw new Exception("Insert failed");
            }
            try (ResultSet rs = s.getGeneratedKeys()) {
                if (rs.next()) {
                    String id = rs.getString(1);
                    System.out.println("Generated ID: " + id);
                    if (!"5".equals(id)) {
                        throw new Exception("Expected ID 5, but got " + id);
                    }
                } else {
                    throw new Exception("No generated key returned for PUT /_create/5");
                }
            }
        }
    }

    @Test
    public void testPostInsertCreateWithId() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            long randomValue = new java.util.Random().nextLong();
            String sql;
            if (isEs7OrLater()) {
                sql = "POST /test_insert_doc/_create/6 { \"name\": \"doc4\", \"value\": " + randomValue + " }";
            } else {
                sql = "POST /test_insert_doc/_doc/6/_create { \"name\": \"doc4\", \"value\": " + randomValue + " }";
            }
            int count = s.executeUpdate(sql);
            if (count != 1) {
                throw new Exception("Insert failed");
            }

            Thread.sleep(1000); // wait for refresh

            try (ResultSet rs = s.executeQuery("POST /test_insert_doc/_search { \"query\": { \"term\": { \"_id\": \"6\" } } }")) {
                if (rs.next()) {
                    long val = rs.getLong("value");
                    if (val != randomValue) {
                        throw new Exception("Data mismatch: expected " + randomValue + ", got " + val);
                    }
                } else {
                    throw new Exception("Data not found for id: 6");
                }
            }
        }
    }

    @Test
    public void testPostInsertCreateWithIdAndReturnKeys() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            String sql;
            if (isEs7OrLater()) {
                sql = "POST /test_insert_doc/_create/7 { \"name\": \"doc4\", \"value\": 101112 }";
            } else {
                sql = "POST /test_insert_doc/_doc/7/_create { \"name\": \"doc4\", \"value\": 101112 }";
            }
            int count = s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            if (count != 1) {
                throw new Exception("Insert failed");
            }
            try (ResultSet rs = s.getGeneratedKeys()) {
                if (rs.next()) {
                    String id = rs.getString(1);
                    System.out.println("Generated ID: " + id);
                    if (!"7".equals(id)) {
                        throw new Exception("Expected ID 7, but got " + id);
                    }
                } else {
                    throw new Exception("No generated key returned for POST /_create/7");
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
            if (count != 1) {
                throw new Exception("Insert failed");
            }

            Thread.sleep(1000); // wait for refresh

            // Verify with search
            try (ResultSet rs = s.executeQuery("POST /test_insert_generic/_search { \"query\": { \"term\": { \"_id\": \"1\" } } }")) {
                if (rs.next()) {
                    long val = rs.getLong("value");
                    if (val != randomValue) {
                        throw new Exception("Data mismatch: expected " + randomValue + ", got " + val);
                    }
                } else {
                    throw new Exception("Data not found for id: 1");
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
            if (count != 1) {
                throw new Exception("Insert failed");
            }

            try (ResultSet rs = s.getGeneratedKeys()) {
                if (rs.next()) {
                    String id = rs.getString(1);
                    System.out.println("Generated ID: " + id);
                    if (id == null || id.isEmpty()) {
                        throw new Exception("Expected generated ID, got null/empty");
                    }
                } else {
                    throw new Exception("No generated key returned for POST /test_insert_generic/mytype");
                }
            }
        }
    }
}
