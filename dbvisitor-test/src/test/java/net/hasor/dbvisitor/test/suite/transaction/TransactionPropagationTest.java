package net.hasor.dbvisitor.test.suite.transaction;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 事务传播行为测试 — 使用 TransactionManager 编程式 API
 * <p>覆盖全部 7 种传播行为：REQUIRED / REQUIRES_NEW / NESTED / SUPPORTS / NOT_SUPPORTED / MANDATORY / NEVER
 * <p>数据操作统一使用 LambdaTemplate，验证双层事务在各传播行为下的提交/回滚组合行为。
 */
public class TransactionPropagationTest extends AbstractOneApiTest {

    private TransactionManager getTxManager() {
        return TransactionHelper.txManager(dataSource);
    }

    private void insertUser(int id, String name) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
    }

    private long countById(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, id).queryForCount();
    }

    // ==================== REQUIRED ====================

    /**
     * REQUIRED + REQUIRED：内层加入外层事务，两层都提交 → 数据持久化
     */
    @Test
    public void testRequired_BothCommit() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(60001, "OuterReq");

        TransactionStatus inner = tm.begin(Propagation.REQUIRED);
        insertUser(60002, "InnerReq");
        tm.commit(inner);

        tm.commit(outer);

        assertEquals(1, countById(60001));
        assertEquals(1, countById(60002));
    }

    /**
     * REQUIRED + REQUIRED：内层"提交"后外层回滚 → 两条数据全部回滚（因为共享同一事务）
     */
    @Test
    public void testRequired_InnerCommit_OuterRollback() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(60003, "OuterReq2");

        TransactionStatus inner = tm.begin(Propagation.REQUIRED);
        insertUser(60004, "InnerReq2");
        tm.commit(inner); // 内层 commit 在 REQUIRED 下不产生真正提交

        tm.rollBack(outer); // 外层回滚 → 全部回滚

        assertEquals(0, countById(60003));
        assertEquals(0, countById(60004));
    }

    // ==================== REQUIRES_NEW ====================

    /**
     * REQUIRES_NEW：内层独立事务提交，外层回滚 → 内层数据保留，外层数据丢失
     */
    @Test
    public void testRequiresNew_InnerCommit_OuterRollback() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(60011, "OuterRN");

        TransactionStatus inner = tm.begin(Propagation.REQUIRES_NEW);
        insertUser(60012, "InnerRN");
        tm.commit(inner); // 独立事务真正提交

        tm.rollBack(outer); // 外层回滚不影响已提交的内层

        assertEquals(0, countById(60011));
        assertEquals(1, countById(60012));
    }

    /**
     * REQUIRES_NEW：内层独立事务回滚，外层提交 → 外层数据保留，内层数据丢失
     */
    @Test
    public void testRequiresNew_InnerRollback_OuterCommit() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(60013, "OuterRN2");

        TransactionStatus inner = tm.begin(Propagation.REQUIRES_NEW);
        insertUser(60014, "InnerRN2");
        tm.rollBack(inner); // 独立事务回滚

        tm.commit(outer); // 外层不受影响

        assertEquals(1, countById(60013));
        assertEquals(0, countById(60014));
    }

    // ==================== NESTED ====================

    /**
     * NESTED：内层通过 Savepoint 回滚，外层提交 → 外层数据保留，嵌套数据丢失
     */
    @Test
    public void testNested_InnerRollback_OuterCommit() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(60021, "OuterNested");

        TransactionStatus nested = tm.begin(Propagation.NESTED);
        insertUser(60022, "InnerNested");
        tm.rollBack(nested); // 回滚到 Savepoint

        tm.commit(outer);

        assertEquals(1, countById(60021));
        assertEquals(0, countById(60022));
    }

    /**
     * NESTED：两层都提交 → 所有数据持久化
     */
    @Test
    public void testNested_BothCommit() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(60023, "OuterNested2");

        TransactionStatus nested = tm.begin(Propagation.NESTED);
        insertUser(60024, "InnerNested2");
        tm.commit(nested); // 释放 Savepoint

        tm.commit(outer);

        assertEquals(1, countById(60023));
        assertEquals(1, countById(60024));
    }

    /**
     * NESTED：外层回滚 → 嵌套事务也跟着回滚（嵌套在外层事务内）
     */
    @Test
    public void testNested_OuterRollback_AllLost() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(60025, "OuterNested3");

        TransactionStatus nested = tm.begin(Propagation.NESTED);
        insertUser(60026, "InnerNested3");
        tm.commit(nested);

        tm.rollBack(outer); // 外层回滚级联

        assertEquals(0, countById(60025));
        assertEquals(0, countById(60026));
    }

    // ==================== SUPPORTS ====================

    /**
     * SUPPORTS + 有外层事务：加入外层事务，外层回滚 → 两条数据都回滚
     */
    @Test
    public void testSupports_WithExistingTx() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(60031, "OuterSup");

        TransactionStatus inner = tm.begin(Propagation.SUPPORTS);
        insertUser(60032, "InnerSup");
        tm.commit(inner);

        tm.rollBack(outer);

        assertEquals(0, countById(60031));
        assertEquals(0, countById(60032));
    }

    /**
     * SUPPORTS + 无外层事务：以非事务方式执行，数据立即可见（自动提交）
     */
    @Test
    public void testSupports_WithoutTx() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus status = tm.begin(Propagation.SUPPORTS);
        insertUser(60033, "SupNoTx");
        tm.commit(status);

        // 非事务模式下数据已自动提交
        assertEquals(1, countById(60033));
    }

    // ==================== NOT_SUPPORTED ====================

    /**
     * NOT_SUPPORTED：挂起外层事务，内层以非事务方式执行 → 内层数据独立于外层
     */
    @Test
    public void testNotSupported_SuspendsOuter() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(60041, "OuterNotSup");

        TransactionStatus inner = tm.begin(Propagation.NOT_SUPPORTED);
        insertUser(60042, "InnerNotSup");
        tm.commit(inner); // 非事务方式，数据已自动提交

        tm.rollBack(outer); // 外层回滚

        assertEquals(0, countById(60041)); // 外层数据回滚
        assertEquals(1, countById(60042)); // 内层数据已提交，不受影响
    }

    // ==================== MANDATORY ====================

    /**
     * MANDATORY + 有外层事务：加入外层事务，正常工作
     */
    @Test
    public void testMandatory_WithTx() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);

        TransactionStatus inner = tm.begin(Propagation.MANDATORY);
        insertUser(60051, "MandatoryOK");
        tm.commit(inner);

        tm.commit(outer);

        assertEquals(1, countById(60051));
    }

    /**
     * MANDATORY + 无外层事务：抛出异常
     */
    @Test
    public void testMandatory_WithoutTx_ThrowsException() throws SQLException {
        TransactionManager tm = getTxManager();

        try {
            tm.begin(Propagation.MANDATORY);
            fail("MANDATORY 在无事务环境下应抛异常");
        } catch (Exception e) {
            // 期望抛出异常
            assertNotNull(e.getMessage());
        }
    }

    // ==================== NEVER ====================

    /**
     * NEVER + 无事务：正常以非事务方式执行
     */
    @Test
    public void testNever_WithoutTx() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus status = tm.begin(Propagation.NEVER);
        insertUser(60061, "NeverNoTx");
        tm.commit(status);

        assertEquals(1, countById(60061));
    }

    /**
     * NEVER + 有外层事务：抛出异常
     */
    @Test
    public void testNever_WithTx_ThrowsException() throws SQLException {
        TransactionManager tm = getTxManager();
        TransactionStatus outer = tm.begin(Propagation.REQUIRED);

        try {
            tm.begin(Propagation.NEVER);
            fail("NEVER 在有事务环境下应抛异常");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        } finally {
            tm.rollBack(outer);
        }
    }
}
