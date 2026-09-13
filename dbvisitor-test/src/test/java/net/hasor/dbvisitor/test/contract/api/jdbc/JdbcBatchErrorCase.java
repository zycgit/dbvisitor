/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@NxnContract
public abstract class JdbcBatchErrorCase extends JdbcBatchSupport {
    @Test
    @Capability(CapabilityId.JDBC_BATCH_SQL_ERROR)
    public void jdbcBatchStatements_shouldPropagateSqlErrorWithoutRequiringUniqueConstraints() throws SQLException {
        int existingId = baseId() + 50;
        jdbcTemplate.executeUpdate("INSERT INTO basic_types_test (id, string_value) VALUES (?, ?)",
                new Object[] { existingId, "UnchangedByFailure" });
        try {
            jdbcTemplate.executeBatch(new String[] {
                    "INSERT INTO basic_types_test (id, string_value) VALUES (" + (baseId() + 51) + ", 'BeforeError')",
                    "INSERT INTO nxn_batch_missing_table (id) VALUES (1)",
                    "INSERT INTO basic_types_test (id, string_value) VALUES (" + (baseId() + 52) + ", 'AfterError')" });
            fail("A failed statement must be reported to the caller");
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals("UnchangedByFailure", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { existingId }));
        // JDBC drivers may stop, continue, or roll back their batch; no common atomicity is assumed.
        for (int id : new int[] { baseId() + 51, baseId() + 52 }) {
            Integer count = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM basic_types_test WHERE id = ?", new Object[] { id });
            assertTrue("Each valid insert may appear at most once", count == 0 || count == 1);
        }
    }
}
