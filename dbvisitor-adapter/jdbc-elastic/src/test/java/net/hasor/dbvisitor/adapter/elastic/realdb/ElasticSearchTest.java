package net.hasor.dbvisitor.adapter.elastic.realdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Before;
import org.junit.Test;

public class ElasticSearchTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://127.0.0.1:19200";

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

            Thread.sleep(1500); // wait for refresh
        }
    }

    @Test
    public void testSearchMatchAll() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("POST /test_search/_search { \"query\": { \"match_all\": {} } }")) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("Found: " + rs.getString("name"));
                }
                if (count != 3) {
                    throw new Exception("Expected 3 results, got " + count);
                }
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
                    if (!city.contains("York")) {
                        throw new Exception("Unexpected city: " + city);
                    }
                }
                if (count != 2) {
                    throw new Exception("Expected 2 results for 'York', got " + count);
                }
            }
        }
    }

    @Test
    public void testSearchPagination() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("POST /test_search/_search { \"from\": 0, \"size\": 1, \"sort\": [{\"age\": \"asc\"}] }")) {
                if (rs.next()) {
                    if (!"Bob".equals(rs.getString("name"))) {
                        throw new Exception("Expected Bob as first result");
                    }
                } else {
                    throw new Exception("Expected 1 result");
                }
                if (rs.next()) {
                    throw new Exception("Expected only 1 result");
                }
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
                        throw new Exception("Expected count 3, got " + count);
                    }
                } else {
                    throw new Exception("No result for count");
                }
            }

            try (ResultSet rs = s.executeQuery("POST /test_search/_count { \"query\": { \"match\": { \"city\": \"York\" } } }")) {
                if (rs.next()) {
                    long count = rs.getLong("COUNT");
                    if (count != 2) {
                        throw new Exception("Expected count 2 for 'York', got " + count);
                    }
                } else {
                    throw new Exception("No result for count");
                }
            }
        }
    }

    @Test
    public void testMultiSearch() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            boolean hasResult = s.execute("POST /_msearch \n" +     //
                    "[ \n" +                                        //
                    "    { \"index\": \"test_msearch_1\" }, \n" +   //
                    "    { \"query\": { \"match_all\": {} } }, \n" +//
                    "    { \"index\": \"test_msearch_2\" }, \n" +   //
                    "    { \"query\": { \"match_all\": {} } } \n" + //
                    "]");

            if (hasResult) {
                try (ResultSet rs = s.getResultSet()) {
                    if (rs.next()) {
                        System.out.println("Result 1 ID: " + rs.getString("_ID"));
                        if (!"1".equals(rs.getString("_ID"))) {
                            throw new Exception("First result should be doc 1");
                        }
                    } else {
                        throw new Exception("First result set empty");
                    }
                }

                if (s.getMoreResults()) {
                    try (ResultSet rs = s.getResultSet()) {
                        if (rs.next()) {
                            System.out.println("Result 2 ID: " + rs.getString("_ID"));
                            if (!"2".equals(rs.getString("_ID"))) {
                                throw new Exception("Second result should be doc 2");
                            }
                        } else {
                            throw new Exception("Second result set empty");
                        }
                    }
                } else {
                    throw new Exception("Expected more results");
                }
            } else {
                throw new Exception("No results returned");
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
                    if (!"Bob".equals(rs.getString("name"))) {
                        throw new Exception("Expected Bob as first result, but got " + rs.getString("name"));
                    }
                } else {
                    throw new Exception("Expected 1 result");
                }
                if (rs.next()) {
                    throw new Exception("Expected only 1 result");
                }
            }

            // Page 2: limit=1, skip=1
            String sql2 = "/*+ overwrite_find_limit=1; overwrite_find_skip=1 */ POST /test_search/_search { \"sort\": [{\"age\": \"asc\"}] }";
            try (ResultSet rs = s.executeQuery(sql2)) {
                if (rs.next()) {
                    if (!"Alice".equals(rs.getString("name"))) {
                        throw new Exception("Expected Alice as second result, but got " + rs.getString("name"));
                    }
                } else {
                    throw new Exception("Expected 1 result");
                }
                if (rs.next()) {
                    throw new Exception("Expected only 1 result");
                }
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

            String sql = "/*+ overwrite_find_limit=1; overwrite_find_skip=0 */ POST /_msearch \n" +//
                    "[ " +                                                                         //
                    "  { \"index\": \"test_search\" }, " +                                         //
                    "  { \"query\": { \"match_all\": {} }, \"sort\": [{\"age\": \"asc\"}] }, " +   //
                    "  { \"index\": \"test_search\" }, " +                                         //
                    "  { \"query\": { \"match_all\": {} }, \"sort\": [{\"age\": \"asc\"}] } " +    //
                    "]";

            boolean hasResult = s.execute(sql);
            if (hasResult) {
                try (ResultSet rs = s.getResultSet()) {
                    if (rs.next()) {
                        if (!"Bob".equals(rs.getString("name"))) {
                            throw new Exception("Expected Bob in first msearch result, but got " + rs.getString("name"));
                        }
                    } else {
                        throw new Exception("Expected result for first query");
                    }
                    if (rs.next()) {
                        throw new Exception("Expected only 1 row in first result set");
                    }
                }
            } else {
                throw new Exception("Expected first result set");
            }

            if (s.getMoreResults()) {
                try (ResultSet rs = s.getResultSet()) {
                    if (rs.next()) {
                        if (!"Bob".equals(rs.getString("name"))) {
                            throw new Exception("Expected Bob in second msearch result, but got " + rs.getString("name"));
                        }
                    } else {
                        throw new Exception("Expected result for second query");
                    }
                    if (rs.next()) {
                        throw new Exception("Expected only 1 row in second result set");
                    }
                }
            } else {
                throw new Exception("Expected second result set");
            }
        }
    }

    @Test
    public void testHeader() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("HEAD /test_search")) {
                if (rs.next()) {
                    int status = rs.getInt("STATUS");
                    if (status != 200) {
                        throw new Exception("Expected status 200, got " + status);
                    }
                } else {
                    throw new Exception("Expected result for HEAD request");
                }
            }

            try (ResultSet rs = s.executeQuery("HEAD /test_search_not_exist")) {
                if (rs.next()) {
                    int status = rs.getInt("STATUS");
                    if (status != 404) {
                        throw new Exception("Expected status 404, got " + status);
                    }
                } else {
                    throw new Exception("Expected result for HEAD request");
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

                    if (!"User99".equals(name)) {
                        throw new Exception("Expected name User99, got " + name);
                    }
                    if (!"99".equals(id)) {
                        throw new Exception("Expected _id 99, got " + id);
                    }
                } else {
                    throw new Exception("Expected 1 result, got 0");
                }
            }
        }
    }

    @Test
    public void testQueryBySource() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            s.executeUpdate("POST /test_search/_doc/99?refresh=true { \"name\": \"User99\", \"value\": 99 }");

            try (ResultSet rs = s.executeQuery("GET /test_search/_source/99")) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    int value = rs.getInt("value");
                    if (!"User99".equals(name)) {
                        throw new Exception("Expected name User99, got " + name);
                    }
                    if (value != 99) {
                        throw new Exception("Expected value 99, got " + value);
                    }
                } else {
                    throw new Exception("Expected 1 result, got 0");
                }
            }
        }
    }

    @Test
    public void testExplain() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            s.executeUpdate("POST /test_search/_doc/99?refresh=true { \"name\": \"User99\", \"value\": 99 }");

            try (ResultSet rs = s.executeQuery("POST /test_search/_explain/99 { \"query\": { \"match_all\": {} } }")) {
                if (rs.next()) {
                    boolean matched = rs.getBoolean("matched");
                    if (!matched) {
                        throw new Exception("Expected matched=true");
                    }
                } else {
                    throw new Exception("Expected 1 result, got 0");
                }
            }
        }
    }
}
