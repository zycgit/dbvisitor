/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

import static org.junit.Assert.*;

public class MilvusSchemaSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_NOT_NULL)
    public void notNullField_shouldRejectNullWithoutCreatingARow() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, name VARCHAR(32) NOT NULL, v FLOAT_VECTOR(2)");
        indexAndLoad();
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (id, name, v) VALUES (1, ?, [1, 0])")) {
            insert.setNull(1, Types.VARCHAR);
            assertThrows(SQLException.class, insert::executeUpdate);
            assertEquals(0, countRows(""));
            insert.setString(1, "present");
            assertEquals(1, insert.executeUpdate());
        }
        assertEquals(1, countRows(" WHERE name = 'present'"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_NULLABLE_DEFAULT)
    public void omittedFields_shouldUseDefaultsAndPreserveNullableValues() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, note VARCHAR(64) NULL, priority INT64 DEFAULT 7, enabled BOOL DEFAULT true, v FLOAT_VECTOR(2)");
        indexAndLoad();
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id, v) VALUES (1, [1, 0])");
        try (Statement statement = this.connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT id, note, priority, enabled FROM " + this.collection + " WHERE id = 1")) {
            assertTrue(result.next());
            assertNull(result.getString("note"));
            assertTrue(result.wasNull());
            assertEquals(7, result.getLong("priority"));
            assertTrue(result.getBoolean("enabled"));
            assertEquals(ResultSetMetaData.columnNullable, result.getMetaData().isNullable(result.findColumn("note")));
            assertFalse(result.next());
        }
        try (PreparedStatement update = this.connection.prepareStatement("UPDATE " + this.collection + " SET note = ? WHERE id = ?")) {
            update.setString(1, "filled");
            update.setLong(2, 1);
            assertEquals(1, update.executeUpdate());
            update.setNull(1, Types.VARCHAR);
            assertEquals(1, update.executeUpdate());
        }
        assertEquals(1, countRows(" WHERE note IS NULL AND priority = 7"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_ARRAY_ROUND_TRIP)
    public void arrays_shouldRoundTripThroughJdbcArrayWithElementTypes() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, tags ARRAY<VARCHAR(16)>(3) NULL, nums ARRAY<INT16>(3), v FLOAT_VECTOR(2)");
        indexAndLoad();
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (id, tags, nums, v) VALUES (1, ?, ?, [1, 0])")) {
            insert.setObject(1, new String[] { "中文", "a'b", "slash\\" });
            insert.setObject(2, new short[] { -32768, 0, 32767 });
            assertEquals(1, insert.executeUpdate());
        }
        try (Statement statement = this.connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT tags, nums FROM " + this.collection + " WHERE id = 1")) {
            assertTrue(result.next());
            Array tags = result.getArray("tags");
            Array nums = result.getArray("nums");
            try {
                assertEquals(Types.VARCHAR, tags.getBaseType());
                assertArrayEquals(new Object[] { "中文", "a'b", "slash\\" }, (Object[]) tags.getArray());
                assertEquals(Types.SMALLINT, nums.getBaseType());
                Object[] values = (Object[]) nums.getArray();
                assertEquals(3, values.length);
                assertEquals(-32768, ((Number) values[0]).intValue());
                assertEquals(0, ((Number) values[1]).intValue());
                assertEquals(32767, ((Number) values[2]).intValue());
            } finally {
                tags.free();
                nums.free();
            }
            assertFalse(result.next());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_ARRAY_NULL_EMPTY)
    public void nullableArray_shouldDistinguishNullFromEmptyArray() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, tags ARRAY<VARCHAR(16)>(3) NULL, v FLOAT_VECTOR(2)");
        indexAndLoad();
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (id, tags, v) VALUES (?, ?, [1, 0])")) {
            insert.setLong(1, 1);
            insert.setNull(2, Types.ARRAY);
            assertEquals(1, insert.executeUpdate());
            insert.setLong(1, 2);
            insert.setObject(2, List.of());
            assertEquals(1, insert.executeUpdate());
        }
        try (Statement statement = this.connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT id, tags FROM " + this.collection + " WHERE tags IS NULL")) {
            assertTrue(result.next());
            assertEquals(1, result.getLong("id"));
            assertNull(result.getArray("tags"));
            assertTrue(result.wasNull());
            assertFalse(result.next());
        }
        try (Statement statement = this.connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT id, tags FROM " + this.collection + " WHERE tags IS NOT NULL")) {
            assertTrue(result.next());
            assertEquals(2, result.getLong("id"));
            Array tags = result.getArray("tags");
            try {
                assertEquals(0, ((Object[]) tags.getArray()).length);
            } finally {
                tags.free();
            }
            assertFalse(result.next());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_ARRAY_VALIDATION)
    public void arrayConstraints_shouldRejectCapacityLengthAndNullElements() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, tags ARRAY<VARCHAR(4)>(2), v FLOAT_VECTOR(2)");
        indexAndLoad();
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (id, tags, v) VALUES (1, ?, [1, 0])")) {
            List<?>[] invalid = { List.of("a", "b", "c"), List.of("中文"), Arrays.asList("a", null) };
            for (List<?> value : invalid) {
                insert.setObject(1, value);
                assertThrows("Invalid array: " + value, SQLException.class, insert::executeUpdate);
            }
        }
        assertEquals(0, countRows(""));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_SCHEMA_DESCRIPTION)
    public void showTable_shouldExposeArrayAndNullableDefinition() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, tags ARRAY<VARCHAR(16)>(3) NULL, v FLOAT_VECTOR(2)");
        boolean found = false;
        try (Statement statement = this.connection.createStatement();
             ResultSet result = statement.executeQuery("SHOW TABLE " + this.collection)) {
            while (result.next()) {
                if ("tags".equals(result.getString("FIELD"))) {
                    found = true;
                    assertTrue(result.getBoolean("NULLABLE"));
                    assertEquals("VarChar", result.getString("ELEMENT_TYPE"));
                    assertEquals(3, result.getInt("MAX_CAPACITY"));
                    assertEquals(16, result.getInt("MAX_LENGTH"));
                }
            }
        }
        assertTrue("SHOW TABLE must include the tags field", found);
    }

    private void indexAndLoad() throws SQLException {
        createIndex("v", "FLAT", "L2");
        loadCollection();
    }
}
