/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic.commands;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;

public class ElasticIndexCommandTest extends AbstractElasticCommandTest {
    @Test
    public void mappingSupportsTypedAndTypelessResponses() throws Exception {
        String properties = "{\"title\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"keyword\"}}},\"author\":{\"type\":\"nested\",\"properties\":{\"name\":{\"type\":\"keyword\"}}}}";
        for (boolean typed : new boolean[] { false, true }) {
            String mapping = "{\"properties\":" + properties + "}";
            respondWith("{\"books\":{\"mappings\":" + (typed ? "{\"book\":" + mapping + "}" : mapping) + "}}");
            try (Connection connection = elasticConnection(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("GET /books/_mapping")) {
                assertTrue(result.next());
                assertEquals("books", result.getString("NAME"));
                assertEquals("title", result.getString("MAPPING"));
                assertNull(result.getString("FIELD"));
                assertEquals("text", result.getString("TYPE"));
                assertFalse(result.getBoolean("NESTED"));
                assertTrue(result.next());
                assertEquals("raw", result.getString("FIELD"));
                assertEquals("keyword", result.getString("TYPE"));
                assertTrue(result.next());
                assertEquals("author", result.getString("MAPPING"));
                assertTrue(result.getBoolean("NESTED"));
                assertTrue(json.readTree(result.getString("OPTION")).has("properties"));
                assertFalse(result.next());
            }
        }
        assertRequest(0, "GET", "/books/_mapping", null);
        assertRequest(1, "GET", "/books/_mapping", null);
    }

    @Test
    public void settingsAreFlattenedAndRespectMaxRows() throws Exception {
        respondWith("{\"books\":{\"settings\":{\"index\":{\"number_of_shards\":\"1\",\"analysis\":{\"analyzer\":{\"default\":{\"type\":\"standard\"}}}}}}}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            statement.setMaxRows(1);
            try (ResultSet result = statement.executeQuery("GET /books/_settings")) {
                assertTrue(result.next());
                assertEquals("books", result.getString("NAME"));
                assertEquals("index.number_of_shards", result.getString("SETTING"));
                assertEquals("1", result.getString("VALUE"));
                assertFalse(result.next());
            }
        }
        assertRequest(0, "GET", "/books/_settings", null);
    }

    @Test
    public void aliasesReturnIndexAndAliasRows() throws Exception {
        respondWith("{\"books\":{\"aliases\":{\"current\":{}}},\"archive\":{\"aliases\":{}}}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("GET /_aliases")) {
            assertTrue(result.next());
            assertEquals("books", result.getString("NAME"));
            assertFalse(result.getBoolean("ALIASES"));
            assertNull(result.getString("SOURCE"));
            assertTrue(result.next());
            assertEquals("current", result.getString("NAME"));
            assertEquals("books", result.getString("SOURCE"));
            assertTrue(result.getBoolean("ALIASES"));
            assertTrue(result.next());
            assertEquals("archive", result.getString("NAME"));
            assertFalse(result.next());
        }
        assertRequest(0, "GET", "/_aliases", null);
    }

    @Test
    public void schemaWritesDispatchMethodEndpointAndBody() throws Exception {
        // @formatter:off
        String[][] commands = {
            { "PUT", "/books/_mapping", "{\"properties\":{\"year\":{\"type\":\"integer\"}}}" },
            { "POST", "/books/_mapping", "{\"properties\":{\"tag\":{\"type\":\"keyword\"}}}" },
            { "PUT", "/books/_settings", "{\"index\":{\"number_of_replicas\":0}}" },
            { "POST", "/_aliases", "{\"actions\":[{\"add\":{\"index\":\"books\",\"alias\":\"current\"}}]}" },
            { "POST", "/books/_open", null },
            { "POST", "/books/_close", null },
            { "POST", "/books/_refresh", null },
            { "GET", "/books/_refresh", null }
        };
        // @formatter:on
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            for (int i = 0; i < commands.length; i++) {
                String[] command = commands[i];
                respondWith("{\"acknowledged\":true}");
                String sql = command[0] + " " + command[1] + (command[2] == null ? "" : " " + command[2]);
                assertEquals(1, statement.executeUpdate(sql));
                assertRequest(i, command[0], command[1], command[2]);
            }
        }
    }

    @Test
    public void reindexReturnsCountOrAsyncTaskDocument() throws Exception {
        respondWith("{\"total\":12}");
        respondWith("{\"task\":\"node:42\"}");
        String body = "{\"source\":{\"index\":\"books\"},\"dest\":{\"index\":\"archive\"}}";
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            assertEquals(12, statement.executeUpdate("POST /_reindex " + body));
            try (ResultSet result = statement.executeQuery("POST /_reindex?wait_for_completion=false " + body)) {
                assertTrue(result.next());
                assertEquals("node:42", json.readTree(result.getString("_DOC")).get("task").asText());
                assertFalse(result.next());
            }
        }
        assertRequest(0, "POST", "/_reindex", body);
        assertRequest(1, "POST", "/_reindex?wait_for_completion=false", body);
    }

    @Test
    public void emptyMappingAndSettingsKeepTheirResultMetadata() throws Exception {
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            for (String suffix : new String[] { "_mapping", "_settings" }) {
                respondWith("{\"books\":{}}");
                try (ResultSet result = statement.executeQuery("GET /books/" + suffix)) {
                    assertEquals("NAME", result.getMetaData().getColumnLabel(1));
                    assertFalse(result.next());
                }
            }
        }
    }
}
