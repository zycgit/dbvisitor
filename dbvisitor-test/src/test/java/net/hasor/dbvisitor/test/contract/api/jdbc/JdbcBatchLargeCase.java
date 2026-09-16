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
public abstract class JdbcBatchLargeCase extends JdbcBatchSupport {
    // 能力归属：编程式 API / 批量化。
    @Test
    @Capability(value = CapabilityId.JDBC_BATCH_LARGE_INSERT, column = "jdbc/batch-operations/batch")
    public void jdbcBatchLargeInsert_shouldInsertManyNamedRows() throws SQLException {
        Map<String, Object>[] args = new Map[200];
        for (int i = 0; i < args.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", baseId() + 1000 + i);
            row.put("val", "NXN-Batch-Large-" + i);
            args[i] = row;
        }

        int[] rows = jdbcTemplate.executeBatch(namedInsertCommand(), args);

        assertEquals(200, rows.length);
        assertSuccessfulBatchCounts(rows);
        assertEquals(200, (int) jdbcTemplate.queryForInt(countRangeCommand(false), new Object[] { baseId() + 1000, baseId() + 1200 }));
        assertEquals("NXN-Batch-Large-199", jdbcTemplate.queryForString(valueCommand(), new Object[] { baseId() + 1199 }));
    }
}
