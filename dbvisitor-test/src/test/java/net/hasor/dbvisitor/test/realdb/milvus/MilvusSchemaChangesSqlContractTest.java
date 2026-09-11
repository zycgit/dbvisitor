/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

import static org.junit.Assert.*;

/** Online changes are issued through SQL against the existing collection, without rebuilding it. */
public class MilvusSchemaChangesSqlContractTest extends MilvusSqlContractSupport {
    private void prepareRow() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, name VARCHAR(8) NULL, v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        loadCollection();
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id, name, v) VALUES (1, 'before', [1, 0])");
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_STORAGE_STATS)
    public void nativeStatistics_shouldExposeCollectionAndPartitionRowsAfterExplicitFlush() throws SQLException {
        prepareRow();
        this.jdbcTemplate.execute("CREATE PARTITION p ON " + this.collection);
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " PARTITION p (id, name, v) VALUES (2, 'part', [0, 1])");
        this.jdbcTemplate.execute("FLUSH " + this.collection);
        try (Statement statement = this.connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("SHOW STATS FROM " + this.collection)) {
                assertTrue(result.next());
                assertEquals(2L, result.getLong("NUM_ENTITIES"));
                assertFalse(result.wasNull());
                assertEquals("2", com.google.gson.JsonParser.parseString(result.getString("STATS")).getAsJsonObject().get("row_count").getAsString());
                assertFalse(result.next());
            }
            try (ResultSet result = statement.executeQuery("SHOW STATS FROM " + this.collection + " PARTITION p")) {
                assertTrue(result.next());
                assertEquals(1L, result.getLong("NUM_ENTITIES"));
                assertEquals("1", com.google.gson.JsonParser.parseString(result.getString("STATS")).getAsJsonObject().get("row_count").getAsString());
                assertFalse(result.next());
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_NATIVE_COUNT)
    public void sqlCount_shouldUseNativeAggregateForAllFilteredAndEmptyResults() throws SQLException {
        prepareRow();
        for (String count : new String[] { "COUNT", "SELECT COUNT(*)" }) {
            assertEquals(Long.valueOf(1), this.jdbcTemplate.queryForLong(count + " FROM " + this.collection));
            try (PreparedStatement statement = this.connection.prepareStatement(count + " FROM " + this.collection + " WHERE name = ?")) {
                statement.setString(1, "before");
                try (ResultSet result = statement.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals(1L, result.getLong("COUNT"));
                    assertFalse(result.next());
                }
                statement.setString(1, "missing");
                try (ResultSet result = statement.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals(0L, result.getLong(1));
                    assertFalse(result.next());
                }
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_ADD_FIELD)
    public void addedField_shouldPreserveOldEntityAndApplyDefault() throws SQLException {
        prepareRow();
        this.jdbcTemplate.execute("ALTER TABLE " + this.collection + " ADD COLUMN priority INT64 NULL DEFAULT 7");
        try (Statement statement = this.connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT id, name, priority FROM " + this.collection + " WHERE id = 1")) {
            assertTrue(result.next());
            assertEquals("before", result.getString("name"));
            assertEquals(7L, result.getLong("priority"));
            assertFalse(result.wasNull());
            assertFalse(result.next());
        }
        assertEquals(1, this.jdbcTemplate.executeUpdate("UPDATE " + this.collection + " SET priority = 9 WHERE id = 1"));
        assertEquals(1L, countRows(" WHERE priority = 9 AND name = 'before'"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_ALTER_FIELD)
    public void increasedVarcharLimit_shouldAcceptLongerValuesWithoutLosingOldRows() throws SQLException {
        prepareRow();
        this.jdbcTemplate.execute("ALTER TABLE " + this.collection + " ALTER COLUMN name SET PROPERTIES (max_length=32)");
        assertEquals(1, this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id, name, v) VALUES (2, 'longer-than-eight', [0, 1])"));
        assertEquals(1L, countRows(" WHERE name = 'longer-than-eight'"));
        assertEquals(1L, countRows(" WHERE name = 'before'"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_COLLECTION_PROPERTIES)
    public void collectionProperties_shouldChangeWithoutDroppingData() throws SQLException {
        prepareRow();
        this.jdbcTemplate.execute("ALTER TABLE " + this.collection + " SET PROPERTIES ('collection.ttl.seconds'=3600)");
        this.jdbcTemplate.execute("ALTER TABLE " + this.collection + " DROP PROPERTIES ('collection.ttl.seconds')");
        assertEquals(1L, countRows(" WHERE id = 1 AND name = 'before'"));
    }
}
