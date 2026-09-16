/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.transaction;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.transaction.*;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class TransactionTemplateCase extends TransactionSupport {
    // 能力归属：数据库事务 / 事务使用方式 / 模板事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_TEMPLATE_COMMIT, column = "transactions/transaction-apis/templates")
    public void template_shouldCommitReturnedCallbackAndWithoutResultCallback() throws Throwable {
        int resultId = baseId() + 71;
        int voidId = baseId() + 72;
        TransactionTemplate template = txTemplate();

        Integer result = template.execute(new TransactionCallback<Integer>() {
            @Override
            public Integer doTransaction(TransactionStatus status) throws Throwable {
                insertUser(resultId, "NXN-TX-Template-Result");
                return 7;
            }
        });

        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doTransactionWithoutResult(TransactionStatus status) throws Throwable {
                insertUser(voidId, "NXN-TX-Template-Void");
            }
        });

        assertEquals(Integer.valueOf(7), result);
        assertEquals(1, countById(resultId));
        assertEquals(1, countById(voidId));
    }

    // 能力归属：数据库事务 / 事务使用方式 / 模板事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_TEMPLATE_ROLLBACK, column = "transactions/transaction-apis/templates")
    public void template_shouldRollbackOnExceptionOrExplicitRollback() throws Throwable {
        int exceptionId = baseId() + 81;
        int explicitId = baseId() + 82;
        TransactionTemplate template = txTemplate();

        try {
            template.execute(new TransactionCallback<Object>() {
                @Override
                public Object doTransaction(TransactionStatus status) throws Throwable {
                    insertUser(exceptionId, "NXN-TX-Template-Exception");
                    throw new IllegalStateException("expected rollback");
                }
            });
            fail("Template callback exception should be rethrown.");
        } catch (IllegalStateException e) {
            assertEquals("expected rollback", e.getMessage());
        }

        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doTransaction(TransactionStatus status) throws Throwable {
                insertUser(explicitId, "NXN-TX-Template-Explicit");
                status.setRollback();
                return null;
            }
        });

        assertEquals(0, countById(exceptionId));
        assertEquals(0, countById(explicitId));
    }

    // 能力归属：数据库事务 / 事务使用方式 / 模板事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_TEMPLATE_READ_ONLY, column = "transactions/transaction-apis/templates")
    public void template_shouldRollbackWhenStatusIsMarkedReadOnly() throws Throwable {
        int id = baseId() + 83;
        TransactionTemplate template = txTemplate();

        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doTransaction(TransactionStatus status) throws Throwable {
                insertUser(id, "NXN-TX-Template-ReadOnly");
                status.setReadOnly();
                return null;
            }
        });

        assertEquals(0, countById(id));
    }

    // 能力归属：数据库事务 / 事务使用方式 / 模板事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_TEMPLATE_EXPLICIT_COMPLETION, column = "transactions/transaction-apis/templates")
    public void template_shouldNotCompleteAnExplicitlyFinishedCallbackTwice() throws Throwable {
        int committedId = baseId() + 84;
        int rolledBackId = baseId() + 85;
        TransactionManager manager = txManager();
        TransactionTemplate template = txTemplate();

        String committed = template.execute(status -> {
            insertUser(committedId, "NXN-TX-Template-Manual-Commit");
            manager.commit(status);
            assertTrue(status.isCompleted());
            assertFalse(manager.hasTransaction());
            return "committed";
        });
        assertEquals("committed", committed);
        assertEquals(1, countById(committedId));

        String rolledBack = template.execute(status -> {
            insertUser(rolledBackId, "NXN-TX-Template-Manual-Rollback");
            manager.rollBack(status);
            assertTrue(status.isCompleted());
            assertFalse(manager.hasTransaction());
            return "rolled back";
        });
        assertEquals("rolled back", rolledBack);
        assertEquals(0, countById(rolledBackId));
        assertFalse(manager.hasTransaction());
    }
}
