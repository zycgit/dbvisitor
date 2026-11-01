/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Elastic6SearchTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://127.0.0.1:2920?indexRefresh=true";

    @BeforeEach
    public void before() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_msearch_1");
            } catch (Exception e) {
                Elastic6Cleanup.requireMissingIndex(e);
            }
            try {
                s.execute("DELETE /test_msearch_2");
            } catch (Exception e) {
                Elastic6Cleanup.requireMissingIndex(e);
            }
            s.executeUpdate("POST /test_msearch_1/_doc/1 { \"name\": \"doc1\", \"value\": 100 }");
            s.executeUpdate("POST /test_msearch_2/_doc/2 { \"name\": \"doc2\", \"value\": 200 }");

            try {
                s.execute("DELETE /test_search");
            } catch (Exception e) {
                Elastic6Cleanup.requireMissingIndex(e);
            }
            s.executeUpdate("POST /test_search/_doc/1 { \"name\": \"Alice\", \"age\": 30, \"city\": \"New York\" }");
            s.executeUpdate("POST /test_search/_doc/2 { \"name\": \"Bob\", \"age\": 25, \"city\": \"Los Angeles\" }");
            s.executeUpdate("POST /test_search/_doc/3 { \"name\": \"Charlie\", \"age\": 35, \"city\": \"New York\" }");
        }
    }

    @AfterEach
    public void after() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_msearch_1");
            } catch (Exception e) {
                Elastic6Cleanup.requireMissingIndex(e);
            }
            try {
                s.execute("DELETE /test_msearch_2");
            } catch (Exception e) {
                Elastic6Cleanup.requireMissingIndex(e);
            }
            try {
                s.execute("DELETE /test_search");
            } catch (Exception e) {
                Elastic6Cleanup.requireMissingIndex(e);
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
                Assertions.assertEquals(3, count, "Expected 3 results");
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
                    Assertions.assertTrue(city.contains("York"), "Unexpected city: " + city);
                }
                Assertions.assertEquals(2, count, "Expected 2 results for 'York'");
            }
        }
    }

    @Test
    public void testSearchPagination() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("POST /test_search/_search { \"from\": 0, \"size\": 1, \"sort\": [{\"age\": \"asc\"}] }")) {
                if (rs.next()) {
                    Assertions.assertEquals("Bob", rs.getString("name"), "Expected Bob as first result");
                } else {
                    Assertions.fail("Expected 1 result");
                }
                Assertions.assertFalse(rs.next(), "Expected only 1 result");
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
                        Assertions.assertEquals(3, count, "Expected count 3");
                    }
                } else {
                    Assertions.fail("No result for count");
                }
            }

            try (ResultSet rs = s.executeQuery("POST /test_search/_count { \"query\": { \"match\": { \"city\": \"York\" } } }")) {
                if (rs.next()) {
                    long count = rs.getLong("COUNT");
                    Assertions.assertEquals(2, count, "Expected count 2 for 'York'");
                } else {
                    Assertions.fail("No result for count");
                }
            }
        }
    }

    @Test
    public void testMultiSearch() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            boolean hasResult = s.execute("""
                    POST /_msearch\s
                    [\s
                        { "index": "test_msearch_1" },\s
                        { "query": { "match_all": {} } },\s
                        { "index": "test_msearch_2" },\s
                        { "query": { "match_all": {} } }\s
                    ]
                    """);

            if (hasResult) {
                try (ResultSet rs = s.getResultSet()) {
                    if (rs.next()) {
                        System.out.println("Result 1 ID: " + rs.getString("_ID"));
                        Assertions.assertEquals("1", rs.getString("_ID"), "First result should be doc 1");
                    } else {
                        Assertions.fail("First result set empty");
                    }
                }

                if (s.getMoreResults()) {
                    try (ResultSet rs = s.getResultSet()) {
                        if (rs.next()) {
                            System.out.println("Result 2 ID: " + rs.getString("_ID"));
                            Assertions.assertEquals("2", rs.getString("_ID"), "Second result should be doc 2");
                        } else {
                            Assertions.fail("Second result set empty");
                        }
                    }
                } else {
                    Assertions.fail("Expected more results");
                }
            } else {
                Assertions.fail("No results returned");
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
                    Assertions.assertEquals("Bob", rs.getString("name"), "Expected Bob as first result");
                } else {
                    Assertions.fail("Expected 1 result");
                }
                Assertions.assertFalse(rs.next(), "Expected only 1 result");
            }

            // Page 2: limit=1, skip=1
            String sql2 = "/*+ overwrite_find_limit=1; overwrite_find_skip=1 */ POST /test_search/_search { \"sort\": [{\"age\": \"asc\"}] }";
            try (ResultSet rs = s.executeQuery(sql2)) {
                if (rs.next()) {
                    Assertions.assertEquals("Alice", rs.getString("name"), "Expected Alice as second result");
                } else {
                    Assertions.fail("Expected 1 result");
                }
                Assertions.assertFalse(rs.next(), "Expected only 1 result");
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

            String sql = """
                    /*+ overwrite_find_limit=1; overwrite_find_skip=0 */ POST /_msearch\s
                    [
                      { "index": "test_search" },
                      { "query": { "match_all": {} }, "sort": [{"age": "asc"}] },
                      { "index": "test_search" },
                      { "query": { "match_all": {} }, "sort": [{"age": "asc"}] }
                    ]
                    """;

            boolean hasResult = s.execute(sql);
            if (hasResult) {
                try (ResultSet rs = s.getResultSet()) {
                    if (rs.next()) {
                        Assertions.assertEquals("Bob", rs.getString("name"), "Expected Bob in first msearch result");
                    } else {
                        Assertions.fail("Expected result for first query");
                    }
                    Assertions.assertFalse(rs.next(), "Expected only 1 row in first result set");
                }
            } else {
                Assertions.fail("Expected first result set");
            }

            if (s.getMoreResults()) {
                try (ResultSet rs = s.getResultSet()) {
                    if (rs.next()) {
                        Assertions.assertEquals("Bob", rs.getString("name"), "Expected Bob in second msearch result");
                    } else {
                        Assertions.fail("Expected result for second query");
                    }
                    Assertions.assertFalse(rs.next(), "Expected only 1 row in second result set");
                }
            } else {
                Assertions.fail("Expected second result set");
            }
        }
    }

    @Test
    public void testHeader() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("HEAD /test_search")) {
                if (rs.next()) {
                    int status = rs.getInt("STATUS");
                    Assertions.assertEquals(200, status, "Expected status 200");
                } else {
                    Assertions.fail("Expected result for HEAD request");
                }
            }

            try (ResultSet rs = s.executeQuery("HEAD /test_search_not_exist")) {
                if (rs.next()) {
                    int status = rs.getInt("STATUS");
                    Assertions.assertEquals(404, status, "Expected status 404");
                } else {
                    Assertions.fail("Expected result for HEAD request");
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

                    Assertions.assertEquals("User99", name, "Expected name User99");
                    Assertions.assertEquals("99", id, "Expected _id 99");
                } else {
                    Assertions.fail("Expected 1 result, got 0");
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
                    Assertions.assertEquals("User99", name, "Expected name User99");
                    Assertions.assertEquals(99, value, "Expected value 99");
                } else {
                    Assertions.fail("Expected 1 result, got 0");
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
                    Assertions.assertTrue(matched, "Expected matched=true");
                } else {
                    Assertions.fail("Expected 1 result, got 0");
                }
            }
        }
    }

    public static void main(String[] args) {
        net.hasor.dbvisitor.test.realdb.RealDbTestRunner.run(Elastic6SearchTest.class);
    }
}
