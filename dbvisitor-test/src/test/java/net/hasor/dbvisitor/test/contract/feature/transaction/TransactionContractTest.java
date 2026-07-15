package net.hasor.dbvisitor.test.contract.feature.transaction;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.test.contract.material.service.CallerTransactionService;
import net.hasor.dbvisitor.test.contract.material.service.UserTransactionService;
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionCallback;
import net.hasor.dbvisitor.transaction.TransactionCallbackWithoutResult;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.TransactionTemplate;
import net.hasor.dbvisitor.transaction.TransactionTemplateManager;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@NxnContract
public abstract class TransactionContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 910000;
    }

    @Before
    public void requireTransactionFeature() {
        requiresNxnFeature(FeatureId.TRANSACTION);
    }

    @After
    public void resetOracleTransactionState() {
        if (isOracle()) {
            OneApiDataSourceManager.reset();
            dataSource = null;
        }
    }

    private TransactionManager txManager() {
        return TransactionHelper.txManager(dataSource);
    }

    private TransactionTemplate txTemplate() {
        return new TransactionTemplateManager(txManager());
    }

    private void insertUser(int id, String name) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(30);
        user.setEmail(name + "@nxn.test");
        user.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
    }

    private long countById(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, id).queryForCount();
    }

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

    private UserInfo user(int id, String name) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(30);
        user.setEmail(name + "@nxn.test");
        user.setCreateTime(new Date());
        return user;
    }

    private Session sessionWithStatementMapper() throws Exception {
        Configuration configuration = newConfiguration();
        configuration.loadMapper("/mapper/StatementTestMapper.xml");
        return configuration.newSession(dataSource);
    }

    private Map<String, Object> userParams(int id, String name, int age) {
        return mapOf("id", id, "name", name, "age", age, "email", name + "@nxn.test");
    }

    private Map<String, Object> mapOf(Object... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], pairs[i + 1]);
        }
        return map;
    }

    private void assertIsolationCommit(int id, Isolation isolation, String name) throws SQLException {
        TransactionManager tm = txManager();
        TransactionStatus status = tm.begin(Propagation.REQUIRED, isolation);
        assertEquals(isolation, status.getIsolationLevel());
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, @{macro, currentTimestamp})", new Object[] { id, name, 25 });
        tm.commit(status);
        assertEquals(1, countById(id));
    }

    private UserTransactionService userProxy() {
        UserTransactionService rawUser = new UserTransactionService(lambdaTemplate);
        return TransactionHelper.support(rawUser, dataSource);
    }

    private CallerTransactionService callerProxy() {
        CallerTransactionService rawCaller = new CallerTransactionService(userProxy(), lambdaTemplate);
        return TransactionHelper.support(rawCaller, dataSource);
    }
}
