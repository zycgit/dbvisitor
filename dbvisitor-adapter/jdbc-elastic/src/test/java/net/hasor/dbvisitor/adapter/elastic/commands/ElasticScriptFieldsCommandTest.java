/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElasticScriptFieldsCommandTest extends AbstractElasticCommandTest {
    @Test
    public void parameterizedScriptProducesScalarColumnWithoutMappingLookup() throws Exception {
        respondWith("{\"hits\": {\"hits\": [{\"fields\": {\"doubled_age\": [36]}}]}}");
        try (Connection connection = elasticConnection(); PreparedStatement statement = connection.prepareStatement("""
                POST /books/_search {"_source": false, "script_fields": {
                  "doubled_age": {"script": {"source": "doc['age'].value * params.factor", "params": {"factor": ?}}}}}
                """)) {
            statement.setInt(1, 2);
            try (ResultSet result = statement.executeQuery()) {
                assertEquals("doubled_age", result.getMetaData().getColumnLabel(1));
                assertEquals(Types.INTEGER, result.getMetaData().getColumnType(1));
                assertTrue(result.next());
                assertEquals(36, result.getObject(1));
                assertFalse(result.next());
            }
        }
        assertEquals(1, requests.size());
        assertEquals("/books/_search", requests.get(0).getEndpoint());
        assertEquals(2, requestBody(0).at("/script_fields/doubled_age/script/params/factor").asInt());
    }

    @Test
    public void stringScriptParameterRemainsDataAndAliasIgnoresSourceType() throws Exception {
        respondWith("{\"books\": {\"mappings\": {\"properties\": {\"upper\": {\"type\": \"binary\"}, \"missing\": {\"type\": \"keyword\"}}}}}");
        respondWith("{\"hits\": {\"hits\": [{\"_source\": {\"upper\": \"AAEC\"}, \"fields\": {\"upper\": [\"ALICE\"], \"missing\": [\"must not leak\"]}}]}}");
        String suffix = "\"}; return 'injected'; //";
        try (Connection connection = elasticConnection(); PreparedStatement statement = connection.prepareStatement("""
                POST /books/_search {"_source": ["upper","missing"], "script_fields": {
                  "upper": {"script": {"source": "doc['name'].value.toUpperCase() + params.suffix", "params": {"suffix": ?}}}}}
                """)) {
            statement.setString(1, suffix);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals("ALICE", result.getObject("upper"));
                assertNull(result.getObject("missing"));
                assertTrue(result.wasNull());
                assertEquals("upper", result.getMetaData().getColumnLabel(1));
                assertEquals("missing", result.getMetaData().getColumnLabel(2));
            }
        }
        assertEquals(suffix, requestBody(1).at("/script_fields/upper/script/params/suffix").asText());
        assertEquals("doc['name'].value.toUpperCase() + params.suffix", requestBody(1).at("/script_fields/upper/script/source").asText());
    }

    @Test
    public void nullEmptyAndMultipleScriptValuesAreNotConfused() throws Exception {
        respondWith("{\"hits\": {\"hits\": [{\"fields\": {\"empty\": [], \"nothing\": [null], \"many\": [1,2]}}]}}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("""
                        POST /books/_search {"_source": false, "script_fields": {
                          "empty": {"script": "[]"}, "nothing": {"script": "null"}, "many": {"script": "[1,2]"}}}
                        """)) {
            assertTrue(result.next());
            assertNull(result.getObject("empty"));
            assertNull(result.getObject("nothing"));
            assertEquals(Arrays.asList(1, 2), result.getObject("many"));
            assertEquals("[1,2]", result.getString("many"));
        }
    }

    @Test
    public void emptySearchStillDeclaresScriptColumnsInRequestedOrder() throws Exception {
        respondWith("{\"hits\": {\"hits\": []}}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("""
                        POST /books/_search {"_source": ["second","first"], "script_fields": {
                          "first": {"script": "1"}, "second": {"script": "2"}}}
                        """)) {
            assertEquals(2, result.getMetaData().getColumnCount());
            assertEquals("second", result.getMetaData().getColumnLabel(1));
            assertEquals("first", result.getMetaData().getColumnLabel(2));
            assertFalse(result.next());
        }
        assertEquals(1, requests.size());
    }
}
