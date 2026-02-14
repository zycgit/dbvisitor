package net.hasor.dbvisitor.test.suite.transaction;

import java.sql.SQLException;
import java.util.Date;

import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.test.service.CallerTransactionService;
import net.hasor.dbvisitor.test.service.UserTransactionService;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 传播属性组合测试 — 方法调用方法，两层都有 @Transactional 传播属性
 * <p>通过 CallerTransactionService（外层代理）调用 UserTransactionService（内层代理），
 * 每个方法都经过 TransactionHelper.support() 动态代理拦截，自动管理事务。
 * <p>这是传播属性最核心的测试场景：真实业务中 ServiceA 调用 ServiceB，
 * 两个方法各自声明不同的传播行为，验证它们的交互是否正确。
 * <p>覆盖以下组合：
 * <ul>
 *   <li><b>REQUIRED 外层</b>：调用 REQUIRED / REQUIRES_NEW / NESTED / NOT_SUPPORTED / SUPPORTS / MANDATORY / NEVER 内层</li>
 *   <li><b>REQUIRES_NEW 外层</b>：调用 REQUIRED / REQUIRES_NEW 内层</li>
 *   <li><b>三层嵌套</b>：编程式 REQUIRED → 注解 REQUIRES_NEW → 注解 REQUIRED</li>
 *   <li>各组合下的<b>内层失败</b>、<b>外层失败</b>、<b>内层失败外层捕获</b>等场景</li>
 * </ul>
 */
public class TransactionPropagationCombinationTest extends AbstractOneApiTest {

    private CallerTransactionService callerProxy;

    @Before
    public void initServices() {
        UserTransactionService rawUser = new UserTransactionService(lambdaTemplate);
        UserTransactionService userProxy = TransactionHelper.support(rawUser, dataSource);

        CallerTransactionService rawCaller = new CallerTransactionService(userProxy, lambdaTemplate);
        this.callerProxy = TransactionHelper.support(rawCaller, dataSource);
    }

    private TransactionManager getTxManager() {
        return TransactionHelper.txManager(dataSource);
    }

    private long countById(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, id).queryForCount();
    }

    private void insertUser(int id, String name) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
    }

    // =====================================================================
    //  REQUIRED 外层 → 各种内层传播行为
    // =====================================================================

    /**
     * REQUIRED → REQUIRED：两层都正常完成 → 共享同一事务，数据全部持久化
     * <p>内层 REQUIRED 加入外层事务，两层 INSERT 在同一个连接上执行，
     * 外层拦截器 commit 时一次性提交所有数据。
     */
    @Test
    public void testRequired_Required_BothCommit() throws Exception {
        callerProxy.required_callRequired(64001, 64002);

        assertEquals(1, countById(64001));
        assertEquals(1, countById(64002));
    }

    /**
     * REQUIRED → REQUIRED：内层抛异常，异常传播到外层拦截器 → 两层数据全部回滚
     * <p>流程：内层 throws → 内层拦截器 setRollback + rollBack(joinedStatus)=空操作 →
     * 异常传播 → 外层拦截器 setRollback + rollBack(outerStatus)=真正回滚 →
     * connection.rollback()，所有数据丢失。
     */
    @Test
    public void testRequired_Required_InnerFailPropagates_BothRollback() throws Exception {
        try {
            callerProxy.required_callRequired_fail(64003, 64004);
            fail("内层 createUserThenFail 应抛出 RuntimeException");
        } catch (RuntimeException e) {
            // 期望
        }

        assertEquals(0, countById(64003)); // 外层数据回滚
        assertEquals(0, countById(64004)); // 内层数据回滚（虽然内层在异常前已 INSERT）
    }

    /**
     * REQUIRED → REQUIRED：内层抛异常，外层方法捕获异常 → 两层数据都提交
     * <p><b>dbVisitor 特有行为</b>：内层 REQUIRED 加入外层事务时 isNewConnection=false，
     * 内层拦截器的 rollBack(innerStatus) 对加入的事务是空操作（既不回滚连接也不标记底层事务）。
     * 外层方法捕获异常后正常返回，外层拦截器 commit(outerStatus) 正常提交。
     * 内层方法在抛异常前执行的 INSERT 仍在连接的事务缓冲区中，随外层一起提交。
     * <p><b>与 Spring 框架的差异</b>：Spring 内层 REQUIRED 回滚时会标记底层事务为 rollback-only，
     * 外层 commit 时抛出 UnexpectedRollbackException。dbVisitor 没有此机制。
     */
    @Test
    public void testRequired_Required_InnerFailCaught_BothCommit() throws Exception {
        callerProxy.required_callRequired_innerFailCaught(64005, 64006);

        assertEquals(1, countById(64005)); // 外层数据提交
        assertEquals(1, countById(64006)); // 内层数据也提交（即使内层方法曾抛异常）
    }

    /**
     * REQUIRED → REQUIRES_NEW：两层都正常完成 → 两个独立事务各自提交
     */
    @Test
    public void testRequired_RequiresNew_BothCommit() throws Exception {
        callerProxy.required_callRequiresNew(64007, 64008);

        assertEquals(1, countById(64007));
        assertEquals(1, countById(64008));
    }

    /**
     * REQUIRED → REQUIRES_NEW：外层抛异常 → 内层独立事务已提交不受影响，外层回滚
     * <p>内层 REQUIRES_NEW 挂起外层事务，在独立连接上执行并提交。
     * 外层后续抛异常触发回滚，但内层数据已经在另一个连接上 commit，不可逆。
     */
    @Test
    public void testRequired_RequiresNew_OuterFail_InnerPreserved() throws Exception {
        try {
            callerProxy.required_callRequiresNew_outerFail(64009, 64010);
            fail("外层应抛出 RuntimeException");
        } catch (RuntimeException e) {
            // 期望
        }

        assertEquals(0, countById(64009)); // 外层数据回滚
        assertEquals(1, countById(64010)); // 内层 REQUIRES_NEW 独立提交，不受影响
    }

    /**
     * REQUIRED → REQUIRES_NEW：内层抛异常（独立事务回滚），外层捕获后正常提交
     * <p>内层 REQUIRES_NEW 在独立连接上执行。内层 throws → 内层拦截器 rollBack
     * （isNewConnection=true → 真正回滚独立连接）。异常传播到外层方法被捕获，
     * 外层继续正常返回 → 外层拦截器 commit → 外层数据持久化。
     */
    @Test
    public void testRequired_RequiresNew_InnerFailCaught_OuterPreserved() throws Exception {
        callerProxy.required_callRequiresNew_innerFailCaught(64011, 64012);

        assertEquals(1, countById(64011)); // 外层数据提交
        assertEquals(0, countById(64012)); // 内层独立事务已回滚
    }

    /**
     * REQUIRED → NESTED：两层都正常完成 → Savepoint 释放，数据随外层一起提交
     * <p>内层 NESTED 在外层连接上创建 Savepoint。内层完成后释放 Savepoint（不提交）。
     * 外层 commit 时整个连接事务提交，两条数据一起持久化。
     */
    @Test
    public void testRequired_Nested_BothCommit() throws Exception {
        callerProxy.required_callNested(64013, 64014);

        assertEquals(1, countById(64013));
        assertEquals(1, countById(64014));
    }

    /**
     * REQUIRED → NESTED：内层抛异常（回滚到 Savepoint），外层捕获后正常提交
     * <p>内层 NESTED throws → 拦截器 rollBack(nestedStatus) → hasSavepoint=true →
     * rollbackToSavepoint()（撤销内层 INSERT）。异常被外层捕获，外层继续提交。
     * 外层数据保留，内层数据已被 Savepoint 回滚。
     */
    @Test
    public void testRequired_Nested_InnerFailCaught_OuterPreserved() throws Exception {
        callerProxy.required_callNested_innerFailCaught(64015, 64016);

        assertEquals(1, countById(64015)); // 外层数据提交
        assertEquals(0, countById(64016)); // 内层数据被 Savepoint 回滚
    }

    /**
     * REQUIRED → NESTED：外层抛异常 → 两层数据全部回滚
     * <p>内层 NESTED 完成后释放 Savepoint，但数据仍在外层事务中。
     * 外层抛异常 → 外层拦截器 rollBack → connection.rollback() → 两条数据全部丢失。
     */
    @Test
    public void testRequired_Nested_OuterFail_BothRollback() throws Exception {
        try {
            callerProxy.required_callNested_outerFail(64017, 64018);
            fail("外层应抛出 RuntimeException");
        } catch (RuntimeException e) {
            // 期望
        }

        assertEquals(0, countById(64017)); // 外层数据回滚
        assertEquals(0, countById(64018)); // 内层数据也随外层回滚
    }

    /**
     * REQUIRED → NOT_SUPPORTED：外层抛异常 → 内层数据已自动提交不受影响
     * <p>内层 NOT_SUPPORTED 挂起外层事务，以非事务方式（auto-commit）执行。
     * 内层 INSERT 立即持久化。外层后续抛异常回滚，但内层数据已提交。
     */
    @Test
    public void testRequired_NotSupported_OuterFail_InnerPreserved() throws Exception {
        try {
            callerProxy.required_callNotSupported_outerFail(64019, 64020);
            fail("外层应抛出 RuntimeException");
        } catch (RuntimeException e) {
            // 期望
        }

        assertEquals(0, countById(64019)); // 外层数据回滚
        assertEquals(1, countById(64020)); // 内层 NOT_SUPPORTED 数据已自动提交
    }

    /**
     * REQUIRED → SUPPORTS：外层抛异常 → 两层数据都回滚
     * <p>内层 SUPPORTS 在有事务环境下加入外层事务（类似 REQUIRED 的 join）。
     * 内外层在同一个连接、同一个事务中。外层回滚 → 全部丢失。
     */
    @Test
    public void testRequired_Supports_OuterFail_BothRollback() throws Exception {
        try {
            callerProxy.required_callSupports_outerFail(64021, 64022);
            fail("外层应抛出 RuntimeException");
        } catch (RuntimeException e) {
            // 期望
        }

        assertEquals(0, countById(64021)); // 外层数据回滚
        assertEquals(0, countById(64022)); // 内层 SUPPORTS 加入外层，一起回滚
    }

    /**
     * REQUIRED → MANDATORY：两层正常完成 → MANDATORY 要求有事务，被外层 REQUIRED 满足
     * <p>MANDATORY 的语义是"必须在已有事务中运行"。外层 REQUIRED 提供了事务环境，
     * MANDATORY 加入外层事务，行为等同于 REQUIRED join。
     */
    @Test
    public void testRequired_Mandatory_BothCommit() throws Exception {
        callerProxy.required_callMandatory(64023, 64024);

        assertEquals(1, countById(64023));
        assertEquals(1, countById(64024));
    }

    /**
     * REQUIRED → NEVER：内层在事务环境中被调用 → 抛出异常
     * <p>NEVER 要求必须在无事务环境下执行。外层 REQUIRED 已开启事务，
     * 内层 begin(NEVER) 检测到活跃事务 → 抛出 SQLException。
     * 异常传播到外层拦截器 → 外层也回滚。内层方法体从未执行，无数据插入。
     */
    @Test
    public void testRequired_Never_ThrowsException() throws Exception {
        try {
            callerProxy.required_callNever(64025, 64026);
            fail("REQUIRED → NEVER 应当抛出异常");
        } catch (Exception e) {
            // 期望：NEVER 在有事务环境下的 begin() 抛异常
        }

        assertEquals(0, countById(64025)); // 外层数据回滚
        assertEquals(0, countById(64026)); // 内层从未插入
    }

    // =====================================================================
    //  REQUIRES_NEW 外层 → REQUIRED / REQUIRES_NEW 内层
    // =====================================================================

    /**
     * REQUIRES_NEW → REQUIRED：两层都正常完成 → 内层加入外层的新事务，一起提交
     * <p>外层 REQUIRES_NEW 创建独立新事务。内层 REQUIRED 加入该新事务。
     * 行为等同于在一个新事务中执行两次 INSERT。
     */
    @Test
    public void testRequiresNew_Required_BothCommit() throws Exception {
        callerProxy.requiresNew_callRequired(64027, 64028);

        assertEquals(1, countById(64027));
        assertEquals(1, countById(64028));
    }

    /**
     * REQUIRES_NEW → REQUIRED：外层抛异常 → 两层都回滚
     * <p>内层 REQUIRED 加入了外层的新事务。外层抛异常 → 新事务整体回滚。
     */
    @Test
    public void testRequiresNew_Required_OuterFail_BothRollback() throws Exception {
        try {
            callerProxy.requiresNew_callRequired_outerFail(64029, 64030);
            fail("外层应抛出 RuntimeException");
        } catch (RuntimeException e) {
            // 期望
        }

        assertEquals(0, countById(64029)); // 外层数据回滚
        assertEquals(0, countById(64030)); // 内层 REQUIRED 加入外层，一起回滚
    }

    /**
     * REQUIRES_NEW → REQUIRES_NEW：两层都正常完成 → 两个完全独立的事务
     * <p>外层 REQUIRES_NEW 创建新事务 A。内层 REQUIRES_NEW 挂起 A，创建新事务 B。
     * 两个事务在不同连接上各自提交。
     */
    @Test
    public void testRequiresNew_RequiresNew_BothCommit() throws Exception {
        callerProxy.requiresNew_callRequiresNew(64031, 64032);

        assertEquals(1, countById(64031));
        assertEquals(1, countById(64032));
    }

    /**
     * REQUIRES_NEW → REQUIRES_NEW：外层抛异常 → 内层独立提交不受影响
     * <p>内层 REQUIRES_NEW 在独立连接上提交后恢复外层事务。
     * 外层抛异常 → 外层事务回滚。内层数据已在另一连接上 commit，不可逆。
     */
    @Test
    public void testRequiresNew_RequiresNew_OuterFail_InnerPreserved() throws Exception {
        try {
            callerProxy.requiresNew_callRequiresNew_outerFail(64033, 64034);
            fail("外层应抛出 RuntimeException");
        } catch (RuntimeException e) {
            // 期望
        }

        assertEquals(0, countById(64033)); // 外层 REQUIRES_NEW 回滚
        assertEquals(1, countById(64034)); // 内层 REQUIRES_NEW 独立提交
    }

    // =====================================================================
    //  三层嵌套：编程式 REQUIRED → 注解 REQUIRES_NEW → 注解 REQUIRED
    // =====================================================================

    /**
     * 三层事务嵌套：编程式 REQUIRED → 注解 REQUIRES_NEW → 注解 REQUIRED
     * <p>最外层通过 TransactionManager 编程式开启 REQUIRED 事务。
     * 中间层 CallerService.requiresNew_callRequired() 带 @Transactional(REQUIRES_NEW)
     * 会挂起最外层事务并创建独立新事务。最内层 REQUIRED 加入中间层新事务。
     * <p>当最外层编程式回滚时，中间层和最内层的数据已经在独立事务中提交，不受影响。
     */
    @Test
    public void testThreeLevel_Programmatic_RequiresNew_Required() throws Exception {
        TransactionManager tm = getTxManager();
        TransactionStatus outermost = tm.begin(Propagation.REQUIRED);

        // 最外层编程式事务中插入数据
        insertUser(64035, "LevelOne");

        // 中间层 REQUIRES_NEW（挂起最外层）→ 最内层 REQUIRED（加入中间层）
        callerProxy.requiresNew_callRequired(64036, 64037);

        // 回滚最外层编程式事务
        tm.rollBack(outermost);

        assertEquals(0, countById(64035)); // 最外层编程式事务回滚
        assertEquals(1, countById(64036)); // 中间层 REQUIRES_NEW 独立提交
        assertEquals(1, countById(64037)); // 最内层 REQUIRED 加入中间层，一起提交
    }
}
