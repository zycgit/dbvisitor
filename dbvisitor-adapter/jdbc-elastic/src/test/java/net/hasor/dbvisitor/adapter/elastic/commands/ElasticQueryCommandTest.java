package net.hasor.dbvisitor.adapter.elastic.commands;

import static org.junit.Assert.*;

import java.sql.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import net.hasor.dbvisitor.adapter.elastic.ElasticKeys;

@RunWith(Parameterized.class)
public class ElasticQueryCommandTest extends AbstractElasticCommandTest {
    private static final String HITS = """
            {"took":1,"hits":{"total":{"value":2,"relation":"eq"},"hits":[\
            {"_id":"1","_source":{"title":"Java","year":2026,"tags":["jdbc"],"author":{"name":"Alice"}}},\
            {"_id":"2","_source":{"title":"SQL","active":true}}]}}\
            """;
    private final boolean       preRead;

    @Rule
    public TemporaryFolder cache = new TemporaryFolder();

    @Parameterized.Parameters(name = "preRead={0}")
    public static Collection<Object[]> modes() {
        return Arrays.asList(new Object[] { true }, new Object[] { false });
    }

    public ElasticQueryCommandTest(boolean preRead) {
        this.preRead = preRead;
    }

    private Properties settings() {
        Properties settings = new Properties();
        settings.setProperty(ElasticKeys.PREREAD_ENABLED, Boolean.toString(preRead));
        settings.setProperty(ElasticKeys.PREREAD_CACHE_DIR, cache.getRoot().getAbsolutePath());
        return settings;
    }

    @Test
    public void searchMapsDocumentsAndRespectsMaxRows() throws Exception {
        respondWith(HITS);
        try (Connection connection = elasticConnection(settings()); Statement statement = connection.createStatement()) {
            statement.setMaxRows(1);
            try (ResultSet result = statement.executeQuery("POST /books/_search {\"query\":{\"match_all\":{}}}")) {
                assertTrue(result.next());
                assertEquals("1", result.getString("_ID"));
                assertEquals("Alice", json.readTree(result.getString("_DOC")).at("/author/name").asText());
                if (preRead) {
                    assertEquals(2026, result.getInt("year"));
                    assertEquals("[\"jdbc\"]", result.getString("tags"));
                } else {
                    assertEquals(2, result.getMetaData().getColumnCount());
                }
                assertFalse(result.next());
            }
        }
        assertRequest(0, "POST", "/books/_search", "{\"query\":{\"match_all\":{}}}");
    }

    @Test
    public void searchUnionsFieldsAcrossRowsWhenPreReading() throws Exception {
        respondWith(HITS);
        try (Connection connection = elasticConnection(settings()); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("GET /books/_search")) {
            assertTrue(result.next());
            if (preRead) {
                assertNull(result.getString("active"));
                assertTrue(result.wasNull());
            }
            assertTrue(result.next());
            assertEquals("2", result.getString("_ID"));
            if (preRead) {
                assertEquals("true", result.getString("active"));
                assertNull(result.getString("year"));
            }
            assertFalse(result.next());
        }
    }

    @Test
    public void emptyOrMissingHitsStillReturnAnEmptyResultSet() throws Exception {
        for (String body : new String[] { "{\"hits\":{\"hits\":[]}}", "{\"took\":0}", "{\"hits\":{\"total\":0}}" }) {
            respondWith(body);
            try (Connection connection = elasticConnection(settings()); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("GET /books/_search")) {
                assertEquals("_ID", result.getMetaData().getColumnLabel(1));
                assertEquals("_DOC", result.getMetaData().getColumnLabel(2));
                assertFalse(result.next());
            }
        }
    }

    @Test
    public void pagingHintsBindBeforePathAndBodyParameters() throws Exception {
        respondWith("{\"hits\":{\"hits\":[]}}");
        String sql = "/*+ overwrite_find_limit=?, overwrite_find_skip=? */ POST /{?}/_search?routing={?} {\"query\":?,\"size\":99,\"from\":88}";
        try (Connection connection = elasticConnection(settings()); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, 2);
            statement.setInt(2, 4);
            statement.setString(3, "books");
            statement.setString(4, "tenant-1");
            statement.setObject(5, Collections.singletonMap("match_all", Collections.emptyMap()));
            try (ResultSet result = statement.executeQuery()) {
                assertFalse(result.next());
            }
        }
        assertRequest(0, "POST", "/books/_search?routing=tenant-1", "{\"query\":{\"match_all\":{}},\"size\":2,\"from\":4}");
    }

    @Test
    public void countHintRewritesEndpointAndKeepsLongCounts() throws Exception {
        respondWith("{\"count\":3000000000}");
        try (Connection connection = elasticConnection(settings()); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("/*+ overwrite_find_as_count */ POST /books/_search {\"query\":{\"match_all\":{}}}")) {
            assertTrue(result.next());
            assertEquals(3000000000L, result.getLong("COUNT"));
            assertEquals(Types.BIGINT, result.getMetaData().getColumnType(1));
            assertFalse(result.next());
        }
        assertRequest(0, "POST", "/books/_count", "{\"query\":{\"match_all\":{}}}");
    }

    @Test(timeout = 5000)
    public void multiSearchUsesNdjsonAndJdbcMultipleResultsIncludingEmptyGroups() throws Exception {
        respondWith("{\"responses\":[" + HITS + ",{\"hits\":{\"hits\":[]}},{\"hits\":{\"hits\":[{\"_id\":\"3\",\"_source\":{\"title\":\"Third\"}}]}}]}");
        String sql = "/*+ overwrite_find_limit=2, overwrite_find_skip=1 */ POST /books/_msearch [{},{\"query\":{\"match_all\":{}}},{},{\"query\":{\"term\":{\"active\":true}}},{},{}]";
        try (Connection connection = elasticConnection(settings()); Statement statement = connection.createStatement()) {
            assertTrue(statement.execute(sql));
            try (ResultSet first = statement.getResultSet()) {
                assertTrue(first.next());
                assertEquals("1", first.getString("_ID"));
                assertTrue(first.next());
                assertEquals("2", first.getString("_ID"));
                assertFalse(first.next());
            }
            assertTrue(statement.getMoreResults());
            try (ResultSet empty = statement.getResultSet()) {
                assertFalse(empty.next());
            }
            assertTrue(statement.getMoreResults());
            try (ResultSet last = statement.getResultSet()) {
                assertTrue(last.next());
                assertEquals("3", last.getString("_ID"));
                assertFalse(last.next());
            }
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
        assertEquals("POST", requests.get(0).getMethod());
        assertEquals("/books/_msearch", requests.get(0).getEndpoint());
        String body = EntityUtils.toString(requests.get(0).getEntity());
        assertTrue(body.endsWith("\n"));
        String[] lines = body.split("\n");
        assertEquals(6, lines.length);
        for (int index = 1; index < lines.length; index += 2) {
            assertEquals(2, json.readTree(lines[index]).get("size").asInt());
            assertEquals(1, json.readTree(lines[index]).get("from").asInt());
        }
    }

    @Test
    public void multiGetMapsDocumentsAndEmptyResults() throws Exception {
        for (String body : new String[] { "{\"docs\":[{\"_id\":\"1\",\"_source\":{\"title\":\"Java\"}}]}", "{\"docs\":[]}" }) {
            respondWith(body);
            try (Connection connection = elasticConnection(settings()); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("POST /books/_mget {\"ids\":[\"1\"]}")) {
                if (body.contains("_source")) {
                    assertTrue(result.next());
                    assertEquals("1", result.getString("_ID"));
                    assertEquals("Java", json.readTree(result.getString("_DOC")).get("title").asText());
                }
                assertFalse(result.next());
            }
        }
        assertRequest(0, "POST", "/books/_mget", "{\"ids\":[\"1\"]}");
    }

    @Test
    public void sourceMapsNestedAndNullValues() throws Exception {
        respondWith("{\"title\":\"Java\",\"tags\":[\"jdbc\"],\"optional\":null}");
        try (Connection connection = elasticConnection(settings()); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("GET /books/_source/1")) {
            assertTrue(result.next());
            assertNull(result.getString("_ID"));
            assertEquals("Java", json.readTree(result.getString("_DOC")).get("title").asText());
            if (preRead) {
                assertEquals("[\"jdbc\"]", result.getString("tags"));
                assertNull(result.getString("optional"));
            }
            assertFalse(result.next());
        }
        assertRequest(0, "GET", "/books/_source/1", null);
    }

    @Test
    public void explainPreservesItsFieldsAndRequestBody() throws Exception {
        respondWith("{\"_id\":\"1\",\"matched\":true,\"explanation\":{\"value\":1.5}}");
        try (Connection connection = elasticConnection(settings()); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("POST /books/_explain/1 {\"query\":{\"match_all\":{}}}")) {
            assertTrue(result.next());
            assertEquals("1", result.getString("_id"));
            assertEquals("true", result.getString("matched"));
            assertTrue(result.getString("explanation").contains("1.5"));
            assertFalse(result.next());
        }
        assertRequest(0, "POST", "/books/_explain/1", "{\"query\":{\"match_all\":{}}}");
    }

    @Test
    public void spillingResultsCleansTemporaryFiles() throws Exception {
        Properties settings = settings();
        settings.setProperty(ElasticKeys.PREREAD_THRESHOLD, "1B");
        settings.setProperty(ElasticKeys.PREREAD_MAX_FILE_SIZE, "1MB");
        respondWith(HITS);
        try (Connection connection = elasticConnection(settings); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("GET /books/_search")) {
            assertTrue(result.next());
            assertEquals("1", result.getString("_ID"));
            assertTrue(result.next());
            assertEquals("2", result.getString("_ID"));
            assertFalse(result.next());
        }
        assertArrayEquals(new String[0], cache.getRoot().list());
    }
}
