package net.hasor.dbvisitor.test.oneapi.realdb.elastic7;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import net.hasor.dbvisitor.test.oneapi.config.OneApiDataSourceManager;
import org.junit.Test;
import static org.junit.Assert.*;

public class Elastic7CommandTest {
    @Test
    public void testGetSet() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection("es7")) {
            try (Statement s = conn.createStatement()) {
                // 1. clean
                try {
                    s.execute("DELETE /test_user_info");
                } catch (Exception e) {
                    // ignore
                }

                // 2. insert
                s.execute("POST /test_user_info/_doc/1 { \"name\": \"mali\", \"age\": 26 }");

                // 3. query
                try (ResultSet rs = s.executeQuery("POST /test_user_info/_search { \"query\": { \"match\": { \"name\": \"mali\" } } }")) {
                    if (rs.next()) {
                        String id = rs.getString("_ID");
                        String doc = rs.getString("_DOC");
                        String name = rs.getString("name");
                        int age = rs.getInt("age");

                        assertEquals("id not match", "1", id);
                        assertTrue("doc not match (name): " + doc, doc.contains("\"name\":\"mali\"") || doc.contains("\"name\": \"mali\""));
                        assertTrue("doc not match (age): " + doc, doc.contains("\"age\":26") || doc.contains("\"age\": 26"));
                        assertEquals("name not match", "mali", name);
                        assertEquals("age not match", 26, age);
                    } else {
                        fail("no data found");
                    }
                }
            }
        }
    }
}
