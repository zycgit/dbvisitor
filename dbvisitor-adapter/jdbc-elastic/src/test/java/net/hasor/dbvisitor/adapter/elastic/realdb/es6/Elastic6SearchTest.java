package net.hasor.dbvisitor.adapter.elastic.realdb.es6;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Elastic6SearchTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://127.0.0.1:19200?indexRefresh=true";

    @Before
    public void before() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_msearch_1");
            } catch (Exception e) {
                // ignore
            }
            try {
                s.execute("DELETE /test_msearch_2");
            } catch (Exception e) {
                // ignore
            }
            s.executeUpdate("POST /test_msearch_1/_doc/1 { \"name\": \"doc1\", \"value\": 100 }");
            s.executeUpdate("POST /test_msearch_2/_doc/2 { \"name\": \"doc2\", \"value\": 200 }");

            try {
                s.execute("DELETE /test_search");
            } catch (Exception e) {
                // ignore
            }
            s.executeUpdate("POST /test_search/_doc/1 { \"name\": \"Alice\", \"age\": 30, \"city\": \"New York\" }");
            s.executeUpdate("POST /test_search/_doc/2 { \"name\": \"Bob\", \"age\": 25, \"city\": \"Los Angeles\" }");
            s.executeUpdate("POST /test_search/_doc/3 { \"name\": \"Charlie\", \"age\": 35, \"city\": \"New York\" }");
        }
    }

    @After
    public void after() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_msearch_1");
            } catch (Exception e) {
                // ignore
            }
            try {
                s.execute("DELETE /test_msearch_2");
            } catch (Exception e) {
                // ignore
            }
            try {
                s.execute("DELETE /test_search");
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void testSearchMatchAll() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("POST /test_search/_search { \"query\": { \"match_all\": {} } }")) {
                int count = 0;
                while (rs.next()) {
                    count++;
                }
                Assert.assertEquals("Expected 3 results", 3, count);
            }
        }
    }

    @Test
    public void testSearchMatch() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("POST /test_search/_search { \"query\": { \"match\": { \"city\": \"York\" } } }")) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    String city = rs.getString("city");
                    Assert.assertTrue("Unexpected city: " + city, city.contains("York"));
                }
                Assert.assertEquals("Expected 2 results for 'York'", 2, count);
            }
        }
    }

    @Test
    public void testSearchPagination() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("POST /test_search/_search { \"from\": 0, \"size\": 1, \"sort\": [{\"age\": \"asc\"}] }")) {
                if (rs.next()) {
                    Assert.assertEquals("Expected Bob as first result", "Bob", rs.getString("name"));
                } else {
                    Assert.fail("Expected 1 result");
                }
                Assert.assertFalse("Expected only 1 result", rs.next());
            }
        }
    }

    @Test
    public void testCount() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("POST /test_search/_count { \"query\": { \"match_all\": {} } }")) {
                if (rs.next()) {
                    long count = rs.getLong("COUNT");
                    if (count != 3) {
                        System.err.println("DEBUG: Found " + count + " docs. Listing them:");
                        try (ResultSet rs2 = s.executeQuery("POST /test_search/_search { \"query\": { \"match_all\": {} } }")) {
                            while (rs2.next()) {
                                System.err.println("Doc: " + rs2.getString("name"));
                            }
                        }
                        Assert.assertEquals("Expected count 3", 3, count);
                    }
                } else {
                    Assert.fail("No result for count");
                }
            }

            try (ResultSet rs = s.executeQuery("POST /test_search/_count { \"query\": { \"match\": { \"city\": \"York\" } } }")) {
                if (rs.next()) {
                    long count = rs.getLong("COUNT");
                    Assert.assertEquals("Expected count 2 for 'York'", 2, count);
                } else {
                    Assert.fail("No result for count");
                }
            }
        }
    }

    @Test
    public void testMultiSearch() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            boolean hasResult = s.execute("POST /_msearch \n" + //
                    "[ \n" + //
                    "    { \"index\": \"test_msearch_1\" }, \n" + //
                    "    { \"query\": { \"match_all\": {} } }, \n" + //
                    "    { \"index\": \"test_msearch_2\" }, \n" + //
                    "    { \"query\": { \"match_all\": {} } } \n" + //
                    "]");

            if (hasResult) {
                try (ResultSet rs = s.getResultSet()) {
                    if (rs.next()) {
                        System.out.println("Result 1 ID: " + rs.getString("_ID"));
                        Assert.assertEquals("First result should be doc 1", "1", rs.getString("_ID"));
                    } else {
                        Assert.fail("First result set empty");
                    }
                }

                if (s.getMoreResults()) {
                    try (ResultSet rs = s.getResultSet()) {
                        if (rs.next()) {
                            System.out.println("Result 2 ID: " + rs.getString("_ID"));
                            Assert.assertEquals("Second result should be doc 2", "2", rs.getString("_ID"));
                        } else {
                            Assert.fail("Second result set empty");
                        }
                    }
                } else {
                    Assert.fail("Expected more results");
                }
            } else {
                Assert.fail("No results returned");
            }
        }
    }

    @Test
    public void testSearchPaginationWithHints() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            // Page 1: limit=1, skip=0
            String sql1 = "/*+ overwrite_find_limit=1; overwrite_find_skip=0 */ POST /test_search/_search { \"sort\": [{\"age\": \"asc\"}] }";
            try (ResultSet rs = s.executeQuery(sql1)) {
                if (rs.next()) {
                    Assert.assertEquals("Expected Bob as first result", "Bob", rs.getString("name"));
                } else {
                    Assert.fail("Expected 1 result");
                }
                Assert.assertFalse("Expected only 1 result", rs.next());
            }

            // Page 2: limit=1, skip=1
            String sql2 = "/*+ overwrite_find_limit=1; overwrite_find_skip=1 */ POST /test_search/_search { \"sort\": [{\"age\": \"asc\"}] }";
            try (ResultSet rs = s.executeQuery(sql2)) {
                if (rs.next()) {
                    Assert.assertEquals("Expected Alice as second result", "Alice", rs.getString("name"));
                } else {
                    Assert.fail("Expected 1 result");
                }
                Assert.assertFalse("Expected only 1 result", rs.next());
            }
        }
    }

    @Test
    public void testMultiSearchPaginationWithHints() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            // MultiSearch with hints applying to both sub-requests
            // Request 1: match_all on test_search (sorted by age)
            // Request 2: match_all on test_search (sorted by age)
            // Hints: limit=1, skip=0 -> Both should return Bob (first result)

            String sql = "/*+ overwrite_find_limit=1; overwrite_find_skip=0 */ POST /_msearch \n" + //
                    "[ " + //
                    "  { \"index\": \"test_search\" }, " + //
                    "  { \"query\": { \"match_all\": {} }, \"sort\": [{\"age\": \"asc\"}] }, " + //
                    "  { \"index\": \"test_search\" }, " + //
                    "  { \"query\": { \"match_all\": {} }, \"sort\": [{\"age\": \"asc\"}] } " + //
                    "]";

            boolean hasResult = s.execute(sql);
            if (hasResult) {
                try (ResultSet rs = s.getResultSet()) {
                    if (rs.next()) {
                        Assert.assertEquals("Expected Bob in first msearch result", "Bob", rs.getString("name"));
                    } else {
                        Assert.fail("Expected result for first query");
                    }
                    Assert.assertFalse("Expected only 1 row in first result set", rs.next());
                }
            } else {
                Assert.fail("Expected first result set");
            }

            if (s.getMoreResults()) {
                try (ResultSet rs = s.getResultSet()) {
                    if (rs.next()) {
                        Assert.assertEquals("Expected Bob in second msearch result", "Bob", rs.getString("name"));
                    } else {
                        Assert.fail("Expected result for second query");
                    }
                    Assert.assertFalse("Expected only 1 row in second result set", rs.next());
                }
            } else {
                Assert.fail("Expected second result set");
            }
        }
    }

    @Test
    public void testHeader() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("HEAD /test_search")) {
                if (rs.next()) {
                    int status = rs.getInt("STATUS");
                    Assert.assertEquals("Expected status 200", 200, status);
                } else {
                    Assert.fail("Expected result for HEAD request");
                }
            }

            try (ResultSet rs = s.executeQuery("HEAD /test_search_not_exist")) {
                if (rs.next()) {
                    int status = rs.getInt("STATUS");
                    Assert.assertEquals("Expected status 404", 404, status);
                } else {
                    Assert.fail("Expected result for HEAD request");
                }
            }
        }
    }

    @Test
    public void testMGet() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            s.executeUpdate("POST /test_search/_doc/99?refresh=true { \"name\": \"User99\", \"value\": 99 }");

            try (ResultSet rs = s.executeQuery("POST /test_search/_mget { \"ids\": [\"99\"] }")) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String id = rs.getString("_ID");

                    Assert.assertEquals("Expected name User99", "User99", name);
                    Assert.assertEquals("Expected _id 99", "99", id);
                } else {
                    Assert.fail("Expected 1 result, got 0");
                }
            }
        }
    }

    @Test
    public void testQueryBySource() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            s.executeUpdate("POST /test_search/_doc/99?refresh=true { \"name\": \"User99\", \"value\": 99 }");

            String sql = "GET /test_search/_doc/99/_source";

            try (ResultSet rs = s.executeQuery(sql)) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    int value = rs.getInt("value");
                    Assert.assertEquals("Expected name User99", "User99", name);
                    Assert.assertEquals("Expected value 99", 99, value);
                } else {
                    Assert.fail("Expected 1 result, got 0");
                }
            }
        }
    }

    @Test
    public void testExplain() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            s.executeUpdate("POST /test_search/_doc/99?refresh=true { \"name\": \"User99\", \"value\": 99 }");

            String sql = "POST /test_search/_doc/99/_explain { \"query\": { \"match_all\": {} } }";

            try (ResultSet rs = s.executeQuery(sql)) {
                if (rs.next()) {
                    boolean matched = rs.getBoolean("matched");
                    Assert.assertTrue("Expected matched=true", matched);
                } else {
                    Assert.fail("Expected 1 result, got 0");
                }
            }
        }
    }
}
