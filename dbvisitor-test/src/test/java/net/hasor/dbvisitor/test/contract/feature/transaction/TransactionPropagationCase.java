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
public abstract class TransactionPropagationCase extends TransactionSupport {
    @Test
    @Capability(CapabilityId.TRANSACTION_REQUIRES_NEW)
    public void requiresNew_shouldCommitIndependentlyFromOuterRollback() throws SQLException {
        int outerId = baseId() + 11;
        int innerId = baseId() + 12;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(outerId, "NXN-TX-Outer-RN");
        TransactionStatus inner = tm.begin(Propagation.REQUIRES_NEW);
        insertUser(innerId, "NXN-TX-Inner-RN");
        tm.commit(inner);
        tm.rollBack(outer);

        assertEquals(0, countById(outerId));
        assertEquals(1, countById(innerId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_REQUIRES_NEW_INNER_ROLLBACK)
    public void requiresNew_shouldRollbackInnerAndCommitOuterIndependently() throws SQLException {
        int outerId = baseId() + 13;
        int innerId = baseId() + 14;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(outerId, "NXN-TX-Outer-RN-Rollback");
        TransactionStatus inner = tm.begin(Propagation.REQUIRES_NEW);
        insertUser(innerId, "NXN-TX-Inner-RN-Rollback");
        tm.rollBack(inner);
        tm.commit(outer);

        assertEquals(1, countById(outerId));
        assertEquals(0, countById(innerId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_SUPPORTS)
    public void supports_shouldJoinExistingTransactionOrRunWithoutTransaction() throws SQLException {
        int joinedOuterId = baseId() + 31;
        int joinedInnerId = baseId() + 32;
        int noTxId = baseId() + 33;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(joinedOuterId, "NXN-TX-Supports-Outer");
        TransactionStatus inner = tm.begin(Propagation.SUPPORTS);
        insertUser(joinedInnerId, "NXN-TX-Supports-Inner");
        tm.commit(inner);
        tm.rollBack(outer);

        TransactionStatus noTx = tm.begin(Propagation.SUPPORTS);
        insertUser(noTxId, "NXN-TX-Supports-NoTx");
        tm.commit(noTx);

        assertEquals(0, countById(joinedOuterId));
        assertEquals(0, countById(joinedInnerId));
        assertEquals(1, countById(noTxId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_SUPPORTS_NO_TX)
    public void supports_shouldCommitImmediatelyWhenNoTransactionExists() throws SQLException {
        int id = baseId() + 34;
        TransactionManager tm = txManager();

        TransactionStatus status = tm.begin(Propagation.SUPPORTS);
        insertUser(id, "NXN-TX-Supports-NoTx-Only");
        tm.commit(status);

        assertEquals(1, countById(id));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_NOT_SUPPORTED)
    public void notSupported_shouldSuspendOuterTransaction() throws SQLException {
        int outerId = baseId() + 41;
        int innerId = baseId() + 42;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(outerId, "NXN-TX-NotSupported-Outer");
        TransactionStatus inner = tm.begin(Propagation.NOT_SUPPORTED);
        insertUser(innerId, "NXN-TX-NotSupported-Inner");
        tm.commit(inner);
        tm.rollBack(outer);

        assertEquals(0, countById(outerId));
        assertEquals(1, countById(innerId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_MANDATORY)
    public void mandatory_shouldRequireExistingTransaction() throws SQLException {
        int id = baseId() + 51;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        TransactionStatus inner = tm.begin(Propagation.MANDATORY);
        insertUser(id, "NXN-TX-Mandatory");
        tm.commit(inner);
        tm.commit(outer);

        assertEquals(1, countById(id));

        try {
            tm.begin(Propagation.MANDATORY);
            fail("MANDATORY should require an existing transaction.");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_NEVER)
    public void never_shouldRejectExistingTransaction() throws SQLException {
        int id = baseId() + 61;
        TransactionManager tm = txManager();

        TransactionStatus noTx = tm.begin(Propagation.NEVER);
        insertUser(id, "NXN-TX-Never");
        tm.commit(noTx);
        assertEquals(1, countById(id));

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        try {
            tm.begin(Propagation.NEVER);
            fail("NEVER should reject an existing transaction.");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        } finally {
            tm.rollBack(outer);
        }
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_NEVER_NO_TX)
    public void never_shouldRunWithoutTransactionWhenNoTransactionExists() throws SQLException {
        int id = baseId() + 62;
        TransactionManager tm = txManager();

        TransactionStatus status = tm.begin(Propagation.NEVER);
        insertUser(id, "NXN-TX-Never-NoTx-Only");
        tm.commit(status);

        assertEquals(1, countById(id));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_TEMPLATE_REQUIRES_NEW)
    public void template_shouldCommitRequiresNewWorkIndependentlyFromOuterRollback() throws Throwable {
        int outerId = baseId() + 91;
        int innerId = baseId() + 92;
        TransactionManager tm = txManager();
        TransactionTemplate template = txTemplate();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(outerId, "NXN-TX-Template-Outer-RN");
        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doTransaction(TransactionStatus status) throws Throwable {
                insertUser(innerId, "NXN-TX-Template-Inner-RN");
                return null;
            }
        }, Propagation.REQUIRES_NEW);
        tm.rollBack(outer);

        assertEquals(0, countById(outerId));
        assertEquals(1, countById(innerId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_MIXED_REQUIRES_NEW)
    public void mixedApis_shouldKeepRequiresNewWorkWhenOuterApiRollsBack() throws SQLException {
        int outerJdbcId = baseId() + 131;
        int innerLambdaId = baseId() + 132;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, @{macro, currentTimestamp})", new Object[] { outerJdbcId, "NXN-TX-Mixed-Outer-Jdbc", 25 });
        TransactionStatus inner = tm.begin(Propagation.REQUIRES_NEW);
        insertUser(innerLambdaId, "NXN-TX-Mixed-Inner-Lambda");
        tm.commit(inner);
        tm.rollBack(outer);

        assertEquals(0, countById(outerJdbcId));
        assertEquals(1, countById(innerLambdaId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_ANNOTATION_REQUIRES_NEW)
    public void annotationRequiresNew_shouldCommitIndependentlyAndRollbackOwnFailure() throws Exception {
        UserTransactionService service = userProxy();
        int independentId = baseId() + 171;
        int failedId = baseId() + 172;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        service.createUserInNewTx(independentId, "NXN-TX-Anno-RN-Independent");
        tm.rollBack(outer);
        assertEquals(1, countById(independentId));

        try {
            service.createUserInNewTxThenFail(failedId, "NXN-TX-Anno-RN-Fail");
            fail("Annotation REQUIRES_NEW failure should rollback its own transaction.");
        } catch (RuntimeException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(failedId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_ANNOTATION_OTHER_PROPAGATION)
    public void annotationOtherPropagationModes_shouldMatchProgrammaticTransactionBoundary() throws Exception {
        UserTransactionService service = userProxy();
        int supportsJoinedId = baseId() + 191;
        int supportsNoTxId = baseId() + 192;
        int notSupportedId = baseId() + 193;
        int mandatoryId = baseId() + 194;
        int mandatoryNoTxId = baseId() + 195;
        int neverNoTxId = baseId() + 196;
        int neverWithTxId = baseId() + 197;
        TransactionManager tm = txManager();

        TransactionStatus supportsOuter = tm.begin(Propagation.REQUIRED);
        service.createUserSupports(supportsJoinedId, "NXN-TX-Anno-Supports-Joined");
        tm.rollBack(supportsOuter);
        assertEquals(0, countById(supportsJoinedId));

        service.createUserSupports(supportsNoTxId, "NXN-TX-Anno-Supports-NoTx");
        assertEquals(1, countById(supportsNoTxId));

        TransactionStatus notSupportedOuter = tm.begin(Propagation.REQUIRED);
        service.createUserNotSupported(notSupportedId, "NXN-TX-Anno-NotSupported");
        tm.rollBack(notSupportedOuter);
        assertEquals(1, countById(notSupportedId));

        TransactionStatus mandatoryOuter = tm.begin(Propagation.REQUIRED);
        service.createUserMandatory(mandatoryId, "NXN-TX-Anno-Mandatory");
        tm.commit(mandatoryOuter);
        assertEquals(1, countById(mandatoryId));

        try {
            service.createUserMandatory(mandatoryNoTxId, "NXN-TX-Anno-Mandatory-Fail");
            fail("Annotation MANDATORY should require an existing transaction.");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(mandatoryNoTxId));

        service.createUserNever(neverNoTxId, "NXN-TX-Anno-Never-NoTx");
        assertEquals(1, countById(neverNoTxId));

        TransactionStatus neverOuter = tm.begin(Propagation.REQUIRED);
        try {
            service.createUserNever(neverWithTxId, "NXN-TX-Anno-Never-Fail");
            fail("Annotation NEVER should reject an existing transaction.");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        } finally {
            tm.rollBack(neverOuter);
        }
        assertEquals(0, countById(neverWithTxId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_PROXY_REQUIRED_REQUIRED)
    public void proxyRequiredToRequired_shouldExposeJoinedTransactionSemantics() throws Exception {
        CallerTransactionService caller = callerProxy();
        int base = baseId() + 200;

        caller.required_callRequired(base + 1, base + 2);
        assertEquals(1, countById(base + 1));
        assertEquals(1, countById(base + 2));

        try {
            caller.required_callRequired_fail(base + 3, base + 4);
            fail("Inner REQUIRED failure should propagate.");
        } catch (RuntimeException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(base + 3));
        assertEquals(0, countById(base + 4));

        caller.required_callRequired_innerFailCaught(base + 5, base + 6);
        assertEquals(1, countById(base + 5));
        assertEquals(1, countById(base + 6));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_PROXY_REQUIRED_REQUIRES_NEW)
    public void proxyRequiredToRequiresNew_shouldIsolateInnerNewTransaction() throws Exception {
        CallerTransactionService caller = callerProxy();
        int base = baseId() + 210;

        caller.required_callRequiresNew(base + 1, base + 2);
        assertEquals(1, countById(base + 1));
        assertEquals(1, countById(base + 2));

        try {
            caller.required_callRequiresNew_outerFail(base + 3, base + 4);
            fail("Outer REQUIRED failure should propagate.");
        } catch (RuntimeException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(base + 3));
        assertEquals(1, countById(base + 4));

        caller.required_callRequiresNew_innerFailCaught(base + 5, base + 6);
        assertEquals(1, countById(base + 5));
        assertEquals(0, countById(base + 6));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_PROXY_REQUIRED_OTHER_PROPAGATION)
    public void proxyRequiredToOtherPropagationModes_shouldMatchDeclaredSemantics() throws Exception {
        CallerTransactionService caller = callerProxy();
        int base = baseId() + 230;

        try {
            caller.required_callNotSupported_outerFail(base + 1, base + 2);
            fail("Outer REQUIRED failure should propagate.");
        } catch (RuntimeException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(base + 1));
        assertEquals(1, countById(base + 2));

        try {
            caller.required_callSupports_outerFail(base + 3, base + 4);
            fail("Outer REQUIRED failure should propagate.");
        } catch (RuntimeException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(base + 3));
        assertEquals(0, countById(base + 4));

        caller.required_callMandatory(base + 5, base + 6);
        assertEquals(1, countById(base + 5));
        assertEquals(1, countById(base + 6));

        try {
            caller.required_callNever(base + 7, base + 8);
            fail("NEVER inside REQUIRED should fail.");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(base + 7));
        assertEquals(0, countById(base + 8));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_PROXY_REQUIRES_NEW_COMBINATIONS)
    public void proxyRequiresNewOuter_shouldCombineWithRequiredAndRequiresNewInner() throws Exception {
        CallerTransactionService caller = callerProxy();
        int base = baseId() + 240;

        caller.requiresNew_callRequired(base + 1, base + 2);
        assertEquals(1, countById(base + 1));
        assertEquals(1, countById(base + 2));

        try {
            caller.requiresNew_callRequired_outerFail(base + 3, base + 4);
            fail("Outer REQUIRES_NEW failure should propagate.");
        } catch (RuntimeException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(base + 3));
        assertEquals(0, countById(base + 4));

        caller.requiresNew_callRequiresNew(base + 5, base + 6);
        assertEquals(1, countById(base + 5));
        assertEquals(1, countById(base + 6));

        try {
            caller.requiresNew_callRequiresNew_outerFail(base + 7, base + 8);
            fail("Outer REQUIRES_NEW failure should propagate.");
        } catch (RuntimeException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(base + 7));
        assertEquals(1, countById(base + 8));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_PROXY_THREE_LEVEL)
    public void proxyThreeLevel_shouldPreserveMiddleRequiresNewAfterProgrammaticOuterRollback() throws Exception {
        int programmaticOuterId = baseId() + 251;
        int proxyMiddleId = baseId() + 252;
        int proxyInnerId = baseId() + 253;
        TransactionManager tm = txManager();
        TransactionStatus outer = tm.begin(Propagation.REQUIRED);

        insertUser(programmaticOuterId, "NXN-TX-Proxy-Level-One");
        callerProxy().requiresNew_callRequired(proxyMiddleId, proxyInnerId);
        tm.rollBack(outer);

        assertEquals(0, countById(programmaticOuterId));
        assertEquals(1, countById(proxyMiddleId));
        assertEquals(1, countById(proxyInnerId));
    }
}
