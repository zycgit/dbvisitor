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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

import static org.junit.Assert.*;

public class MilvusGeneratedKeysSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_AUTO_ID_KEYS)
    public void autoId_shouldReturnEveryGeneratedKeyForPagedIterableInsert() throws SQLException {
        createCollection("id INT64 PRIMARY KEY AUTO_ID, name VARCHAR(32), v FLOAT_VECTOR(2)");
        indexAndLoad();
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            rows.add(new Object[] { "row-" + i, new float[] { i, 0 } });
        }
        Set<Long> keys = new HashSet<>();
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (name, v) VALUES ?", Statement.RETURN_GENERATED_KEYS)) {
            insert.setFetchSize(2);
            insert.setObject(1, rows.iterator());
            assertEquals(5, insert.executeLargeUpdate());
            try (ResultSet generated = insert.getGeneratedKeys()) {
                assertEquals("id", generated.getMetaData().getColumnLabel(1));
                while (generated.next()) {
                    assertTrue(keys.add(generated.getLong(1)));
                }
            }
        }
        assertEquals(5, keys.size());
        try (Statement statement = this.connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT id FROM " + this.collection)) {
            assertEquals(keys, new HashSet<>(readIds(result)));
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_VARCHAR_KEYS)
    public void varcharPrimaryKeys_shouldRoundTripThroughInsertAndUpsert() throws SQLException {
        createCollection("id VARCHAR(64) PRIMARY KEY, name VARCHAR(32), v FLOAT_VECTOR(2)");
        indexAndLoad();
        String key = "key'\"\\中文";
        for (String command : List.of("INSERT", "UPSERT")) {
            try (PreparedStatement write = this.connection.prepareStatement(command + " INTO " + this.collection + " (id, name, v) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                write.setString(1, key);
                write.setString(2, command);
                write.setObject(3, new float[] { 1, 0 });
                assertEquals(1, write.executeUpdate());
                try (ResultSet generated = write.getGeneratedKeys()) {
                    assertTrue(generated.next());
                    assertEquals(key, generated.getString(1));
                    assertFalse(generated.next());
                }
            }
        }
        assertEquals(1, countRows(""));
        Map<String, Object> row = this.jdbcTemplate.queryForMap("SELECT id, name FROM " + this.collection + " WHERE id = ?", new Object[] { key });
        assertEquals(key, row.get("id"));
        assertEquals("UPSERT", row.get("name"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_NO_GENERATED_KEYS)
    public void defaultInsert_shouldNotExposeKeysAndMaxRowsShouldNotLimitWrites() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, name VARCHAR(32), v FLOAT_VECTOR(2)");
        indexAndLoad();
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (id, name, v) VALUES ?")) {
            insert.setFetchSize(2);
            insert.setMaxRows(1);
            insert.setObject(1, List.of(
                    new Object[] { 1L, "a", new float[] { 1, 0 } },
                    new Object[] { 2L, "b", new float[] { 2, 0 } },
                    new Object[] { 3L, "c", new float[] { 3, 0 } }));
            assertEquals(3, insert.executeLargeUpdate());
            try (ResultSet keys = insert.getGeneratedKeys()) {
                assertFalse(keys.next());
            }
        }
        assertEquals(3, countRows(""));
    }

    private void indexAndLoad() throws SQLException {
        createIndex("v", "FLAT", "L2");
        loadCollection();
    }
}
