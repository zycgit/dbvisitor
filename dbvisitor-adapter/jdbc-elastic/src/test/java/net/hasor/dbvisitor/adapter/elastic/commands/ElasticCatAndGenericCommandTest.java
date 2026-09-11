/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic.commands;

import static org.junit.Assert.*;

import java.sql.*;

import org.junit.Test;

public class ElasticCatAndGenericCommandTest extends AbstractElasticCommandTest {
    @Test
    public void catCommandsAddJsonFormatAndMapTheirColumns() throws Exception {
        // @formatter:off
        String[][] cases = {
            { "/_cat/indices", "[{\"index\":\"books\",\"docs.count\":\"12\",\"health\":\"green\"}]", "INDEX", "books", "STORE.SIZE" },
            { "/_cat/nodes?v", "[{\"ip\":\"127.0.0.1\",\"name\":\"node-1\",\"master\":\"*\"}]", "NAME", "node-1", "VERSION" },
            { "/_cat/health?format=json", "[{\"cluster\":\"test\",\"status\":\"green\"}]", "CLUSTER", "test", "NODE.TOTAL" }
        };
        // @formatter:on
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            for (String[] item : cases) {
                respondWith(item[1]);
                try (ResultSet result = statement.executeQuery("GET " + item[0])) {
                    assertTrue(result.next());
                    assertEquals(item[3], result.getString(item[2]));
                    assertNull(result.getString(item[4]));
                    assertFalse(result.next());
                }
            }
        }
        assertRequest(0, "GET", "/_cat/indices?format=json", null);
        assertRequest(1, "GET", "/_cat/nodes?v&format=json", null);
        assertRequest(2, "GET", "/_cat/health?format=json", null);
    }

    @Test
    public void catRejectsNonJsonFormatWithoutCallingSdk() throws Exception {
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            SQLException error = assertThrows(SQLException.class, () -> statement.executeQuery("GET /_cat/indices?format=text"));
            assertTrue(error.getMessage().contains("format=json"));
        }
        assertTrue(requests.isEmpty());
    }

    @Test
    public void catRespectsMaxRows() throws Exception {
        respondWith("[{\"index\":\"books\"},{\"index\":\"archive\"}]");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            statement.setMaxRows(1);
            try (ResultSet result = statement.executeQuery("GET /_cat/indices")) {
                assertTrue(result.next());
                assertEquals("books", result.getString("INDEX"));
                assertFalse(result.next());
            }
        }
    }

    @Test
    public void genericObjectResponseMapsNamedColumns() throws Exception {
        respondWith("{\"cluster_name\":\"test\",\"status\":\"green\",\"number_of_nodes\":2}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("GET /_cluster/health")) {
            assertTrue(result.next());
            assertEquals("test", result.getString("cluster_name"));
            assertEquals(2, result.getInt("number_of_nodes"));
            assertFalse(result.next());
        }
        assertRequest(0, "GET", "/_cluster/health", null);
    }

    @Test
    public void genericArrayUnionsColumnsAndKeepsMissingFieldsNull() throws Exception {
        respondWith("[{\"name\":\"one\"},{\"count\":2},\"plain\"]");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("GET /_cluster/custom")) {
            assertTrue(result.next());
            assertEquals("one", result.getString("name"));
            assertNull(result.getString("count"));
            assertTrue(result.wasNull());
            assertTrue(result.next());
            assertEquals(2, result.getInt("count"));
            assertNull(result.getString("name"));
            assertTrue(result.next());
            assertEquals("plain", result.getString("value"));
            assertFalse(result.next());
        }
    }

    @Test
    public void genericScalarAndEmptyArrayProduceValidResults() throws Exception {
        for (String body : new String[] { "\"ready\"", "[]" }) {
            respondWith(body);
            try (Connection connection = elasticConnection(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("GET /_cluster/custom")) {
                if (!"[]".equals(body)) {
                    assertTrue(result.next());
                    assertEquals("ready", result.getString("value"));
                }
                assertFalse(result.next());
            }
        }
    }

    @Test
    public void headReturnsHttpStatusWithoutReadingResponseBody() throws Exception {
        for (int status : new int[] { 200, 404 }) {
            respondWith(status, "");
            try (Connection connection = elasticConnection(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("HEAD /books")) {
                assertTrue(result.next());
                assertEquals(status, result.getInt("STATUS"));
                assertEquals(Types.INTEGER, result.getMetaData().getColumnType(1));
                assertFalse(result.next());
            }
        }
        assertRequest(0, "HEAD", "/books", null);
    }
}
