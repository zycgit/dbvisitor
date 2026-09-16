/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.test.contract.material.service.UserTransactionService;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.transaction.*;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class TransactionIsolationCase extends TransactionSupport {
    // 能力归属：数据库事务 / 隔离级别 / 隔离级别。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_TEMPLATE_ISOLATION, column = "transactions/isolation-levels/isolation")
    public void template_shouldExposeRequestedIsolationOnTransactionStatus() throws Throwable {
        int id = baseId() + 95;
        TransactionTemplate template = txTemplate();

        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doTransaction(TransactionStatus status) throws Throwable {
                insertUser(id, "NXN-TX-Template-Isolation");
                assertEquals(Isolation.SERIALIZABLE, status.getIsolationLevel());
                jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
                    assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation());
                    return null;
                });
                return null;
            }
        }, Propagation.REQUIRED, Isolation.SERIALIZABLE);

        assertEquals(1, countById(id));
    }

    // 能力归属：数据库事务 / 隔离级别 / 隔离级别。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_ISOLATION_READ_COMMITTED, column = "transactions/isolation-levels/isolation")
    public void isolationReadCommitted_shouldExposeStatusAndCommitWork() throws SQLException {
        assertIsolationCommit(baseId() + 141, Isolation.READ_COMMITTED, "NXN-TX-Isolation-RC");
    }

    // 能力归属：数据库事务 / 隔离级别 / 隔离级别。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_ISOLATION_REPEATABLE_READ, column = "transactions/isolation-levels/isolation")
    public void isolationRepeatableRead_shouldExposeStatusAndCommitWork() throws SQLException {
        requiresNxnFeature(FeatureId.TRANSACTION_REPEATABLE_READ);
        assertIsolationCommit(baseId() + 142, Isolation.REPEATABLE_READ, "NXN-TX-Isolation-RR");
    }

    // 能力归属：数据库事务 / 隔离级别 / 隔离级别。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_ISOLATION_SERIALIZABLE, column = "transactions/isolation-levels/isolation")
    public void isolationSerializable_shouldExposeStatusAndCommitWork() throws SQLException {
        assertIsolationCommit(baseId() + 143, Isolation.SERIALIZABLE, "NXN-TX-Isolation-Ser");
    }

    // 能力归属：数据库事务 / 隔离级别 / 隔离级别。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_ISOLATION_REQUIRES_NEW_RESTORE, column = "transactions/isolation-levels/isolation")
    public void isolationRequiresNew_shouldAllowOuterTransactionToContinueAfterInnerCommit() throws SQLException {
        int innerId = baseId() + 144;
        int outerId = baseId() + 145;
        TransactionManager tm = txManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);
        try {
            assertEquals(Isolation.READ_COMMITTED, outer.getIsolationLevel());
            assertConnectionIsolation(Isolation.READ_COMMITTED);
            TransactionStatus inner = tm.begin(Propagation.REQUIRES_NEW, Isolation.SERIALIZABLE);
            assertEquals(Isolation.SERIALIZABLE, inner.getIsolationLevel());
            assertConnectionIsolation(Isolation.SERIALIZABLE);
            jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, @{macro, currentTimestamp})", new Object[] { innerId, "NXN-TX-Isolation-Inner", 25 });
            tm.commit(inner);
            assertTrue(inner.isCompleted());
            assertTrue(tm.isTopTransaction(outer));
            assertConnectionIsolation(Isolation.READ_COMMITTED);
            jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, @{macro, currentTimestamp})", new Object[] { outerId, "NXN-TX-Isolation-Outer", 25 });
            tm.commit(outer);
        } finally {
            if (!outer.isCompleted()) {
                tm.rollBack(outer);
            }
        }

        assertEquals(1, countById(innerId));
        assertEquals(1, countById(outerId));
    }

    // 能力归属：数据库事务 / 隔离级别 / 隔离级别。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_ANNOTATION_ISOLATION, column = "transactions/isolation-levels/isolation")
    public void annotationIsolation_shouldCommitWithDeclaredIsolationLevels() throws Exception {
        UserTransactionService target = new UserTransactionService(lambdaTemplate);
        UserTransactionService service = TransactionHelper.support(target, dataSource);
        int serializableId = baseId() + 321;
        int readCommittedId = baseId() + 322;

        // Only annotated methods are forwarded; read the captured value from their target.
        service.createUserSerializable(serializableId, "NXN-TX-Anno-Isolation-Ser");
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, target.getLastIsolationLevel());
        service.createUserReadCommitted(readCommittedId, "NXN-TX-Anno-Isolation-RC");
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, target.getLastIsolationLevel());

        assertEquals(1, countById(serializableId));
        assertEquals(1, countById(readCommittedId));
    }
}
