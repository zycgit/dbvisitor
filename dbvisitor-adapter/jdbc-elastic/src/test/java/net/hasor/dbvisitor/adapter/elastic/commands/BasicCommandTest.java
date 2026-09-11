/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.sql.*;
import java.util.Collections;

import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.junit.Test;

import net.hasor.dbvisitor.driver.JdbcErrorCode;

public class BasicCommandTest extends AbstractElasticCommandTest {
    @Test
    public void connectionReadsServerVersionAndClosesClientOnce() throws Exception {
        Connection connection = elasticConnection();
        RestClient client = connection.unwrap(RestClient.class);
        assertEquals("7.17.10", connection.getMetaData().getDatabaseProductVersion());
        assertEquals(7, connection.getMetaData().getDatabaseMajorVersion());
        assertEquals(17, connection.getMetaData().getDatabaseMinorVersion());
        connection.close();
        connection.close();
        verify(client, times(1)).close();
        assertTrue(requests.isEmpty());
    }

    @Test
    public void missingParameterFailsBeforeSdkRequest() throws Exception {
        try (Connection connection = elasticConnection(); PreparedStatement statement = connection.prepareStatement("POST /books/_search {\"query\":?}")) {
            SQLException error = assertThrows(SQLException.class, statement::executeQuery);
            assertTrue(error.getMessage().contains("param size not match"));
            assertTrue(requests.isEmpty());
        }
    }

    @Test
    public void emptyAndMalformedCommandsFailBeforeSdkRequest() throws Exception {
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            SQLException empty = assertThrows(SQLException.class, () -> statement.execute(" "));
            assertEquals(JdbcErrorCode.SQL_STATE_QUERY_EMPTY, empty.getSQLState());
            SQLException syntax = assertThrows(SQLException.class, () -> statement.execute("POST /books/_search {\"query\":"));
            assertEquals(JdbcErrorCode.SQL_STATE_SYNTAX_ERROR, syntax.getSQLState());
            assertTrue(requests.isEmpty());
        }
    }

    @Test
    public void preparedBodyPreservesTypedValuesAndInjectionText() throws Exception {
        String attack = "\"},\"admin\":true,\"x\":\"\\\n中文";
        respondWith("{\"_id\":\"doc-1\"}");
        try (Connection connection = elasticConnection(); PreparedStatement statement = connection.prepareStatement("POST /{?}/_doc/{?} {?:?,\"active\":?,\"age\":?,\"extra\":?,\"empty\":?}")) {
            statement.setString(1, "books");
            statement.setString(2, "doc-1");
            statement.setString(3, "name");
            statement.setString(4, attack);
            statement.setBoolean(5, true);
            statement.setInt(6, 12);
            statement.setObject(7, Collections.singletonMap("tags", new String[] { "java", "jdbc" }));
            statement.setObject(8, null);
            assertEquals(1, statement.executeUpdate());
        }
        assertEquals("/books/_doc/doc-1", requests.get(0).getEndpoint());
        assertEquals(attack, requestBody(0).get("name").asText());
        assertTrue(requestBody(0).get("active").isBoolean());
        assertTrue(requestBody(0).get("age").isIntegralNumber());
        assertEquals("jdbc", requestBody(0).at("/extra/tags/1").asText());
        assertTrue(requestBody(0).get("empty").isNull());
        assertFalse(requestBody(0).has("admin"));
    }

    @Test
    public void multiStatementExecutionKeepsParameterOffsetsAndJdbcResults() throws Exception {
        try (Connection connection = elasticConnection(); PreparedStatement statement = connection.prepareStatement("GET /books/_count {\"query\":?}; POST /books/_doc {\"name\":?}; GET /books/_count {\"query\":?}")) {
            for (int repeat = 0; repeat < 2; repeat++) {
                respondWith("{\"count\":2}");
                respondWith("{\"_id\":\"new-id\"}");
                respondWith("{\"count\":3}");
                statement.setObject(1, Collections.singletonMap("term", Collections.singletonMap("year", 2020 + repeat)));
                statement.setString(2, "book-" + repeat);
                statement.setObject(3, Collections.singletonMap("match_all", Collections.emptyMap()));
                assertTrue(statement.execute());
                try (ResultSet first = statement.getResultSet()) {
                    assertTrue(first.next());
                    assertEquals(2, first.getLong("COUNT"));
                    assertFalse(first.next());
                }
                assertFalse(statement.getMoreResults());
                assertEquals(1, statement.getUpdateCount());
                assertTrue(statement.getMoreResults());
                try (ResultSet last = statement.getResultSet()) {
                    assertTrue(last.next());
                    assertEquals(3, last.getLong(1));
                }
                assertFalse(statement.getMoreResults());
                assertEquals(-1, statement.getUpdateCount());
                assertEquals(2020 + repeat, requestBody(repeat * 3).at("/query/term/year").asInt());
                assertEquals("book-" + repeat, requestBody(repeat * 3 + 1).get("name").asText());
            }
        }
        assertEquals(6, requests.size());
    }

    @Test
    public void serverErrorsExposeReasonSqlStateAndCause() throws Exception {
        for (String body : new String[] { "{\"error\":\"denied\"}", "{\"error\":{\"reason\":\"denied\"}}" }) {
            ResponseException failure = new ResponseException(response(403, body));
            failWith(failure);
            try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
                SQLException error = assertThrows(SQLException.class, () -> statement.executeQuery("GET /_cluster/health"));
                assertEquals("E403", error.getSQLState());
                assertEquals("denied", error.getMessage());
                assertSame(failure, error.getCause());
            }
        }
    }

    @Test
    public void transportFailureStopsFollowingCommandsAndStatementCanBeReused() throws Exception {
        IOException failure = new IOException("connection lost");
        failWith(failure);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            SQLException error = assertThrows(SQLException.class, () -> statement.execute("GET /books/_count; DELETE /books"));
            assertSame(failure, error.getCause());
            assertEquals(1, requests.size());
            respondWith("{\"count\":4}");
            try (ResultSet result = statement.executeQuery("GET /books/_count")) {
                assertTrue(result.next());
                assertEquals(4, result.getLong(1));
            }
        }
    }
}
