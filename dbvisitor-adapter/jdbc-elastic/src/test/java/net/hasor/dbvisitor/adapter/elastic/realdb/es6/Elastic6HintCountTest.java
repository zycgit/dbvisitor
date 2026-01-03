package net.hasor.dbvisitor.adapter.elastic.realdb.es6;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Elastic6HintCountTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://127.0.0.1:19200?indexRefresh=true";

    @Before
    public void before() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_hint_count");
            } catch (Exception e) {
                // ignore
            }
            s.executeUpdate("POST /test_hint_count/_doc/1 { \"name\": \"Alice\", \"age\": 30, \"seq\": 1 }");
            s.executeUpdate("POST /test_hint_count/_doc/2 { \"name\": \"Bob\", \"age\": 25, \"seq\": 2 }");
            s.executeUpdate("POST /test_hint_count/_doc/3 { \"name\": \"Charlie\", \"age\": 35, \"seq\": 3 }");
            s.executeUpdate("POST /test_hint_count/_doc/4 { \"name\": \"David\", \"age\": 28, \"seq\": 4 }");
            s.executeUpdate("POST /test_hint_count/_doc/5 { \"name\": \"Eve\", \"age\": 22, \"seq\": 5 }");
        }
    }

    @After
    public void after() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_hint_count");
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void testHintCount() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            // Test count all
            String sql1 = "/*+ overwrite_find_as_count */ POST /test_hint_count/_search { \"query\": { \"match_all\": {} } }";
            try (ResultSet rs = s.executeQuery(sql1)) {
                assertTrue(rs.next());
                assertEquals(5, rs.getLong("COUNT"));
                assertFalse(rs.next());
            }

            // Test count with filter
            String sql2 = "/*+ overwrite_find_as_count */ POST /test_hint_count/_search { \"query\": { \"range\": { \"age\": { \"gte\": 30 } } } }";
            try (ResultSet rs = s.executeQuery(sql2)) {
                assertTrue(rs.next());
                // Alice(30), Charlie(35) -> 2
                assertEquals(2, rs.getLong("COUNT"));
                assertFalse(rs.next());
            }
        }
    }
}
