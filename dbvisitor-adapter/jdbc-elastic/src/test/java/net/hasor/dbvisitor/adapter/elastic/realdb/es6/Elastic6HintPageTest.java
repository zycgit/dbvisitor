package net.hasor.dbvisitor.adapter.elastic.realdb.es6;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Elastic6HintPageTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://127.0.0.1:19200?indexRefresh=true";

    @Before
    public void before() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_hint_search");
            } catch (Exception e) {
                // ignore
            }
            s.executeUpdate("POST /test_hint_search/_doc/1 { \"name\": \"Alice\", \"age\": 30, \"seq\": 1 }");
            s.executeUpdate("POST /test_hint_search/_doc/2 { \"name\": \"Bob\", \"age\": 25, \"seq\": 2 }");
            s.executeUpdate("POST /test_hint_search/_doc/3 { \"name\": \"Charlie\", \"age\": 35, \"seq\": 3 }");
            s.executeUpdate("POST /test_hint_search/_doc/4 { \"name\": \"David\", \"age\": 28, \"seq\": 4 }");
            s.executeUpdate("POST /test_hint_search/_doc/5 { \"name\": \"Eve\", \"age\": 22, \"seq\": 5 }");
        }
    }

    @org.junit.After
    public void after() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_hint_search");
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void testHintPagination() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            // Test limit=2, skip=0
            // Note: We use sort to ensure deterministic order
            String sql1 = "/*+ overwrite_find_limit=2, overwrite_find_skip=0 */ POST /test_hint_search/_search { \"query\": { \"match_all\": {} }, \"sort\": [{\"seq\": \"asc\"}] }";
            try (ResultSet rs = s.executeQuery(sql1)) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    if (count == 1) assertEquals(1, rs.getInt("seq"));
                    if (count == 2) assertEquals(2, rs.getInt("seq"));
                }
                assertEquals(2, count);
            }

            // Test limit=2, skip=2
            String sql2 = "/*+ overwrite_find_limit=2, overwrite_find_skip=2 */ POST /test_hint_search/_search { \"query\": { \"match_all\": {} }, \"sort\": [{\"seq\": \"asc\"}] }";
            try (ResultSet rs = s.executeQuery(sql2)) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    if (count == 1) assertEquals(3, rs.getInt("seq"));
                    if (count == 2) assertEquals(4, rs.getInt("seq"));
                }
                assertEquals(2, count);
            }
            
            // Test limit=2, skip=4 (should return 1 result)
            String sql3 = "/*+ overwrite_find_limit=2, overwrite_find_skip=4 */ POST /test_hint_search/_search { \"query\": { \"match_all\": {} }, \"sort\": [{\"seq\": \"asc\"}] }";
            try (ResultSet rs = s.executeQuery(sql3)) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    if (count == 1) assertEquals(5, rs.getInt("seq"));
                }
                assertEquals(1, count);
            }
        }
    }
}
