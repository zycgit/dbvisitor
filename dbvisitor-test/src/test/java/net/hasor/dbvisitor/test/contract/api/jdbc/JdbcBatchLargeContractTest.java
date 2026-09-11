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
public abstract class JdbcBatchLargeContractTest extends JdbcBatchSupport {
    @Test
    @Capability(CapabilityId.JDBC_BATCH_LARGE_INSERT)
    public void jdbcBatchLargeInsert_shouldInsertManyNamedRows() throws SQLException {
        Map<String, Object>[] args = new Map[200];
        for (int i = 0; i < args.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", baseId() + 1000 + i);
            row.put("val", "NXN-Batch-Large-" + i);
            args[i] = row;
        }

        int[] rows = jdbcTemplate.executeBatch("INSERT INTO basic_types_test (id, string_value) VALUES (:id, :val)", args);

        assertEquals(200, rows.length);
        assertSuccessfulBatchCounts(rows);
        assertEquals(200, (int) jdbcTemplate.queryForInt("SELECT COUNT(*) FROM basic_types_test WHERE id >= ? AND id < ?", new Object[] { baseId() + 1000, baseId() + 1200 }));
        assertEquals("NXN-Batch-Large-199", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { baseId() + 1199 }));
    }
}
