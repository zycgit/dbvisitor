/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.transaction;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.test.contract.material.service.UserTransactionService;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionCallback;
import net.hasor.dbvisitor.transaction.TransactionCallbackWithoutResult;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.TransactionTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@NxnContract
public abstract class TransactionBoundaryCase extends TransactionSupport {
    @Test
    @Capability(CapabilityId.TRANSACTION_REQUIRED_COMMIT)
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

    @Test
    @Capability(CapabilityId.TRANSACTION_REQUIRED_ROLLBACK)
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

    @Test
    @Capability(CapabilityId.TRANSACTION_TEMPLATE_COMMIT)
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

    @Test
    @Capability(CapabilityId.TRANSACTION_TEMPLATE_ROLLBACK)
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

    @Test
    @Capability(CapabilityId.TRANSACTION_TEMPLATE_READ_ONLY)
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

    @Test
    @Capability(CapabilityId.TRANSACTION_ANNOTATION_REQUIRED)
    public void annotationRequired_shouldCommitOrRollbackThroughProxy() throws Exception {
        UserTransactionService service = userProxy();
        int commitId = baseId() + 161;
        int rollbackId = baseId() + 162;
        int outerRollbackId = baseId() + 163;

        service.createUser(commitId, "NXN-TX-Anno-Required-Commit");
        assertEquals(1, countById(commitId));

        try {
            service.createUserThenFail(rollbackId, "NXN-TX-Anno-Required-Rollback");
            fail("Annotation REQUIRED failure should rollback.");
        } catch (RuntimeException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(rollbackId));

        TransactionStatus outer = txManager().begin(Propagation.REQUIRED);
        service.createUser(outerRollbackId, "NXN-TX-Anno-Required-Joined");
        txManager().rollBack(outer);
        assertEquals(0, countById(outerRollbackId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_ANNOTATION_READ_ONLY)
    public void annotationReadOnly_shouldRollbackInsertedWorkOnCommit() throws Exception {
        int id = baseId() + 301;

        userProxy().createUserReadOnly(id, "NXN-TX-Anno-ReadOnly");

        assertEquals(0, countById(id));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_ANNOTATION_NO_ROLLBACK_FOR)
    public void annotationNoRollbackFor_shouldCommitOnlyMatchingException() throws Exception {
        UserTransactionService service = userProxy();
        int noRollbackId = baseId() + 311;
        int rollbackId = baseId() + 312;

        try {
            service.createUserNoRollbackForIAE(noRollbackId, "NXN-TX-Anno-NoRollback-IAE");
            fail("Annotated method should rethrow IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(1, countById(noRollbackId));

        try {
            service.createUserRollbackForRTE(rollbackId, "NXN-TX-Anno-NoRollback-RTE");
            fail("Non-matching RuntimeException should still rollback.");
        } catch (RuntimeException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(rollbackId));
    }
}
