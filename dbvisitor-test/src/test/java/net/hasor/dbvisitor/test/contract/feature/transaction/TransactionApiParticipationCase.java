/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.transaction;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class TransactionApiParticipationCase extends TransactionSupport {
    // 能力归属：数据库事务 / 事务使用方式 / 跨 API 事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_MIXED_JDBC_LAMBDA, column = "transactions/transaction-apis/shared-transactions")
    public void mixedJdbcAndLambda_shouldShareCommitAndRollbackBoundary() throws SQLException {
        int commitJdbcId = baseId() + 101;
        int commitLambdaId = baseId() + 102;
        int rollbackJdbcId = baseId() + 103;
        int rollbackLambdaId = baseId() + 104;
        TransactionManager tm = txManager();

        TransactionStatus commit = tm.begin(Propagation.REQUIRED);
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, @{macro, currentTimestamp})", new Object[] { commitJdbcId, "NXN-TX-Mixed-Jdbc", 25 });
        insertUser(commitLambdaId, "NXN-TX-Mixed-Lambda");
        assertEquals(1, countById(commitJdbcId));
        assertEquals(1, countById(commitLambdaId));
        tm.commit(commit);

        TransactionStatus rollback = tm.begin(Propagation.REQUIRED);
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, @{macro, currentTimestamp})", new Object[] { rollbackJdbcId, "NXN-TX-Mixed-Jdbc-Rb", 25 });
        insertUser(rollbackLambdaId, "NXN-TX-Mixed-Lambda-Rb");
        tm.rollBack(rollback);

        assertEquals(1, countById(commitJdbcId));
        assertEquals(1, countById(commitLambdaId));
        assertEquals(0, countById(rollbackJdbcId));
        assertEquals(0, countById(rollbackLambdaId));
    }

    // 能力归属：数据库事务 / 事务使用方式 / 跨 API 事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_MIXED_SESSION_LAMBDA, column = "transactions/transaction-apis/shared-transactions")
    public void mixedSessionStatementAndLambda_shouldShareCommitAndRollbackBoundary() throws Exception {
        int commitSessionId = baseId() + 111;
        int commitLambdaId = baseId() + 112;
        int rollbackSessionId = baseId() + 113;
        int rollbackLambdaId = baseId() + 114;
        Session session = sessionWithStatementMapper();
        TransactionManager tm = txManager();

        TransactionStatus commit = tm.begin(Propagation.REQUIRED);
        session.executeStatement("StatementTestMapper.insertUserWithId", userParams(commitSessionId, "NXN-TX-Mixed-Session", 30));
        insertUser(commitLambdaId, "NXN-TX-Mixed-Session-Lambda");
        List<UserInfo> committedInTx = session.queryStatement("StatementTestMapper.queryUserById", mapOf("id", commitSessionId));
        assertEquals(1, committedInTx.size());
        assertEquals(1, countById(commitLambdaId));
        tm.commit(commit);

        TransactionStatus rollback = tm.begin(Propagation.REQUIRED);
        session.executeStatement("StatementTestMapper.insertUserWithId", userParams(rollbackSessionId, "NXN-TX-Mixed-Session-Rb", 30));
        insertUser(rollbackLambdaId, "NXN-TX-Mixed-Session-Lambda-Rb");
        tm.rollBack(rollback);

        assertEquals(1, countById(commitSessionId));
        assertEquals(1, countById(commitLambdaId));
        assertEquals(0, countById(rollbackSessionId));
        assertEquals(0, countById(rollbackLambdaId));
    }

    // 能力归属：数据库事务 / 事务使用方式 / 跨 API 事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_MIXED_BASEMAPPER_LAMBDA, column = "transactions/transaction-apis/shared-transactions")
    public void mixedBaseMapperAndLambda_shouldSeeEachOtherInsideOneTransaction() throws Exception {
        int mapperId = baseId() + 121;
        int lambdaId = baseId() + 122;
        int rollbackMapperId = baseId() + 123;
        int rollbackLambdaId = baseId() + 124;
        BaseMapper<UserInfo> mapper = newSession().createBaseMapper(UserInfo.class);
        TransactionManager tm = txManager();

        TransactionStatus status = tm.begin(Propagation.REQUIRED);
        assertEquals(1, mapper.insert(user(mapperId, "NXN-TX-Mixed-BaseMapper")));
        insertUser(lambdaId, "NXN-TX-Mixed-BaseMapper-Lambda");

        UserInfo lambdaUserFromMapper = mapper.selectById(lambdaId);
        assertNotNull(lambdaUserFromMapper);
        assertEquals("NXN-TX-Mixed-BaseMapper-Lambda", lambdaUserFromMapper.getName());
        assertEquals(1, countById(mapperId));
        tm.commit(status);

        assertEquals(1, countById(mapperId));
        assertEquals(1, countById(lambdaId));

        TransactionStatus rollback = tm.begin(Propagation.REQUIRED);
        try {
            assertEquals(1, mapper.insert(user(rollbackMapperId, "NXN-TX-Mixed-BaseMapper-Rb")));
            insertUser(rollbackLambdaId, "NXN-TX-Mixed-BaseMapper-Lambda-Rb");
            assertEquals("NXN-TX-Mixed-BaseMapper-Lambda-Rb", mapper.selectById(rollbackLambdaId).getName());
            assertEquals(1, countById(rollbackMapperId));
        } finally {
            tm.rollBack(rollback);
        }
        assertEquals(0, countById(rollbackMapperId));
        assertEquals(0, countById(rollbackLambdaId));
        assertNull(mapper.selectById(rollbackMapperId));
        assertNull(mapper.selectById(rollbackLambdaId));
    }

    // 能力归属：数据库事务 / 事务使用方式 / 跨 API 事务。
    @Test
    @Capability(value = CapabilityId.TRANSACTION_MIXED_MAPPER_PROXIES, column = "transactions/transaction-apis/shared-transactions")
    public void mixedMapperProxies_shouldShareCommitAndRollbackAcrossSessions() throws Exception {
        int annotationCommitId = baseId() + 151;
        int xmlCommitId = baseId() + 152;
        int annotationRollbackId = baseId() + 153;
        int xmlRollbackId = baseId() + 154;
        SessionUserMapper annotationMapper = newSession().createMapper(SessionUserMapper.class);
        SessionRefUserMapper xmlMapper = newSession().createMapper(SessionRefUserMapper.class);
        TransactionManager manager = txManager();

        TransactionStatus committed = manager.begin(Propagation.REQUIRED);
        try {
            assertEquals(1, annotationMapper.insertUser(user(annotationCommitId, "NXN-TX-Proxy-Annotation")));
            assertEquals(1, xmlMapper.insertUser(user(xmlCommitId, "NXN-TX-Proxy-XML")));
            assertEquals("NXN-TX-Proxy-Annotation", xmlMapper.queryUserById(annotationCommitId).getName());
            assertEquals("NXN-TX-Proxy-XML", annotationMapper.selectById(xmlCommitId).getName());
            manager.commit(committed);
        } finally {
            if (!committed.isCompleted()) {
                manager.rollBack(committed);
            }
        }
        assertEquals(1, countById(annotationCommitId));
        assertEquals(1, countById(xmlCommitId));

        TransactionStatus rolledBack = manager.begin(Propagation.REQUIRED);
        try {
            assertEquals(1, annotationMapper.insertUser(user(annotationRollbackId, "NXN-TX-Proxy-Annotation-Rb")));
            assertEquals(1, xmlMapper.insertUser(user(xmlRollbackId, "NXN-TX-Proxy-XML-Rb")));
            assertEquals("NXN-TX-Proxy-Annotation-Rb", xmlMapper.queryUserById(annotationRollbackId).getName());
            assertEquals("NXN-TX-Proxy-XML-Rb", annotationMapper.selectById(xmlRollbackId).getName());
        } finally {
            manager.rollBack(rolledBack);
        }
        assertEquals(0, countById(annotationRollbackId));
        assertEquals(0, countById(xmlRollbackId));
        assertNull(annotationMapper.selectById(annotationRollbackId));
        assertNull(xmlMapper.queryUserById(xmlRollbackId));
        assertFalse(manager.hasTransaction());
    }
}
