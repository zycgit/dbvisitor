/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic.commands;

import java.sql.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElasticMetadataCommandTest extends AbstractElasticCommandTest {
    @Test
    public void tablePatternsAndNamespacesUseRealIndices() throws Exception {
        respondWith("{\"book_one\": {\"mappings\": {}}, \"bookXone\": {\"mappings\": {}}}");
        try (Connection connection = elasticConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            assertEquals("\\", metadata.getSearchStringEscape());
            try (ResultSet rows = metadata.getTables(null, null, "book\\_one", new String[] {"TABLE"})) {
                assertTrue(rows.next());
                assertEquals("book_one", rows.getString("TABLE_NAME"));
                assertEquals("TABLE", rows.getString("TABLE_TYPE"));
                assertNull(rows.getObject("TABLE_CAT"));
                assertFalse(rows.next());
            }
            try (ResultSet rows = metadata.getTables("other", null, "%", null)) {
                assertFalse(rows.next());
            }
            try (ResultSet rows = metadata.getTables(null, null, "%", new String[] {"VIEW"})) {
                assertFalse(rows.next());
            }
        }
        assertEquals(1, requests.size());
        assertEquals("/_mapping", requests.get(0).getEndpoint());
    }

    @Test
    public void typedMappingReportsDeclaredTypesWithoutInventingPrecision() throws Exception {
        respondWith("""
                {"books": {"mappings": {"_doc": {"properties": {
                  "id": {"type": "long"}, "name": {"type": "keyword"},
                  "address": {"properties": {"city": {"type": "keyword"}}}
                }}}}}
                """);
        try (Connection connection = elasticConnection(); ResultSet rows = connection.getMetaData().getColumns(null, "", "books", "id")) {
            assertTrue(rows.next());
            assertEquals(Types.BIGINT, rows.getInt("DATA_TYPE"));
            assertEquals("long", rows.getString("TYPE_NAME"));
            assertEquals(DatabaseMetaData.columnNullableUnknown, rows.getInt("NULLABLE"));
            assertNull(rows.getObject("COLUMN_SIZE"));
            assertNull(rows.getObject("COLUMN_DEF"));
            assertEquals(3, rows.getInt("ORDINAL_POSITION"));
            assertFalse(rows.next());
        }
    }

    @Test
    public void permissionFailureAndClosedConnectionAreNotEmptySchemas() throws Exception {
        respondWith(403, "{\"error\": \"forbidden\"}");
        Connection connection = elasticConnection();
        DatabaseMetaData metadata = connection.getMetaData();
        try {
            metadata.getTables(null, null, null, null);
            fail("Expected mapping permission failure");
        } catch (SQLException expected) {
            assertEquals("E403", expected.getSQLState());
        } finally {
            connection.close();
        }
        try {
            metadata.getColumns(null, null, null, null);
            fail("Expected closed connection failure");
        } catch (SQLException expected) {
            assertNotNull(expected.getMessage());
        }
        assertEquals(1, requests.size());
    }
}
