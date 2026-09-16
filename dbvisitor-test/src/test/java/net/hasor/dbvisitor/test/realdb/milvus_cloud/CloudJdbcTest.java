/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus_cloud;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class CloudJdbcTest extends CloudTestSupport {
    @Test
    public void schemaValuesAndMetadataShouldRoundTripInTheConfiguredDatabase() throws Exception {
        String table = createCollection("""
                id INT64 PRIMARY KEY, name VARCHAR(128) NULL, age INT32 DEFAULT 7,
                tags ARRAY<VARCHAR(32)>(8) NULL, doc JSON NULL, v FLOAT_VECTOR(2)
                """, "L2");
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + table + " (id,tags,doc,v) VALUES (1,?,?,?)")) {
            insert.setObject(1, new String[] { "中文", "a'b", "slash\\" });
            insert.setObject(2, Map.of("nested", Map.of("x", 2)));
            insert.setObject(3, new double[] { 0, 1 });
            assertEquals(1, insert.executeUpdate());
        }
        try (Statement statement = this.connection.createStatement();
                ResultSet row = statement.executeQuery("SELECT name,age,tags,doc,v FROM " + table + " WHERE id=1 LIMIT 1")) {
            assertTrue(row.next());
            assertNull(row.getString("name"));
            assertTrue(row.wasNull());
            assertEquals(7, row.getInt("age"));
            assertArrayEquals(new Object[] { "中文", "a'b", "slash\\" }, (Object[]) row.getArray("tags").getArray());
            assertTrue(row.getString("doc").contains("nested"));
            assertEquals(List.of(0f, 1f), row.getObject("v"));
            assertFalse(row.next());
        }
        try (ResultSet tables = this.connection.getMetaData().getTables(null, null, table, null)) {
            assertTrue(tables.next());
            assertEquals(table, tables.getString("TABLE_NAME"));
        }
        Set<String> columns = new HashSet<>();
        try (ResultSet fields = this.connection.getMetaData().getColumns(null, null, table, null)) {
            while (fields.next()) {
                columns.add(fields.getString("COLUMN_NAME"));
            }
        }
        assertTrue(columns.containsAll(Set.of("id", "name", "age", "tags", "doc", "v")));
        assertTrue(this.jdbc.queryForList("SHOW IMPORTS FROM " + table).isEmpty());
    }

    @Test
    public void specialTextShouldRemainAParameterValue() throws Exception {
        String table = seededCollection();
        String value = "\" || id > 0 || name == \"'\\中文";
        assertEquals(1, this.jdbc.executeUpdate("UPDATE " + table + " SET name=? WHERE id=1", value));
        try (PreparedStatement query = this.connection.prepareStatement("SELECT id FROM " + table + " WHERE name=? LIMIT 20")) {
            query.setString(1, value);
            try (ResultSet rows = query.executeQuery()) {
                assertEquals(List.of(1L), readIds(rows));
            }
            query.setObject(1, new StringBuilder(value), Types.VARCHAR);
            try (ResultSet rows = query.executeQuery()) {
                assertEquals(List.of(1L), readIds(rows));
            }
        }
    }

    @Test
    public void pagingAndLimitedMutationsShouldRespectTheWholeStatement() throws Exception {
        String table = seededCollection();
        try (PreparedStatement query = this.connection.prepareStatement("SELECT id FROM " + table)) {
            query.setFetchSize(2);
            try (ResultSet rows = query.executeQuery()) {
                assertEquals(Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L), new HashSet<>(readIds(rows)));
            }
        }
        try (PreparedStatement query = this.connection.prepareStatement("SELECT id FROM " + table + " ORDER BY v <-> ? LIMIT 3 OFFSET 2")) {
            query.setFetchSize(2);
            query.setObject(1, new float[] { 0, 0 });
            try (ResultSet rows = query.executeQuery()) {
                assertEquals(List.of(3L, 4L, 5L), readIds(rows));
            }
            query.setMaxRows(2);
            try (ResultSet rows = query.executeQuery()) {
                assertEquals(List.of(3L, 4L), readIds(rows));
            }
        }
        try (PreparedStatement update = this.connection.prepareStatement("UPDATE " + table + " SET name=? WHERE id>0 LIMIT 3")) {
            update.setFetchSize(2);
            update.setString(1, "updated");
            assertEquals(3, update.executeUpdate());
        }
        assertEquals(Long.valueOf(3), this.jdbc.queryForLong("COUNT FROM " + table + " WHERE name='updated'"));
        try (Statement statement = this.connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT id,v FROM " + table)) {
            while (rows.next()) {
                assertEquals(List.of((float) rows.getLong("id"), 0f), rows.getObject("v"));
            }
        }
        try (PreparedStatement delete = this.connection.prepareStatement("DELETE FROM " + table + " WHERE v <-> ? < 100 LIMIT 2")) {
            delete.setFetchSize(1);
            delete.setObject(1, new float[] { 0, 0 });
            assertEquals(2, delete.executeUpdate());
        }
        assertEquals(Long.valueOf(5), this.jdbc.queryForLong("COUNT FROM " + table));
        assertEquals(5, this.jdbc.executeUpdate("DELETE FROM " + table));
        assertEquals(Long.valueOf(0), this.jdbc.queryForLong("COUNT FROM " + table));
    }

    @Test
    public void defaultFieldPartialUpdateAndMultipleResultsShouldPreserveParameters() throws Exception {
        String table = createCollection("id INT64 PRIMARY KEY, age INT32 DEFAULT 7, v FLOAT_VECTOR(2)", "L2");
        this.jdbc.executeUpdate("INSERT INTO " + table + " (id,v) VALUES (1,[1,0])");
        String sql = "SELECT id FROM " + table + " WHERE id=? LIMIT 1; UPDATE " + table + " SET age=? WHERE id=?; SELECT age FROM " + table + " WHERE id=? LIMIT 1";
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setLong(1, 1);
            statement.setInt(2, 88);
            statement.setLong(3, 1);
            statement.setLong(4, 1);
            assertTrue(statement.execute());
            try (ResultSet rows = statement.getResultSet()) {
                assertEquals(List.of(1L), readIds(rows));
            }
            assertFalse(statement.getMoreResults());
            assertEquals(1, statement.getUpdateCount());
            assertTrue(statement.getMoreResults());
            try (ResultSet rows = statement.getResultSet()) {
                assertTrue(rows.next());
                assertEquals(88, rows.getInt(1));
                assertFalse(rows.next());
            }
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
    }

    @Test
    public void autoIdShouldReturnEveryKeyAcrossWritePages() throws Exception {
        String table = createCollection("id INT64 PRIMARY KEY AUTO_ID, name VARCHAR(32), v FLOAT_VECTOR(2)", "L2");
        List<Object[]> input = List.of(new Object[] { "a", new float[] { 1, 0 } }, new Object[] { "b", new float[] { 2, 0 } }, new Object[] { "c", new float[] { 3, 0 } });
        Set<Long> keys = new HashSet<>();
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + table + " (name,v) VALUES ?", Statement.RETURN_GENERATED_KEYS)) {
            insert.setFetchSize(2);
            insert.setObject(1, input.iterator());
            assertEquals(3, insert.executeUpdate());
            try (ResultSet generated = insert.getGeneratedKeys()) {
                while (generated.next()) {
                    assertTrue(keys.add(generated.getLong(1)));
                }
            }
        }
        assertEquals(3, keys.size());
        try (Statement statement = this.connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT id FROM " + table)) {
            assertEquals(keys, new HashSet<>(readIds(rows)));
        }
    }

    private String seededCollection() throws Exception {
        String table = createCollection("id INT64 PRIMARY KEY, name VARCHAR(128), v FLOAT_VECTOR(2)", "L2");
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + table + " (id,name,v) VALUES (?,?,?)")) {
            for (int i = 1; i <= 7; i++) {
                insert.setLong(1, i);
                insert.setString(2, "row-" + i);
                insert.setObject(3, new float[] { i, 0 });
                assertEquals(1, insert.executeUpdate());
            }
        }
        return table;
    }
}
