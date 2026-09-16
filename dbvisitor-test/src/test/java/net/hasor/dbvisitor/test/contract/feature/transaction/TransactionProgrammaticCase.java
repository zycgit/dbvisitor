/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.transaction;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class TransactionProgrammaticCase extends TransactionSupport {
    // 能力归属：数据库事务 / 事务使用方式 / 编程式事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_REQUIRED_COMMIT, column = "transactions/transaction-apis/programmatic")
    public void required_shouldCommitOuterAndJoinedInnerWork() throws SQLException {
        int outerId = baseId() + 1;
        int innerId = baseId() + 2;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(outerId, "NXN-TX-Required-Outer");
        TransactionStatus inner = tm.begin(Propagation.REQUIRED);
        insertUser(innerId, "NXN-TX-Required-Inner");
        tm.commit(inner);
        tm.commit(outer);

        assertEquals(1, countById(outerId));
        assertEquals(1, countById(innerId));
    }

    // 能力归属：数据库事务 / 事务使用方式 / 编程式事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_REQUIRED_ROLLBACK, column = "transactions/transaction-apis/programmatic")
    public void required_shouldRollbackOuterAndJoinedInnerWork() throws SQLException {
        int outerId = baseId() + 3;
        int innerId = baseId() + 4;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(outerId, "NXN-TX-Required-Rollback-Outer");
        TransactionStatus inner = tm.begin(Propagation.REQUIRED);
        insertUser(innerId, "NXN-TX-Required-Rollback-Inner");
        tm.commit(inner);
        tm.rollBack(outer);

        assertEquals(0, countById(outerId));
        assertEquals(0, countById(innerId));
    }

    // 能力归属：数据库事务 / 事务使用方式 / 编程式事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_PROGRAMMATIC_COMPLETION, column = "transactions/transaction-apis/programmatic")
    public void programmaticCompletion_shouldFinishTopScopeAndLeaveTheManagerReusable() throws SQLException {
        int outerId = baseId() + 5;
        int innerId = baseId() + 6;
        int nextId = baseId() + 7;
        TransactionManager manager = txManager();
        assertFalse(manager.hasTransaction());

        TransactionStatus outer = manager.begin();
        try {
            assertTrue(manager.hasTransaction());
            assertTrue(manager.isTopTransaction(outer));
            assertTrue(outer.isNewConnection());
            assertFalse(outer.isCompleted());
            insertUser(outerId, "NXN-TX-Completion-Outer");

            TransactionStatus inner = manager.begin();
            assertTrue(manager.isTopTransaction(inner));
            assertFalse(manager.isTopTransaction(outer));
            assertFalse(inner.isNewConnection());
            insertUser(innerId, "NXN-TX-Completion-Joined");

            manager.commit();
            assertTrue(inner.isCompleted());
            assertFalse(outer.isCompleted());
            assertTrue(manager.isTopTransaction(outer));
            manager.rollBack();
            assertTrue(outer.isCompleted());
            assertFalse(manager.hasTransaction());
        } finally {
            if (!outer.isCompleted()) {
                manager.rollBack(outer);
            }
        }
        assertEquals(0, countById(outerId));
        assertEquals(0, countById(innerId));
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            assertTrue(connection.getAutoCommit());
            return null;
        });

        TransactionStatus next = manager.begin();
        try {
            assertFalse(next.isRollbackOnly());
            insertUser(nextId, "NXN-TX-Completion-Next");
            manager.commit();
            assertTrue(next.isCompleted());
            assertFalse(manager.hasTransaction());
        } finally {
            if (!next.isCompleted()) {
                manager.rollBack(next);
            }
        }
        assertEquals(1, countById(nextId));

        try {
            manager.commit(next);
            fail("A completed transaction must reject a second completion.");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("already completed"));
        }
        assertFalse(manager.hasTransaction());
    }
}
