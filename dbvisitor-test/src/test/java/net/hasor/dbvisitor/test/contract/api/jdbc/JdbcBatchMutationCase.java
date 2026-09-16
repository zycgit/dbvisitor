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
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcBatchMutationCase extends JdbcBatchSupport {
    // 能力归属：编程式 API / 批量化。
    @Test
    @Capability(value = CapabilityId.JDBC_BATCH_POSITIONAL_INSERT, column = "jdbc/batch-operations/batch")
    public void jdbcBatchPositionalInsert_shouldInsertRows() throws SQLException {
        Object[][] args = new Object[3][];
        for (int i = 0; i < args.length; i++) {
            args[i] = new Object[] { baseId() + i + 1, "NXN-Batch-Pos-" + i };
        }

        int[] rows = jdbcTemplate.executeBatch(insertCommand(), args);

        assertEquals(3, rows.length);
        assertSuccessfulBatchCounts(rows);
        assertEquals(3, (int) jdbcTemplate.queryForInt(countRangeCommand(true), new Object[] { baseId() + 1, baseId() + 3 }));
        for (int i = 0; i < args.length; i++) {
            assertEquals("NXN-Batch-Pos-" + i, jdbcTemplate.queryForString(valueCommand(), new Object[] { baseId() + i + 1 }));
        }
    }

    // 能力归属：编程式 API / 批量化。
    @Test
    @Capability(value = CapabilityId.JDBC_BATCH_NAMED_INSERT, column = "jdbc/batch-operations/batch")
    public void jdbcBatchNamedInsert_shouldInsertRows() throws SQLException {
        Map<String, Object>[] args = new Map[3];
        for (int i = 0; i < args.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", baseId() + 11 + i);
            row.put("val", "NXN-Batch-Named-" + i);
            args[i] = row;
        }

        int[] rows = jdbcTemplate.executeBatch(namedInsertCommand(), args);

        assertEquals(3, rows.length);
        assertSuccessfulBatchCounts(rows);
        assertEquals("NXN-Batch-Named-1", jdbcTemplate.queryForString(valueCommand(), new Object[] { baseId() + 12 }));
        for (int i = 0; i < args.length; i++) {
            assertEquals("NXN-Batch-Named-" + i, jdbcTemplate.queryForString(valueCommand(), new Object[] { baseId() + 11 + i }));
        }
    }

    // 能力归属：编程式 API / 批量化。
    @Test
    @Capability(value = CapabilityId.JDBC_BATCH_UPDATE, column = "jdbc/batch-operations/batch")
    public void jdbcBatchUpdate_shouldChangeRows() throws SQLException {
        insertPositionalFixture();

        Object[][] args = new Object[][] { //
                updateArguments("NXN-Batch-Updated-1", baseId() + 1), //
                updateArguments("NXN-Batch-Updated-2", baseId() + 2) };

        int[] rows = jdbcTemplate.executeBatch(updateCommand(), args);

        assertEquals(2, rows.length);
        assertSuccessfulBatchCounts(rows, expectedUpdateCount());
        assertEquals("NXN-Batch-Updated-1", jdbcTemplate.queryForString(valueCommand(), new Object[] { baseId() + 1 }));
        assertEquals("NXN-Batch-Updated-2", jdbcTemplate.queryForString(valueCommand(), new Object[] { baseId() + 2 }));
        assertEquals("NXN-Batch-Pos-2", jdbcTemplate.queryForString(valueCommand(), new Object[] { baseId() + 3 }));
    }

    // 能力归属：编程式 API / 批量化。
    @Test
    @Capability(value = CapabilityId.JDBC_BATCH_DELETE, column = "jdbc/batch-operations/batch")
    public void jdbcBatchDelete_shouldRemoveRows() throws SQLException {
        insertPositionalFixture();

        Object[][] args = new Object[][] { //
                new Object[] { baseId() + 1 }, //
                new Object[] { baseId() + 2 } };

        int[] rows = jdbcTemplate.executeBatch(deleteCommand(), args);

        assertEquals(2, rows.length);
        assertSuccessfulBatchCounts(rows);
        assertEquals(1, (int) jdbcTemplate.queryForInt(countRangeCommand(true), new Object[] { baseId() + 1, baseId() + 3 }));
        assertEquals("NXN-Batch-Pos-2", jdbcTemplate.queryForString(valueCommand(), new Object[] { baseId() + 3 }));
    }

    // 能力归属：编程式 API / 批量化。
    @Test
    @Capability(value = CapabilityId.JDBC_BATCH_STATEMENTS, column = "jdbc/batch-operations/batch")
    public void jdbcBatchStatements_shouldReturnCountsForEveryStatement() throws SQLException {
        int firstId = baseId() + 41;
        int secondId = baseId() + 42;
        int[] rows = jdbcTemplate.executeBatch(new String[] { literalInsertCommand(firstId, "StatementFirst"), literalInsertCommand(secondId, "StatementSecond"), literalUpdateCommand(firstId, "StatementUpdated") });

        assertEquals(3, rows.length);
        assertSuccessfulBatchCounts(new int[] { rows[0], rows[1] });
        assertSuccessfulBatchCounts(new int[] { rows[2] }, expectedUpdateCount());
        assertEquals("StatementUpdated", jdbcTemplate.queryForString(valueCommand(), new Object[] { firstId }));
        assertEquals("StatementSecond", jdbcTemplate.queryForString(valueCommand(), new Object[] { secondId }));
    }
}
