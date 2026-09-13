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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** 小 fetchSize 强制跨页；使用固定向量和 FLAT 索引，避免近似检索/随机数据影响断言。 */
public class MilvusPagingSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_KNN_UNLIMITED)
    public void knnWithoutLimit_shouldReadAllPagesAndAllowEarlyClose() throws SQLException {
        try (PreparedStatement query = this.connection.prepareStatement("SELECT id FROM " + this.collection + " ORDER BY v <-> ?")) {
            query.setFetchSize(2);
            query.setObject(1, new float[] { 0, 0 });
            try (ResultSet result = query.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1, result.getLong("id"));
            }
            try (ResultSet result = query.executeQuery()) {
                assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L), readIds(result));
            }
        }
    }

    @Before
    public void prepareRows() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, label VARCHAR(64), value INT64, v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        loadCollection();
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (id, label, value, v) VALUES (?, ?, ?, ?)")) {
            for (int i = 1; i <= 7; i++) {
                insert.setLong(1, i);
                insert.setString(2, "row-" + i);
                insert.setLong(3, i);
                insert.setObject(4, new float[] { i, 0 });
                assertEquals(1, insert.executeUpdate());
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_QUERY_PAGES)
    public void unlimitedSelect_shouldReadEveryMatchingRowAcrossPages() throws SQLException {
        try (PreparedStatement query = this.connection.prepareStatement("SELECT id FROM " + this.collection + " WHERE value >= ?")) {
            query.setFetchSize(2);
            query.setLong(1, 2);
            try (ResultSet result = query.executeQuery()) {
                List<Long> ids = readIds(result);
                assertEquals(6, ids.size());
                assertEquals(Set.of(2L, 3L, 4L, 5L, 6L, 7L), new HashSet<>(ids));
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_SEARCH_WINDOW)
    public void knnLimitAndOffset_shouldApplyToWholeResultNotEachPage() throws SQLException {
        try (PreparedStatement query = this.connection.prepareStatement("SELECT id, score FROM " + this.collection + " ORDER BY v <-> ? LIMIT ? OFFSET ?")) {
            query.setFetchSize(2);
            query.setObject(1, new float[] { 0, 0 });
            query.setInt(2, 3);
            query.setInt(3, 2);
            try (ResultSet result = query.executeQuery()) {
                assertEquals(List.of(3L, 4L, 5L), readIds(result));
            }
            query.setMaxRows(2);
            try (ResultSet result = query.executeQuery()) {
                assertEquals(List.of(3L, 4L), readIds(result));
            }
        }
        // Request-level options must survive both bounded and iterator search paths.
        // Seal inserted data before excluding growing segments.
        try (Statement flush = this.connection.createStatement()) {
            flush.execute("FLUSH " + this.collection);
            // Reload sealed segments; FLUSH alone does not wait for query-node handoff.
            flush.execute("RELEASE TABLE " + this.collection);
        }
        loadCollection();
        for (int fetchSize : new int[] { 1, 10 }) {
            String sql = "SELECT id, score FROM " + this.collection
                    + " ORDER BY v <-> ? LIMIT 2 WITH (round_decimal=?, ignore_growing=?)";
            try (PreparedStatement query = this.connection.prepareStatement(sql)) {
                query.setFetchSize(fetchSize);
                query.setObject(1, new float[] { 0.1234f, 0 });
                query.setInt(2, 2);
                query.setBoolean(3, true);
                try (ResultSet result = query.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals(1L, result.getLong("id"));
                    assertEquals(0.77, result.getDouble("score"), 0.00001);
                    assertTrue(result.next());
                    assertEquals(2L, result.getLong("id"));
                    assertEquals(3.52, result.getDouble("score"), 0.00001);
                    assertFalse(result.next());
                }
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_RANGE_PAGES)
    public void rangeSearch_shouldReturnAllMatchesWithoutLimit() throws SQLException {
        try (PreparedStatement query = this.connection.prepareStatement("SELECT id FROM " + this.collection + " WHERE v <-> ? < ?")) {
            query.setFetchSize(2);
            query.setObject(1, new float[] { 0, 0 });
            query.setDouble(2, 26);
            try (ResultSet result = query.executeQuery()) {
                assertEquals(List.of(1L, 2L, 3L, 4L, 5L), readIds(result));
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_PARTIAL_UPDATE_PAGES)
    public void partialUpdate_shouldPreserveVectorsAndOtherFieldsAcrossPages() throws SQLException {
        try (PreparedStatement update = this.connection.prepareStatement("UPDATE " + this.collection + " SET value = ? WHERE value >= ?")) {
            update.setFetchSize(2);
            // 修改后不再满足选行条件，验证跨页更新不会漏行。
            update.setLong(1, 0);
            update.setLong(2, 2);
            assertEquals(6, update.executeLargeUpdate());
        }
        List<Map<String, Object>> rows = this.jdbcTemplate.queryForList("SELECT id, label, value, v FROM " + this.collection);
        assertEquals(7, rows.size());
        Set<Long> ids = new HashSet<>();
        for (Map<String, Object> row : rows) {
            long id = ((Number) row.get("id")).longValue();
            assertTrue(ids.add(id));
            assertEquals("row-" + id, row.get("label"));
            assertEquals(List.of((float) id, 0.0f), row.get("v"));
            assertEquals(id == 1 ? 1 : 0, ((Number) row.get("value")).longValue());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_UPDATE_LIMIT)
    public void updateLimit_shouldCapAffectedRowsAcrossPages() throws SQLException {
        try (PreparedStatement update = this.connection.prepareStatement("UPDATE " + this.collection + " SET label = ? WHERE id > ? LIMIT ?")) {
            update.setFetchSize(2);
            update.setString(1, "changed");
            update.setLong(2, 0);
            update.setInt(3, 3);
            assertEquals(3, update.executeLargeUpdate());
        }
        assertEquals(3, countRows(" WHERE label = 'changed'"));
        assertEquals(7, countRows(""));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_DELETE_LIMIT)
    public void rangeDeleteLimit_shouldNotDeleteBeyondRequestedRows() throws SQLException {
        try (PreparedStatement delete = this.connection.prepareStatement("DELETE FROM " + this.collection + " WHERE v <-> ? < ? LIMIT ?")) {
            delete.setFetchSize(2);
            delete.setObject(1, new float[] { 0, 0 });
            delete.setDouble(2, 100);
            delete.setInt(3, 3);
            assertEquals(3, delete.executeLargeUpdate());
        }
        assertEquals(4, countRows(""));
        assertEquals(0, countRows(" WHERE id <= 3"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_DELETE_ALL_PAGES)
    public void rangeDeleteWithoutLimit_shouldDeleteAllMatchingPages() throws SQLException {
        try (PreparedStatement delete = this.connection.prepareStatement("DELETE FROM " + this.collection + " WHERE v <-> ? < ?")) {
            delete.setFetchSize(2);
            delete.setObject(1, new float[] { 0, 0 });
            delete.setDouble(2, 26);
            assertEquals(5, delete.executeLargeUpdate());
        }
        assertEquals(2, countRows(""));
        assertEquals(2, countRows(" WHERE id >= 6"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_LIMIT_ZERO)
    public void zeroLimit_shouldBeRejectedWithoutMutatingRows() throws SQLException {
        // LIMIT 必须为正整数；与 JDBC setMaxRows(0) 表示无限制不同。
        List<String> commands = List.of(
                "SELECT id FROM " + this.collection + " WHERE id > 0",
                "UPDATE " + this.collection + " SET value = 99 WHERE id > 0",
                "DELETE FROM " + this.collection + " WHERE id > 0");
        for (String command : commands) {
            try (Statement statement = this.connection.createStatement()) {
                statement.setFetchSize(2);
                SQLException error = assertThrows(SQLException.class, () -> statement.execute(command + " LIMIT 0"));
                assertEquals("LIMIT must be greater than 0.", error.getMessage());
            }
            try (PreparedStatement statement = this.connection.prepareStatement(command + " LIMIT ?")) {
                statement.setFetchSize(2);
                statement.setInt(1, 0);
                SQLException error = assertThrows(SQLException.class, statement::execute);
                assertEquals("LIMIT must be greater than 0.", error.getMessage());
            }
        }
        assertEquals(7, countRows(""));
        assertEquals(0, countRows(" WHERE value = 99"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_MULTIPLE_RESULTS)
    public void multipleStatements_shouldKeepParameterOrderAndJdbcResultSequence() throws SQLException {
        String sql = "SELECT id FROM " + this.collection + " WHERE id = ?;"
                + "UPDATE " + this.collection + " SET label = ? WHERE id = ?;"
                + "SELECT id, label FROM " + this.collection + " WHERE id = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setLong(1, 1);
            statement.setString(2, "updated");
            statement.setLong(3, 2);
            statement.setLong(4, 2);
            assertTrue(statement.execute());
            try (ResultSet result = statement.getResultSet()) {
                assertEquals(List.of(1L), readIds(result));
            }
            assertFalse(statement.getMoreResults());
            assertEquals(1, statement.getUpdateCount());
            assertTrue(statement.getMoreResults());
            try (ResultSet result = statement.getResultSet()) {
                assertTrue(result.next());
                assertEquals(2, result.getLong("id"));
                assertEquals("updated", result.getString("label"));
                assertFalse(result.next());
            }
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
    }
}
