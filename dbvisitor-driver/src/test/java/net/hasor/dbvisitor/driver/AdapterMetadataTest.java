/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;

import org.junit.Test;
import java.sql.*;
import java.util.*;
import static org.junit.Assert.*;

public class AdapterMetadataTest {
    @Test
    public void adapterRowsKeepJdbcTypesAndConnectionBoundaries() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        try (JdbcConnection connection = new JdbcConnection("jdbc:dbvisitor:mock://localhost", properties)) {
            MockAdapterConnection adapter = new MockAdapterConnection("jdbc:dbvisitor:mock://localhost", null) {
                @Override
                public AdapterCursor getTables(String catalog, String schema, String table, String[] types) {
                    assertEquals("catalog", catalog);
                    assertEquals("schema", schema);
                    assertEquals("table%", table);
                    assertArrayEquals(new String[] {"TABLE"}, types);
                    return AdapterMetadata.tables(Collections.singletonList(Map.of("TABLE_NAME", "table_one", "TABLE_TYPE", "TABLE")));
                }

                @Override
                public AdapterCursor getColumns(String catalog, String schema, String table, String column) {
                    return AdapterMetadata.columns(Collections.singletonList(Map.of("TABLE_NAME", "table_one", "COLUMN_NAME", "id",
                            "DATA_TYPE", Types.BIGINT, "TYPE_NAME", "long", "ORDINAL_POSITION", 1)));
                }
            };
            DatabaseMetaData metadata = new JdbcDatabaseMetaData(connection, adapter);
            assertEquals("\\", metadata.getSearchStringEscape());
            try (ResultSet rows = metadata.getTables("catalog", "schema", "table%", new String[] {"TABLE"})) {
                assertEquals(10, rows.getMetaData().getColumnCount());
                assertTrue(rows.next());
                assertEquals("table_one", rows.getString("TABLE_NAME"));
                assertFalse(rows.next());
            }
            ResultSet columns = metadata.getColumns(null, null, null, null);
            assertEquals(24, columns.getMetaData().getColumnCount());
            assertTrue(columns.next());
            assertEquals(Integer.valueOf(Types.BIGINT), columns.getObject("DATA_TYPE"));
            assertNull(columns.getObject("COLUMN_SIZE"));
            assertEquals(DatabaseMetaData.columnNullableUnknown, columns.getInt("NULLABLE"));
            assertEquals("", columns.getString("IS_NULLABLE"));
            assertEquals("", columns.getString("IS_AUTOINCREMENT"));
            assertEquals("", columns.getString("IS_GENERATEDCOLUMN"));
            columns.close();
            assertTrue(columns.isClosed());
            connection.close();
            try {
                metadata.getTables(null, null, null, null);
                fail("Closed metadata connection must fail before adapter invocation");
            } catch (SQLException expected) {
                assertNotNull(expected.getMessage());
            }
            try {
                metadata.getColumns(null, null, null, null);
                fail("Closed metadata connection must fail before adapter invocation");
            } catch (SQLException expected) {
                assertNotNull(expected.getMessage());
            }
        }
    }

    @Test
    public void jdbcPatternEscapesAndEmptyPatterns() {
        assertTrue(AdapterMetadata.matchesPattern("a_b", "a\\_b"));
        assertFalse(AdapterMetadata.matchesPattern("axb", "a\\_b"));
        assertTrue(AdapterMetadata.matchesPattern("a%b", "a\\%b"));
        assertFalse(AdapterMetadata.matchesPattern("ab", "a\\%b"));
        assertTrue(AdapterMetadata.matchesPattern("a\\b", "a\\\\b"));
        assertTrue(AdapterMetadata.matchesPattern("abc", "a%"));
        assertTrue(AdapterMetadata.matchesPattern("abc", "a_c"));
        assertTrue(AdapterMetadata.matchesPattern("", ""));
        assertFalse(AdapterMetadata.matchesPattern("abc", ""));
        assertTrue(AdapterMetadata.matchesPattern(null, null));
        assertFalse(AdapterMetadata.matchesPattern(null, "%"));
    }
}
