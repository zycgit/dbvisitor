/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.transaction;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/** Propagation scopes that never start a database transaction. */
@NxnContract
public abstract class NonTransactionPropagationCase extends AbstractNxnContractTest {
    @Test
    @Capability(CapabilityId.TRANSACTION_SUPPORTS_NO_TX)
    public void supports_shouldCommitImmediatelyWhenNoTransactionExists() throws Exception {
        assertAutocommitScope(Propagation.SUPPORTS, 910034, "NXN-TX-Supports-NoTx-Only");
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_NEVER_NO_TX)
    public void never_shouldRunWithoutTransactionWhenNoTransactionExists() throws Exception {
        assertAutocommitScope(Propagation.NEVER, 910062, "NXN-TX-Never-NoTx-Only");
    }

    private void assertAutocommitScope(Propagation propagation, int id, String name) throws Exception {
        // The manager and command execution use the same datasource, including native commands.
        try (LocalTransactionManager manager = new LocalTransactionManager(jdbcTemplate.getDataSource())) {
            assertFalse(manager.hasTransaction());
            TransactionStatus status = manager.begin(propagation);
            insertUser(id, name);
            assertEquals(1, countById(id));
            manager.commit(status);
            assertFalse(manager.hasTransaction());
            assertEquals(1, countById(id));
        }
    }

    protected void insertUser(int id, String name) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, @{macro, currentTimestamp})",
                new Object[] { id, name, 30 });
    }

    protected long countById(int id) throws SQLException {
        return jdbcTemplate.queryForLong("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { id });
    }
}
