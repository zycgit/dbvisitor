/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcBatchMutationContractTest extends JdbcBatchSupport {
    @Test
    @Capability(CapabilityId.JDBC_BATCH_POSITIONAL_INSERT)
    public void jdbcBatchPositionalInsert_shouldInsertRows() throws SQLException {
        Object[][] args = new Object[3][];
        for (int i = 0; i < args.length; i++) {
            args[i] = new Object[] { baseId() + i + 1, "NXN-Batch-Pos-" + i };
        }

        int[] rows = jdbcTemplate.executeBatch("INSERT INTO basic_types_test (id, string_value) VALUES (?, ?)", args);

        assertEquals(3, rows.length);
        assertSuccessfulBatchCounts(rows);
        assertEquals(3, (int) jdbcTemplate.queryForInt("SELECT COUNT(*) FROM basic_types_test WHERE id BETWEEN ? AND ?", new Object[] { baseId() + 1, baseId() + 3 }));
    }

    @Test
    @Capability(CapabilityId.JDBC_BATCH_NAMED_INSERT)
    public void jdbcBatchNamedInsert_shouldInsertRows() throws SQLException {
        Map<String, Object>[] args = new Map[3];
        for (int i = 0; i < args.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", baseId() + 11 + i);
            row.put("val", "NXN-Batch-Named-" + i);
            args[i] = row;
        }

        int[] rows = jdbcTemplate.executeBatch("INSERT INTO basic_types_test (id, string_value) VALUES (:id, :val)", args);

        assertEquals(3, rows.length);
        assertSuccessfulBatchCounts(rows);
        assertEquals("NXN-Batch-Named-1", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { baseId() + 12 }));
    }

    @Test
    @Capability(CapabilityId.JDBC_BATCH_UPDATE)
    public void jdbcBatchUpdate_shouldChangeRows() throws SQLException {
        insertPositionalFixture();

        Object[][] args = new Object[][] { //
                new Object[] { "NXN-Batch-Updated-1", baseId() + 1 }, //
                new Object[] { "NXN-Batch-Updated-2", baseId() + 2 } };

        int[] rows = jdbcTemplate.executeBatch("UPDATE basic_types_test SET string_value = ? WHERE id = ?", args);

        assertEquals(2, rows.length);
        assertSuccessfulBatchCounts(rows);
        assertEquals("NXN-Batch-Updated-1", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { baseId() + 1 }));
        assertEquals("NXN-Batch-Updated-2", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { baseId() + 2 }));
        assertEquals("NXN-Batch-Pos-2", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { baseId() + 3 }));
    }

    @Test
    @Capability(CapabilityId.JDBC_BATCH_DELETE)
    public void jdbcBatchDelete_shouldRemoveRows() throws SQLException {
        insertPositionalFixture();

        Object[][] args = new Object[][] { //
                new Object[] { baseId() + 1 }, //
                new Object[] { baseId() + 2 } };

        int[] rows = jdbcTemplate.executeBatch("DELETE FROM basic_types_test WHERE id = ?", args);

        assertEquals(2, rows.length);
        assertSuccessfulBatchCounts(rows);
        assertEquals(1, (int) jdbcTemplate.queryForInt("SELECT COUNT(*) FROM basic_types_test WHERE id BETWEEN ? AND ?", new Object[] { baseId() + 1, baseId() + 3 }));
        assertEquals("NXN-Batch-Pos-2", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { baseId() + 3 }));
    }

    @Test
    @Capability(CapabilityId.JDBC_BATCH_STATEMENTS)
    public void jdbcBatchStatements_shouldReturnCountsForEveryStatement() throws SQLException {
        int firstId = baseId() + 41;
        int secondId = baseId() + 42;
        int[] rows = jdbcTemplate.executeBatch(new String[] {
                "INSERT INTO basic_types_test (id, string_value) VALUES (" + firstId + ", 'StatementFirst')",
                "INSERT INTO basic_types_test (id, string_value) VALUES (" + secondId + ", 'StatementSecond')",
                "UPDATE basic_types_test SET string_value = 'StatementUpdated' WHERE id = " + firstId });

        assertEquals(3, rows.length);
        assertSuccessfulBatchCounts(rows);
        assertEquals("StatementUpdated", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { firstId }));
        assertEquals("StatementSecond", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { secondId }));
    }
}
