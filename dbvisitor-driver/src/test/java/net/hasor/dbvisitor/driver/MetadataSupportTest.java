/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MetadataSupportTest {
    private MetadataSupport support;
    @Before
    public void registerAdapter() {
        AdapterManager.register("mock", new MockAdapterFactory());
    }

    @Test
    public void adapterRowsKeepJdbcTypesAndConnectionBoundaries() throws Exception {
        support = provider(path -> {
            switch (path.type()) {
                case CATALOG:
                    return List.of(new MetadataNode(MetadataType.CATALOG, "catalog"));
                case SCHEMA:
                    assertEquals("catalog", path.name(MetadataType.CATALOG));
                    return List.of(new MetadataNode(MetadataType.SCHEMA, "schema"));
                case TABLE:
                    assertEquals("schema", path.name(MetadataType.SCHEMA));
                    return List.of(new MetadataNode(MetadataType.TABLE, "table_one"));
                case COLUMN:
                    assertEquals("table_one", path.name(MetadataType.TABLE));
                    return List.of(new MetadataNode(MetadataType.COLUMN, "id", Map.of(MetadataNode.JDBC_TYPE, Types.BIGINT, MetadataNode.TYPE_NAME, "long")));
                default:
                    return List.of();
            }
        }, MetadataType.CATALOG, MetadataType.SCHEMA, MetadataType.TABLE, MetadataType.COLUMN);
        try (JdbcConnection connection = connection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            assertEquals("\\", metadata.getSearchStringEscape());
            try (ResultSet schemas = metadata.getSchemas(); ResultSet filtered = metadata.getSchemas("catalog", "schema")) {
                assertTrue(schemas.next());
                assertEquals("schema", schemas.getString("TABLE_SCHEM"));
                assertTrue(filtered.next());
                assertEquals("schema", filtered.getString("TABLE_SCHEM"));
            }
            try (ResultSet rows = metadata.getTables("catalog", "schema", "table%", new String[] { "TABLE" })) {
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
    public void defaultProviderReturnsIndependentEmptyResults() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        try (JdbcConnection connection = new JdbcConnection("jdbc:dbvisitor:mock://localhost", properties)) {
            MockAdapterConnection adapter = new MockAdapterConnection("jdbc:dbvisitor:mock://localhost", null);
            assertNull(connection.metadataSupport());
            assertFalse(connection.isWrapperFor(MetadataSupport.class));
            DatabaseMetaData metadata = new JdbcDatabaseMetaData(connection, adapter);
            try (ResultSet catalogs = metadata.getCatalogs(); ResultSet schemas = metadata.getSchemas(); ResultSet types = metadata.getTableTypes(); ResultSet tables = metadata.getTables(null, null, null, null); ResultSet columns = metadata.getColumns(null, null, null, null)) {
                assertFalse(catalogs.next());
                assertFalse(schemas.next());
                assertFalse(types.next());
                assertFalse(tables.next());
                assertFalse(columns.next());
                assertEquals(10, tables.getMetaData().getColumnCount());
                assertEquals(24, columns.getMetaData().getColumnCount());
                assertEquals(Types.INTEGER, columns.getMetaData().getColumnType(5));
                tables.close();
                assertFalse(columns.isClosed());
            }
            try (ResultSet tables = metadata.getTables(null, null, null, null)) {
                assertFalse(tables.next());
            }
            connection.close();
            assertThrows(SQLException.class, metadata::getSchemas);
        }
    }

    @Test
    public void jdbcPatternsFilterLiteralNativeNames() throws Exception {
        try (JdbcConnection connection = connection()) {
            DatabaseMetaData metadata = metadata(connection, path -> {
                return List.of(new MetadataNode(MetadataType.TABLE, "a_b"), new MetadataNode(MetadataType.TABLE, "axb"), new MetadataNode(MetadataType.TABLE, "a%b"), new MetadataNode(MetadataType.TABLE, "a/b"));
            }, MetadataType.TABLE);
            try (ResultSet rows = metadata.getTables(null, null, "a\\_b", null)) {
                assertTrue(rows.next());
                assertEquals("a_b", rows.getString("TABLE_NAME"));
                assertFalse(rows.next());
            }
            try (ResultSet rows = metadata.getTables(null, null, "a\\%b", null)) {
                assertTrue(rows.next());
                assertEquals("a%b", rows.getString("TABLE_NAME"));
                assertFalse(rows.next());
            }
            try (ResultSet rows = metadata.getTables(null, null, "", null)) {
                assertFalse(rows.next());
            }
            try (ResultSet rows = metadata.getTables(null, "missing", "%", null)) {
                assertFalse(rows.next());
            }
        }
    }

    @Test
    public void typeDeclarationsDoNotRequireExistingTables() throws Exception {
        try (JdbcConnection connection = connection()) {
            DatabaseMetaData metadata = metadata(connection, path -> List.of(), MetadataType.TABLE);
            try (ResultSet rows = metadata.getTableTypes()) {
                assertTrue(rows.next());
                assertEquals("TABLE", rows.getString(1));
                assertFalse(rows.next());
            }
            try (ResultSet rows = metadata.getTables(null, null, null, null)) {
                assertFalse(rows.next());
            }
        }
    }

    @Test
    public void providerFailuresAreNotEmptyMetadata() throws Exception {
        try (JdbcConnection connection = connection()) {
            SQLException failure = new SQLException("permission denied");
            assertSame(failure, assertThrows(SQLException.class, () -> metadata(connection, path -> {
                throw failure;
            }).getCatalogs()));
            assertThrows(SQLException.class, () -> metadata(connection, path -> null).getCatalogs());
            assertThrows(SQLException.class, () -> metadata(connection, path -> List.of(new MetadataNode(MetadataType.TABLE, "wrong"))).getCatalogs());
        }
    }

    @Test
    public void pathsKeepNamesAndAttributesImmutable() {
        MetadataPath path = new MetadataPath(MetadataType.CATALOG).child(new MetadataNode(MetadataType.CATALOG, "a/b_%"), MetadataType.TABLE);
        assertEquals("a/b_%", path.name(MetadataType.CATALOG));
        assertThrows(UnsupportedOperationException.class, () -> path.levels().clear());
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MetadataNode.SIZE, 10);
        MetadataNode node = new MetadataNode(MetadataType.COLUMN, "id", attributes);
        attributes.clear();
        assertEquals(10, node.attributes().get(MetadataNode.SIZE));
        assertThrows(UnsupportedOperationException.class, () -> node.attributes().clear());
    }

    private JdbcConnection connection() throws SQLException {
        AdapterManager.register("metadata-test", new MockAdapterFactory() {
            @Override
            public AdapterConnection createConnection(Connection owner, String url, Properties properties) {
                return new MetadataConnection(url);
            }
        });
        Properties properties = new Properties();
        properties.setProperty(JdbcDriver.P_ADAPTER_NAME, "metadata-test");
        return new JdbcConnection("jdbc:dbvisitor:metadata-test://localhost", properties);
    }

    private DatabaseMetaData metadata(JdbcConnection connection, MetadataSupport query, MetadataType... types) throws SQLException {
        support = provider(query, types);
        return connection.getMetaData();
    }

    private class MetadataConnection extends MockAdapterConnection implements MetadataSupport {
        MetadataConnection(String url) {
            super(url, null);
        }

        @Override
        public Set<MetadataType> supportedTypes() {
            return support.supportedTypes();
        }

        @Override
        public List<MetadataNode> query(MetadataPath path) throws SQLException {
            return support.query(path);
        }
    }

    private MetadataSupport provider(MetadataSupport query, MetadataType... types) {
        return new MetadataSupport() {
            @Override
            public Set<MetadataType> supportedTypes() {
                return Set.of(types);
            }

            @Override
            public List<MetadataNode> query(MetadataPath path) throws SQLException {
                return query.query(path);
            }
        };
    }

}
