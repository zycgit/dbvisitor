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
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class JdbcBatchConflictCase extends JdbcBatchSupport {
    @Test
    @Capability(CapabilityId.JDBC_BATCH_PARTIAL_FAILURE)
    public void jdbcBatchPartialFailure_shouldPropagateDuplicateKeyError() throws SQLException {
        requiresNxnFeature(FeatureId.BATCH_DUPLICATE_FAILURE_PROPAGATED);

        // @formatter:off
        Object[][] args = new Object[][] {
            new Object[] { baseId() + 31, "NXN-Batch-Valid-1" },
            new Object[] { baseId() + 32, "NXN-Batch-Valid-2" },
            new Object[] { baseId() + 31, "NXN-Batch-Duplicate" },
            new Object[] { baseId() + 33, "NXN-Batch-Valid-3" }
        };
        // @formatter:on

        boolean caught = false;
        try {
            jdbcTemplate.executeBatch("INSERT INTO basic_types_test (id, string_value) VALUES (?, ?)", args);
        } catch (SQLException e) {
            caught = true;
            assertTrue(lowerMessage(e), isDuplicateKeyMessage(e));
        }

        assertTrue("Expected SQLException for duplicate primary key in batch", caught);
        assertTrue(jdbcTemplate.queryForInt("SELECT COUNT(*) FROM basic_types_test WHERE id BETWEEN ? AND ?", new Object[] { baseId() + 31, baseId() + 33 }) >= 0);
    }
}
