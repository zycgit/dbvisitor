/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic.commands;

import java.nio.charset.StandardCharsets;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import net.hasor.dbvisitor.adapter.elastic.ElasticKeys;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElasticBulkCommandTest extends AbstractElasticCommandTest {
    @Test
    public void bulkSerializesBoundDocumentsAsNdjsonAndReturnsKeys() throws Exception {
        respondWith("""
                {"errors": false, "items": [
                  {"index": {"status": 201, "result": "created", "_id": "1"}},
                  {"create": {"status": 201, "result": "created", "_id": "2"}}]}
                """);
        String value = "中文 café 😀 quote\"\n{\"delete\":{\"_id\":\"victim\"}}";
        try (Connection connection = elasticConnection(); PreparedStatement statement = connection.prepareStatement("""
                POST /books/_bulk?refresh=true [
                  {"index": {"_id": ?}}, {"name": ?},
                  {"create": {}}, {"name": ?}]
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, "1");
            statement.setString(2, value);
            statement.setObject(3, "second");
            assertEquals(2, statement.executeUpdate());
            try (ResultSet keys = statement.getGeneratedKeys()) {
                assertTrue(keys.next());
                assertEquals("1", keys.getString("_ID"));
                assertTrue(keys.next());
                assertEquals("2", keys.getString("_ID"));
                assertFalse(keys.next());
            }
        }
        assertEquals("/books/_bulk?refresh=true", requests.get(0).getEndpoint());
        byte[] bodyBytes = EntityUtils.toByteArray(requests.get(0).getEntity());
        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        assertTrue(body.endsWith("\n"));
        String[] lines = body.split("\n");
        assertEquals(4, lines.length);
        assertEquals(value, json.readTree(lines[1]).get("name").asText());
        assertEquals("application/x-ndjson", requests.get(0).getEntity().getContentType().getValue());
        assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), bodyBytes);
    }

    @Test
    public void mixedActionsCountActualChangesIncludingMissingDelete() throws Exception {
        respondWith("""
                {"errors": false, "items": [
                  {"index": {"status": 200, "result": "updated"}},
                  {"update": {"status": 200, "result": "noop"}},
                  {"delete": {"status": 404, "result": "not_found"}},
                  {"delete": {"status": 200, "result": "deleted"}}]}
                """);
        Properties props = new Properties();
        props.setProperty(ElasticKeys.INDEX_REFRESH, "true");
        try (Connection connection = elasticConnection(props); Statement statement = connection.createStatement()) {
            assertEquals(2, statement.executeUpdate("""
                    PUT /_bulk [
                      {"index": {"_index": "books", "_id": "1"}}, {"name": "first"},
                      {"update": {"_index": "books", "_id": "1"}}, {"doc": {"name": "first"}},
                      {"delete": {"_index": "books", "_id": "missing"}},
                      {"delete": {"_index": "books", "_id": "1"}}]
                    """));
        }
        assertEquals("/_bulk?refresh=true", requests.get(0).getEndpoint());
        assertEquals(6, EntityUtils.toString(requests.get(0).getEntity()).split("\n").length);
    }

    @Test
    public void partialFailureReportsEveryActionAndErrorDetails() throws Exception {
        respondWith("""
                {"errors": true, "items": [
                  {"create": {"status": 201, "result": "created", "_id": "1"}},
                  {"create": {"status": 409, "_id": "1", "error": {"type": "version_conflict_engine_exception", "reason": "already exists"}}},
                  {"update": {"status": 200, "result": "noop"}},
                  {"index": {"status": 400, "error": {"type": "mapper_parsing_exception", "reason": "invalid age"}}}]}
                """);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            try {
                statement.executeUpdate("""
                        POST /books/_bulk [
                          {"create": {"_id": "1"}}, {"name": "first"},
                          {"create": {"_id": "1"}}, {"name": "duplicate"},
                          {"update": {"_id": "1"}}, {"doc": {"name": "first"}},
                          {"index": {}}, {"age": "bad"}]
                        """);
                fail("Partial failure must not be reported as success");
            } catch (BatchUpdateException expected) {
                assertArrayEquals(new int[] { 1, Statement.EXECUTE_FAILED, 0, Statement.EXECUTE_FAILED }, expected.getUpdateCounts());
                assertEquals("23505", expected.getSQLState());
                assertTrue(expected.getNextException().getMessage().contains("already exists"));
                assertTrue(expected.getNextException().getNextException().getMessage().contains("invalid age"));
            }
        }
    }

    @Test
    public void malformedBulkNeverReachesSdk() throws Exception {
        String[] bodies = { "[]", "{}", "[{\"index\": {}}]", "[{\"unknown\": {}}]", "[{\"delete\": 1}]" };
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            for (String body : bodies) {
                try {
                    statement.executeUpdate("POST /books/_bulk " + body);
                    fail("Invalid bulk body must be rejected: " + body);
                } catch (SQLException expected) {
                    assertNotNull(expected.getMessage());
                }
            }
        }
        assertTrue(requests.isEmpty());
    }

    @Test
    public void topLevelErrorsDoNotInventFailedItemCounts() throws Exception {
        respondWith("{\"errors\": true, \"items\": [{\"delete\": {\"status\": 200, \"result\": \"deleted\"}}]}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            try {
                statement.executeUpdate("POST /books/_bulk [{\"delete\": {\"_id\": \"1\"}}]");
                fail("Inconsistent response must be reported");
            } catch (BatchUpdateException expected) {
                assertArrayEquals(new int[] { 1 }, expected.getUpdateCounts());
                assertTrue(expected.getNextException().getMessage().contains("without item failure details"));
            }
        }
    }

    @Test
    public void itemFailureIsNotHiddenByFalseTopLevelFlag() throws Exception {
        respondWith("{\"errors\": false, \"items\": [{\"delete\": {\"status\": 403, \"error\": {\"reason\": \"forbidden\"}}}]}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            try {
                statement.executeUpdate("POST /books/_bulk [{\"delete\": {\"_id\": \"1\"}}]");
                fail("Item errors must be checked independently");
            } catch (BatchUpdateException expected) {
                assertArrayEquals(new int[] { Statement.EXECUTE_FAILED }, expected.getUpdateCounts());
                assertEquals(403, expected.getNextException().getErrorCode());
            }
        }
    }

    @Test
    public void successWithoutRefreshUsesActualItemResult() throws Exception {
        respondWith("{\"errors\": false, \"items\": [{\"delete\": {\"status\": 200, \"result\": \"deleted\"}}]}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            assertEquals(1, statement.executeUpdate("POST /books/_bulk [{\"delete\": {\"_id\": \"1\"}}]"));
        }
        assertEquals("/books/_bulk", requests.get(0).getEndpoint());
    }

    @Test
    public void truncatedResponseCannotBecomeACompleteSuccess() throws Exception {
        respondWith("{\"errors\": false, \"items\": []}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            try {
                statement.executeUpdate("POST /books/_bulk [{\"delete\": {\"_id\": \"1\"}}]");
                fail("Missing response items must be reported");
            } catch (SQLException expected) {
                assertTrue(expected.getMessage().contains("incomplete"));
            }
        }
    }
}
