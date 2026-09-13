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
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.test.contract.material.service.CallerTransactionService;
import net.hasor.dbvisitor.test.contract.material.service.UserTransactionService;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionCallback;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.TransactionTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@NxnContract
public abstract class TransactionNestedCase extends TransactionSupport {
    @Test
    @Capability(CapabilityId.TRANSACTION_NESTED_SAVEPOINT)
    public void nested_shouldRollbackToSavepointAndCommitOuterWork() throws SQLException {
        int outerId = baseId() + 21;
        int nestedId = baseId() + 22;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(outerId, "NXN-TX-Nested-Outer");
        TransactionStatus nested = tm.begin(Propagation.NESTED);
        insertUser(nestedId, "NXN-TX-Nested-Inner");
        tm.rollBack(nested);
        tm.commit(outer);

        assertEquals(1, countById(outerId));
        assertEquals(0, countById(nestedId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_NESTED_COMMIT)
    public void nested_shouldCommitNestedWorkWithOuterTransaction() throws SQLException {
        requiresNxnFeature(FeatureId.TRANSACTION_RELEASE_SAVEPOINT);
        int outerId = baseId() + 23;
        int nestedId = baseId() + 24;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(outerId, "NXN-TX-Nested-Commit-Outer");
        TransactionStatus nested = tm.begin(Propagation.NESTED);
        insertUser(nestedId, "NXN-TX-Nested-Commit-Inner");
        tm.commit(nested);
        tm.commit(outer);

        assertEquals(1, countById(outerId));
        assertEquals(1, countById(nestedId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_NESTED_OUTER_ROLLBACK)
    public void nested_shouldRollbackCommittedNestedWorkWhenOuterRollsBack() throws SQLException {
        requiresNxnFeature(FeatureId.TRANSACTION_RELEASE_SAVEPOINT);
        int outerId = baseId() + 25;
        int nestedId = baseId() + 26;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(outerId, "NXN-TX-Nested-Outer-Rollback");
        TransactionStatus nested = tm.begin(Propagation.NESTED);
        insertUser(nestedId, "NXN-TX-Nested-Inner-Then-Outer-Rollback");
        tm.commit(nested);
        tm.rollBack(outer);

        assertEquals(0, countById(outerId));
        assertEquals(0, countById(nestedId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_TEMPLATE_NESTED_SAVEPOINT)
    public void template_shouldRollbackNestedCallbackToSavepointAndCommitOuterWork() throws Throwable {
        int outerId = baseId() + 93;
        int nestedId = baseId() + 94;
        TransactionManager tm = txManager();
        TransactionTemplate template = txTemplate();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(outerId, "NXN-TX-Template-Outer-Nested");
        try {
            template.execute(new TransactionCallback<Object>() {
                @Override
                public Object doTransaction(TransactionStatus status) throws Throwable {
                    insertUser(nestedId, "NXN-TX-Template-Inner-Nested");
                    throw new IllegalStateException("nested rollback");
                }
            }, Propagation.NESTED);
            fail("Nested template callback should rethrow the user exception.");
        } catch (IllegalStateException e) {
            assertEquals("nested rollback", e.getMessage());
        }
        tm.commit(outer);

        assertEquals(1, countById(outerId));
        assertEquals(0, countById(nestedId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_ANNOTATION_NESTED)
    public void annotationNested_shouldRollbackToSavepointInsideProgrammaticOuter() throws Exception {
        requiresNxnFeature(FeatureId.TRANSACTION_RELEASE_SAVEPOINT);
        UserTransactionService service = userProxy();
        int committedNestedId = baseId() + 181;
        int rolledBackNestedId = baseId() + 182;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        service.createUserNested(committedNestedId, "NXN-TX-Anno-Nested-Commit");
        try {
            service.createUserNestedThenFail(rolledBackNestedId, "NXN-TX-Anno-Nested-Rollback");
            fail("Annotation NESTED failure should rollback to savepoint.");
        } catch (RuntimeException e) {
            assertNotNull(e.getMessage());
        }
        tm.commit(outer);

        assertEquals(1, countById(committedNestedId));
        assertEquals(0, countById(rolledBackNestedId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_PROXY_REQUIRED_NESTED)
    public void proxyRequiredToNested_shouldApplySavepointSemantics() throws Exception {
        requiresNxnFeature(FeatureId.TRANSACTION_RELEASE_SAVEPOINT);
        CallerTransactionService caller = callerProxy();
        int base = baseId() + 220;

        caller.required_callNested(base + 1, base + 2);
        assertEquals(1, countById(base + 1));
        assertEquals(1, countById(base + 2));

        caller.required_callNested_innerFailCaught(base + 3, base + 4);
        assertEquals(1, countById(base + 3));
        assertEquals(0, countById(base + 4));

        try {
            caller.required_callNested_outerFail(base + 5, base + 6);
            fail("Outer REQUIRED failure should propagate.");
        } catch (RuntimeException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(base + 5));
        assertEquals(0, countById(base + 6));
    }
}
