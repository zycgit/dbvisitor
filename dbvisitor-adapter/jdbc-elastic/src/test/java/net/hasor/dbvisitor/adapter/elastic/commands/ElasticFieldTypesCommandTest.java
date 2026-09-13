/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic.commands;

import java.sql.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.elasticsearch.client.ResponseException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElasticFieldTypesCommandTest extends AbstractElasticCommandTest {
    @Test
    public void mappedBinaryDecodesButOrdinaryStringAndLongDoNot() throws Exception {
        respondWith("""
                {"books": {"mappings": {"properties": {
                  "bytes": {"type": "binary"}, "text": {"type": "keyword"}, "number": {"type": "long"}}}}}
                """);
        respondWith("""
                {"hits": {"hits": [
                  {"_source": {"bytes": "AAEC", "text": "AAEC", "number": 1710432000000}},
                  {"_source": {"bytes": null, "text": "AAAA", "number": 1}}]}}
                """);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("POST /books/_search {\"_source\": [\"bytes\",\"text\",\"number\"]}")) {
            assertTrue(result.next());
            assertArrayEquals(new byte[] { 0, 1, 2 }, result.getBytes("bytes"));
            assertEquals("AAEC", result.getObject("text"));
            assertEquals(1710432000000L, result.getObject("number"));
            assertTrue(result.next());
            assertNull(result.getBytes("bytes"));
            assertTrue(result.wasNull());
        }
        assertEquals(2, requests.size());
        assertRequest(0, "GET", "/books/_mapping", null);
    }

    @Test
    public void sourceArraysAndObjectsKeepStructureAndJsonStrings() throws Exception {
        respondWith("{\"books\": {\"mappings\": {\"properties\": {}}}}");
        respondWith("{\"hits\": {\"hits\": [{\"_source\": {\"array\": [\"a\",null,\"b\"], \"object\": {\"key\": \"a\\\"b\"}, \"empty\": []}}]}}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("POST /books/_search {\"_source\": [\"array\",\"object\",\"empty\"]}")) {
            assertTrue(result.next());
            assertTrue(result.getObject("array") instanceof List);
            assertTrue(result.getObject("object") instanceof Map);
            assertEquals(json.readTree("[\"a\",null,\"b\"]"), json.readTree(result.getString("array")));
            assertEquals("a\"b", json.readTree(result.getString("object")).get("key").asText());
            assertArrayEquals(new Object[] { "a", null, "b" }, (Object[]) result.getArray("array").getArray());
            assertEquals(0, ((Object[]) result.getArray("empty").getArray()).length);
        }
    }

    @Test
    public void timeOnlyTextUsesJdbcTimeWithoutInventingADate() throws Exception {
        respondWith("{\"_id\": \"1\", \"result\": \"created\"}");
        respondWith("{\"books\": {\"mappings\": {\"properties\": {\"time\": {\"type\": \"keyword\"}}}}}");
        respondWith("{\"hits\": {\"hits\": [{\"_source\": {\"time\": \"14:30:45\"}}]}}");
        try (Connection connection = elasticConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("POST /books/_doc {\"time\": ?}")) {
                statement.setTime(1, Time.valueOf("14:30:45"));
                assertEquals(1, statement.executeUpdate());
            }
            try (Statement statement = connection.createStatement();
                    ResultSet result = statement.executeQuery("POST /books/_search {\"_source\": [\"time\"]}")) {
                assertTrue(result.next());
                assertEquals("14:30:45", result.getString(1));
                assertEquals(Time.valueOf("14:30:45"), result.getTime(1));
            }
        }
        assertEquals("14:30:45", requestBody(0).get("time").asText());
        assertFalse(requestBody(2).has("docvalue_fields"));
    }

    @Test
    public void numericArraysUseDeclaredElementTypes() throws Exception {
        respondWith("{\"books\": {\"mappings\": {\"properties\": {\"values\": {\"type\": \"float\"}, \"empty\": {\"type\": \"integer\"}}}}}");
        respondWith("{\"hits\": {\"hits\": [{\"_source\": {\"values\": [1.1,2.2], \"empty\": []}}]}}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("POST /books/_search {\"_source\": [\"values\",\"empty\"]}")) {
            assertTrue(result.next());
            assertEquals(Float.valueOf(1.1f), ((List<?>) result.getObject("values")).get(0));
            assertEquals("Float", result.getArray("values").getBaseTypeName());
            assertEquals("Int", result.getArray("empty").getBaseTypeName());
            assertEquals(Types.ARRAY, result.getMetaData().getColumnType(1));
        }
    }

    @Test
    public void es6DateNormalizesCustomFormatWithoutChangingSource() throws Exception {
        respondWith("{\"books\": {\"mappings\": {\"book\": {\"properties\": {\"created\": {\"type\": \"date\", \"format\": \"dd/MM/yyyy\"}}}}}}");
        respondWith("{\"hits\": {\"hits\": [{\"_source\": {\"created\": \"15/03/2024\"}, \"fields\": {\"created\": [\"2024-03-15T00:00:00.000Z\"]}}]}}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("POST /books/book/_search {\"_source\": [\"created\",\"_DOC\"]}")) {
            assertTrue(result.next());
            assertEquals(Timestamp.from(Instant.parse("2024-03-15T00:00:00Z")), result.getTimestamp("created"));
            assertEquals(Types.TIMESTAMP, result.getMetaData().getColumnType(1));
            assertEquals("15/03/2024", json.readTree(result.getString("_DOC")).get("created").asText());
        }
        assertRequest(0, "GET", "/books/_mapping", null);
        assertEquals("strict_date_time", requestBody(1).path("docvalue_fields").get(0).get("format").asText());
    }

    @Test
    public void nanosAndSourceArrayOrderArePreserved() throws Exception {
        respondWith("{\"books\": {\"mappings\": {\"properties\": {\"time\": {\"type\": \"date_nanos\"}, \"dates\": {\"type\": \"date\"}}}}}");
        respondWith("""
                {"hits": {"hits": [{"_source": {"time": "2024-03-15T00:00:00.123456789Z",
                  "dates": ["2024-03-16T00:00:00Z",null,"2024-03-15T00:00:00Z"]},
                  "fields": {"time": ["2024-03-15T00:00:00.123456789Z"],
                             "dates": ["2024-03-15T00:00:00.000Z","2024-03-16T00:00:00.000Z"]}}]}}
                """);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("POST /books/_search {\"_source\": [\"time\",\"dates\"]}")) {
            assertTrue(result.next());
            assertEquals(123456789, result.getTimestamp("time").getNanos());
            List<?> dates = (List<?>) result.getObject("dates");
            assertEquals(Timestamp.from(Instant.parse("2024-03-16T00:00:00Z")), dates.get(0));
            assertNull(dates.get(1));
            assertEquals(Timestamp.from(Instant.parse("2024-03-15T00:00:00Z")), dates.get(2));
        }
        assertEquals("strict_date_optional_time_nanos", requestBody(1).path("docvalue_fields").get(0).get("format").asText());
    }

    @Test
    public void userDocvalueFormatAndUnindexedDateRemainReadable() throws Exception {
        respondWith("{\"books\": {\"mappings\": {\"properties\": {\"date\": {\"type\": \"date\", \"doc_values\": false, \"format\": \"dd/MM/yyyy\"}}}}}");
        respondWith("{\"hits\": {\"hits\": [{\"_source\": {\"date\": \"15/03/2024\"}}]}}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("POST /books/_search {\"_source\": [\"date\"], \"docvalue_fields\": [{\"field\": \"other\", \"format\": \"epoch_millis\"}]}")) {
            assertTrue(result.next());
            assertEquals("15/03/2024", result.getString(1));
            assertEquals(Types.VARCHAR, result.getMetaData().getColumnType(1));
        }
        assertEquals(1, requestBody(1).path("docvalue_fields").size());
        assertEquals("epoch_millis", requestBody(1).path("docvalue_fields").get(0).get("format").asText());
    }

    @Test
    public void conflictingIndexMappingsFailBeforeSearch() throws Exception {
        respondWith("""
                {"one": {"mappings": {"properties": {"value": {"type": "binary"}}}},
                 "two": {"mappings": {"properties": {"value": {"type": "keyword"}}}}}
                """);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            try {
                statement.executeQuery("POST /one,two/_search {\"_source\": [\"value\"]}");
                fail("A heterogeneous field must not be guessed");
            } catch (SQLException expected) {
                assertTrue(expected.getMessage().contains("Conflicting"));
            }
        }
        assertEquals(1, requests.size());
    }

    @Test
    public void mappingPermissionFailureIsNotSilentlyIgnored() throws Exception {
        failWith(new ResponseException(response(403, "{\"error\": \"mapping permission denied\"}")));
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            try {
                statement.executeQuery("POST /books/_search {\"_source\": [\"value\"]}");
                fail("Schema lookup errors must be visible");
            } catch (SQLException expected) {
                assertTrue(expected.getMessage().contains("mapping permission denied"));
            }
        }
        assertEquals(1, requests.size());
    }
}
