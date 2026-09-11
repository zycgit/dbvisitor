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

import org.junit.Test;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class TransactionApiParticipationContractTest extends TransactionSupport {
    @Test
    @Capability(CapabilityId.TRANSACTION_MIXED_JDBC_LAMBDA)
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

    @Test
    @Capability(CapabilityId.TRANSACTION_MIXED_SESSION_LAMBDA)
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

    @Test
    @Capability(CapabilityId.TRANSACTION_MIXED_BASEMAPPER_LAMBDA)
    public void mixedBaseMapperAndLambda_shouldSeeEachOtherInsideOneTransaction() throws Exception {
        int mapperId = baseId() + 121;
        int lambdaId = baseId() + 122;
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
    }
}
