/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.transaction;

import net.hasor.dbvisitor.test.contract.material.service.ClassLevelTransactionService;
import net.hasor.dbvisitor.test.contract.material.service.UserTransactionService;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class TransactionAnnotationCase extends TransactionSupport {
    // 能力归属：数据库事务 / 事务使用方式 / 注解式事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_ANNOTATION_REQUIRED, column = "transactions/transaction-apis/annotations")
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

    // 能力归属：数据库事务 / 事务使用方式 / 注解式事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_ANNOTATION_READ_ONLY, column = "transactions/transaction-apis/annotations")
    public void annotationReadOnly_shouldRollbackInsertedWorkOnCommit() throws Exception {
        int id = baseId() + 301;

        userProxy().createUserReadOnly(id, "NXN-TX-Anno-ReadOnly");

        assertEquals(0, countById(id));
    }

    // 能力归属：数据库事务 / 事务使用方式 / 注解式事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_ANNOTATION_NO_ROLLBACK_FOR, column = "transactions/transaction-apis/annotations")
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

    // 能力归属：数据库事务 / 事务使用方式 / 注解式事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_ANNOTATION_CLASS_DEFAULTS, column = "transactions/transaction-apis/annotations")
    public void annotationClassDefaults_shouldApplyUnlessMethodOverridesThem() throws Exception {
        ClassLevelTransactionService service = TransactionHelper.support(new ClassLevelTransactionService(lambdaTemplate), dataSource);
        int classDefaultId = baseId() + 331;
        int methodOverrideId = baseId() + 332;

        assertEquals(1, service.createUserWithClassDefaults(classDefaultId, "NXN-TX-Anno-Class-ReadOnly"));
        assertEquals(0, countById(classDefaultId));

        assertEquals(1, service.createUserWithMethodOverride(methodOverrideId, "NXN-TX-Anno-Method-Override"));
        assertEquals(1, countById(methodOverrideId));
        assertFalse(txManager().hasTransaction());
    }

    // 能力归属：数据库事务 / 事务使用方式 / 注解式事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_ANNOTATION_NO_ROLLBACK_CLASS_NAME, column = "transactions/transaction-apis/annotations")
    public void annotationNoRollbackForClassName_shouldMatchOnlyTheExactExceptionName() throws Exception {
        UserTransactionService service = userProxy();
        int committedId = baseId() + 313;
        int rolledBackId = baseId() + 314;

        try {
            service.createUserNoRollbackForClassName(committedId, "NXN-TX-Anno-Named-Commit");
            fail("The matching exception must still reach the caller.");
        } catch (IllegalArgumentException e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("Named exception should commit", e.getMessage());
        }
        assertEquals(1, countById(committedId));

        try {
            service.createUserRollbackForDifferentClassName(rolledBackId, "NXN-TX-Anno-Named-Rollback");
            fail("An exception subclass with a different class name must still rollback.");
        } catch (NumberFormatException e) {
            assertEquals("Different exception class should rollback", e.getMessage());
        }
        assertEquals(0, countById(rolledBackId));
        assertFalse(txManager().hasTransaction());
    }
}
