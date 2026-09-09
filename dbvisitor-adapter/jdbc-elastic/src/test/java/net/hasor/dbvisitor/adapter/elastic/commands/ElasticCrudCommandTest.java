package net.hasor.dbvisitor.adapter.elastic.commands;

import java.sql.*;
import java.util.Properties;
import net.hasor.dbvisitor.adapter.elastic.ElasticKeys;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElasticCrudCommandTest extends AbstractElasticCommandTest {
    @Test
    public void insertReturnsGeneratedKeyAndKeepsBoundDocument() throws Exception {
        respondWith(201, "{\"_id\":\"generated-42\",\"result\":\"created\"}");
        try (Connection connection = elasticConnection(); PreparedStatement statement = connection.prepareStatement("POST /books/_doc {\"name\":?}", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, "Java");
            assertEquals(1, statement.executeUpdate());
            try (ResultSet keys = statement.getGeneratedKeys()) {
                assertTrue(keys.next());
                assertEquals("generated-42", keys.getString("_ID"));
                assertFalse(keys.next());
            }
        }
        assertRequest(0, "POST", "/books/_doc", "{\"name\":\"Java\"}");
    }

    @Test
    public void generatedKeysCanBeDisabled() throws Exception {
        respondWith("{\"_id\":\"ignored\"}");
        try (Connection connection = elasticConnection(); PreparedStatement statement = connection.prepareStatement("PUT /books/_doc/42 {\"name\":\"Java\"}", Statement.NO_GENERATED_KEYS)) {
            assertEquals(1, statement.executeUpdate());
            try (ResultSet keys = statement.getGeneratedKeys()) {
                assertFalse(keys.next());
            }
        }
        assertRequest(0, "PUT", "/books/_doc/42", "{\"name\":\"Java\"}");
    }

    @Test
    public void refreshIsAddedForDocumentsButNotIndexCreation() throws Exception {
        Properties settings = new Properties();
        settings.setProperty(ElasticKeys.INDEX_REFRESH, "true");
        String[] commands = {
                "POST /books/_doc {\"name\":\"new\"}",
                "PUT /books/_create/42?refresh=false {\"name\":\"explicit\"}",
                "PUT /books {\"settings\":{\"number_of_shards\":1}}",
                "PUT /books/book/43 {\"name\":\"typed\"}"
        };
        try (Connection connection = elasticConnection(settings); Statement statement = connection.createStatement()) {
            for (String command : commands) {
                respondWith("{\"_id\":\"42\",\"acknowledged\":true}");
                assertEquals(1, statement.executeUpdate(command));
            }
        }
        assertEquals("/books/_doc?refresh=true", requests.get(0).getEndpoint());
        assertEquals("/books/_create/42?refresh=false", requests.get(1).getEndpoint());
        assertEquals("/books", requests.get(2).getEndpoint());
        assertEquals("/books/book/43?refresh=true", requests.get(3).getEndpoint());
    }

    @Test
    public void updateMapsUpdatedCreatedAndNoopResponses() throws Exception {
        String[] results = { "updated", "created", "noop" };
        int[] counts = { 1, 1, 0 };
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            for (int i = 0; i < results.length; i++) {
                respondWith("{\"result\":\"" + results[i] + "\"}");
                assertEquals(counts[i], statement.executeUpdate("POST /books/_update/42?refresh=true {\"doc\":{\"name\":\"changed\"}}"));
                assertRequest(i, "POST", "/books/_update/42?refresh=true", "{\"doc\":{\"name\":\"changed\"}}");
            }
        }
    }

    @Test
    public void updateAndDeleteByQueryReturnCountsWithRefresh() throws Exception {
        Properties settings = new Properties();
        settings.setProperty(ElasticKeys.INDEX_REFRESH, "true");
        respondWith("{\"updated\":5}");
        respondWith("{\"deleted\":3}");
        try (Connection connection = elasticConnection(settings); Statement statement = connection.createStatement()) {
            assertEquals(5, statement.executeUpdate("POST /books/_update_by_query {\"query\":{\"match_all\":{}},\"script\":{\"source\":\"ctx._source.active=true\"}}"));
            assertEquals(3, statement.executeUpdate("POST /books/_delete_by_query?conflicts=proceed {\"query\":{\"term\":{\"active\":false}}}"));
        }
        assertRequest(0, "POST", "/books/_update_by_query?refresh=true", "{\"query\":{\"match_all\":{}},\"script\":{\"source\":\"ctx._source.active=true\"}}");
        assertRequest(1, "POST", "/books/_delete_by_query?conflicts=proceed&refresh=true", "{\"query\":{\"term\":{\"active\":false}}}");
    }

    @Test
    public void documentDeleteDistinguishesDeletedAndMissing() throws Exception {
        respondWith("{\"result\":\"deleted\"}");
        respondWith("{\"result\":\"not_found\"}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            assertEquals(1, statement.executeUpdate("DELETE /books/_doc/42?refresh=true"));
            assertEquals(0, statement.executeUpdate("DELETE /books/_doc/43?refresh=true"));
        }
        assertRequest(0, "DELETE", "/books/_doc/42?refresh=true", null);
        assertRequest(1, "DELETE", "/books/_doc/43?refresh=true", null);
    }

    @Test
    public void writesWithoutRefreshKeepUnknownCountContract() throws Exception {
        String[] commands = {
                "POST /books/_update/42 {\"doc\":{\"active\":true}}",
                "POST /books/_update_by_query {\"query\":{\"match_all\":{}}}",
                "POST /books/_delete_by_query {\"query\":{\"match_all\":{}}}",
                "DELETE /books/_doc/42"
        };
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            for (String command : commands) {
                respondWith("{\"result\":\"updated\",\"updated\":4,\"deleted\":2}");
                assertEquals(Statement.SUCCESS_NO_INFO, statement.executeUpdate(command));
            }
        }
        assertEquals(4, requests.size());
    }
}
