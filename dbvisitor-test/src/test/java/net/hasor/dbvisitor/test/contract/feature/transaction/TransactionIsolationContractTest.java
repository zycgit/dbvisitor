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
import net.hasor.dbvisitor.test.contract.material.service.UserTransactionService;
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionCallback;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.TransactionTemplate;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class TransactionIsolationContractTest extends TransactionSupport {
    @Test
    @Capability(CapabilityId.TRANSACTION_TEMPLATE_ISOLATION)
    public void template_shouldExposeRequestedIsolationOnTransactionStatus() throws Throwable {
        int id = baseId() + 95;
        TransactionTemplate template = txTemplate();

        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doTransaction(TransactionStatus status) throws Throwable {
                insertUser(id, "NXN-TX-Template-Isolation");
                assertEquals(Isolation.READ_COMMITTED, status.getIsolationLevel());
                return null;
            }
        }, Propagation.REQUIRED, Isolation.READ_COMMITTED);

        assertEquals(1, countById(id));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_ISOLATION_READ_COMMITTED)
    public void isolationReadCommitted_shouldExposeStatusAndCommitWork() throws SQLException {
        assertIsolationCommit(baseId() + 141, Isolation.READ_COMMITTED, "NXN-TX-Isolation-RC");
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_ISOLATION_REPEATABLE_READ)
    public void isolationRepeatableRead_shouldExposeStatusAndCommitWork() throws SQLException {
        requiresNxnFeature(FeatureId.TRANSACTION_REPEATABLE_READ);
        assertIsolationCommit(baseId() + 142, Isolation.REPEATABLE_READ, "NXN-TX-Isolation-RR");
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_ISOLATION_SERIALIZABLE)
    public void isolationSerializable_shouldExposeStatusAndCommitWork() throws SQLException {
        assertIsolationCommit(baseId() + 143, Isolation.SERIALIZABLE, "NXN-TX-Isolation-Ser");
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_ISOLATION_REQUIRES_NEW_RESTORE)
    public void isolationRequiresNew_shouldAllowOuterTransactionToContinueAfterInnerCommit() throws SQLException {
        int innerId = baseId() + 144;
        int outerId = baseId() + 145;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);
        assertEquals(Isolation.READ_COMMITTED, outer.getIsolationLevel());
        TransactionStatus inner = tm.begin(Propagation.REQUIRES_NEW, Isolation.SERIALIZABLE);
        assertEquals(Isolation.SERIALIZABLE, inner.getIsolationLevel());
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, @{macro, currentTimestamp})", new Object[] { innerId, "NXN-TX-Isolation-Inner", 25 });
        tm.commit(inner);
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, @{macro, currentTimestamp})", new Object[] { outerId, "NXN-TX-Isolation-Outer", 25 });
        tm.commit(outer);

        assertEquals(1, countById(innerId));
        assertEquals(1, countById(outerId));
    }

    @Test
    @Capability(CapabilityId.TRANSACTION_ANNOTATION_ISOLATION)
    public void annotationIsolation_shouldCommitWithDeclaredIsolationLevels() throws Exception {
        UserTransactionService service = userProxy();
        int serializableId = baseId() + 321;
        int readCommittedId = baseId() + 322;

        service.createUserSerializable(serializableId, "NXN-TX-Anno-Isolation-Ser");
        service.createUserReadCommitted(readCommittedId, "NXN-TX-Anno-Isolation-RC");

        assertEquals(1, countById(serializableId));
        assertEquals(1, countById(readCommittedId));
    }
}
