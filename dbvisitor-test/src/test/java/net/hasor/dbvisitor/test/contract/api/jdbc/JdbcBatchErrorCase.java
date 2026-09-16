/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class JdbcBatchErrorCase extends JdbcBatchSupport {
    // 能力归属：编程式 API / 批量化。
    @Test
    @Capability(value = CapabilityId.JDBC_BATCH_SQL_ERROR, column = "jdbc/batch-operations/batch")
    public void jdbcBatchStatements_shouldPropagateSqlErrorWithoutRequiringUniqueConstraints() throws SQLException {
        int existingId = baseId() + 50;
        jdbcTemplate.executeUpdate(insertCommand(), new Object[] { existingId, "UnchangedByFailure" });
        try {
            jdbcTemplate.executeBatch(new String[] { literalInsertCommand(baseId() + 51, "BeforeError"), invalidCommand(), literalInsertCommand(baseId() + 52, "AfterError") });
            fail("A failed statement must be reported to the caller");
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals("UnchangedByFailure", jdbcTemplate.queryForString(valueCommand(), new Object[] { existingId }));
        // JDBC drivers may stop, continue, or roll back their batch; no common atomicity is assumed.
        for (int id : new int[] { baseId() + 51, baseId() + 52 }) {
            Integer count = jdbcTemplate.queryForInt(countCommand(), new Object[] { id });
            assertTrue("Each valid insert may appear at most once", count == 0 || count == 1);
        }
    }
}
