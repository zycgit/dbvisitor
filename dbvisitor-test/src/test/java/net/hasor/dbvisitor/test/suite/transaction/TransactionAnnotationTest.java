package net.hasor.dbvisitor.test.suite.transaction;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.test.service.UserTransactionService;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @Transactional 注解式事务测试 — 通过 TransactionHelper.support() 动态代理
 * <p>覆盖：
 * <ul>
 *   <li>基本提交/回滚（@Transactional 默认 REQUIRED）</li>
 *   <li>各种传播行为注解（在编程式外层事务中调用注解方法）</li>
 *   <li>readOnly / noRollbackFor 特殊属性</li>
 *   <li>隔离级别注解</li>
 *   <li>无注解方法不被拦截</li>
 *   <li>service 层多层架构模式</li>
 * </ul>
 */
public class TransactionAnnotationTest extends AbstractOneApiTest {

    private UserTransactionService service;

    @Before
    public void initService() {
        UserTransactionService raw = new UserTransactionService(lambdaTemplate);
        this.service = TransactionHelper.support(raw, dataSource);
    }

    private TransactionManager getTxManager() {
        return TransactionHelper.txManager(dataSource);
    }

    private long countById(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, id).queryForCount();
    }

    // ==================== 基本提交/回滚 ====================

    /**
     * @Transactional 默认 REQUIRED：方法正常结束 → 事务自动提交
     */
    @Test
    public void testAnnotation_BasicCommit() throws Exception {
        service.createUser(62001, "AnnoCommit");
        assertEquals(1, countById(62001));
    }

    /**
     * @Transactional 默认 REQUIRED：方法抛异常 → 事务自动回滚
     */
    @Test
    public void testAnnotation_ExceptionRollback() throws Exception {
        try {
            service.createUserThenFail(62002, "AnnoRollback");
            fail("应抛出 RuntimeException");
        } catch (RuntimeException e) {
            // 期望
        }
        assertEquals(0, countById(62002));
    }

    // ==================== 传播行为 ====================

    /**
     * REQUIRES_NEW：在编程式外层事务内调用 → 内层独立提交，外层回滚不影响内层
     */
    @Test
    public void testAnnotation_RequiresNew_Independent() throws Exception {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);

        service.createUserInNewTx(62011, "AnnoRN");

        tm.rollBack(outer);

        // 内层 REQUIRES_NEW 独立提交，不受外层回滚影响
        assertEquals(1, countById(62011));
    }

    /**
     * NESTED：在编程式外层事务内调用 → 嵌套事务异常回滚到 Savepoint，外层提交
     */
    @Test
    public void testAnnotation_Nested_SavepointRollback() throws Exception {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);

        // 嵌套正常插入
        service.createUserNested(62021, "AnnoNested1");

        // 嵌套抛异常 → 回滚到 Savepoint
        try {
            service.createUserNestedThenFail(62022, "AnnoNested2");
        } catch (RuntimeException e) {
            // 期望
        }

        tm.commit(outer);

        assertEquals(1, countById(62021)); // 正常嵌套提交
        assertEquals(0, countById(62022)); // 异常嵌套回滚
    }

    /**
     * SUPPORTS：在有外层事务时加入，外层回滚 → 内层数据一起回滚
     */
    @Test
    public void testAnnotation_Supports_WithTx() throws Exception {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        service.createUserSupports(62031, "AnnoSup");
        tm.rollBack(outer);

        assertEquals(0, countById(62031));
    }

    /**
     * SUPPORTS：无外层事务时以非事务方式执行 → 数据自动提交
     */
    @Test
    public void testAnnotation_Supports_WithoutTx() throws Exception {
        service.createUserSupports(62032, "AnnoSupNoTx");
        assertEquals(1, countById(62032));
    }

    /**
     * NOT_SUPPORTED：挂起外层事务 → 内层数据独立提交，外层回滚不影响内层
     */
    @Test
    public void testAnnotation_NotSupported_SuspendsOuter() throws Exception {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        service.createUserNotSupported(62041, "AnnoNotSup");
        tm.rollBack(outer);

        assertEquals(1, countById(62041)); // NOT_SUPPORTED 数据未受外层回滚影响
    }

    /**
     * MANDATORY：在有外层事务时正常加入
     */
    @Test
    public void testAnnotation_Mandatory_WithTx() throws Exception {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        service.createUserMandatory(62051, "AnnoMandOK");
        tm.commit(outer);

        assertEquals(1, countById(62051));
    }

    /**
     * MANDATORY：无外层事务时抛出异常
     */
    @Test
    public void testAnnotation_Mandatory_WithoutTx() throws Exception {
        try {
            service.createUserMandatory(62052, "AnnoMandFail");
            fail("MANDATORY 在无事务环境下应抛异常");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(0, countById(62052));
    }

    /**
     * NEVER：无事务时正常执行
     */
    @Test
    public void testAnnotation_Never_WithoutTx() throws Exception {
        service.createUserNever(62061, "AnnoNeverOK");
        assertEquals(1, countById(62061));
    }

    /**
     * NEVER：有事务时抛出异常
     */
    @Test
    public void testAnnotation_Never_WithTx() throws Exception {
        TransactionManager tm = getTxManager();
        TransactionStatus outer = tm.begin(Propagation.REQUIRED);

        try {
            service.createUserNever(62062, "AnnoNeverFail");
            fail("NEVER 在有事务环境下应抛异常");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        } finally {
            tm.rollBack(outer);
        }

        assertEquals(0, countById(62062));
    }

    // ==================== 特殊属性 ====================

    /**
     * readOnly = true：方法正常结束，但提交时自动回滚 → 数据不持久化
     */
    @Test
    public void testAnnotation_ReadOnly() throws Exception {
        service.createUserReadOnly(62071, "AnnoReadOnly");
        assertEquals(0, countById(62071));
    }

    /**
     * noRollbackFor = IAE：抛出 IllegalArgumentException → 不回滚，数据提交
     */
    @Test
    public void testAnnotation_NoRollbackFor_MatchingException() throws Exception {
        try {
            service.createUserNoRollbackForIAE(62081, "AnnoNoRollbackIAE");
            fail("应抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // 期望：IAE 在 noRollbackFor 列表中
        }
        assertEquals(1, countById(62081)); // 数据应被提交
    }

    /**
     * noRollbackFor = IAE，但抛出 RuntimeException → 仍然回滚
     */
    @Test
    public void testAnnotation_NoRollbackFor_NonMatchingException() throws Exception {
        try {
            service.createUserRollbackForRTE(62082, "AnnoRollbackRTE");
            fail("应抛出 RuntimeException");
        } catch (RuntimeException e) {
            // 期望：RTE 不在 noRollbackFor 列表中
        }
        assertEquals(0, countById(62082)); // 数据应被回滚
    }

    // ==================== 隔离级别 ====================

    /**
     * isolation = SERIALIZABLE：指定隔离级别正常提交
     */
    @Test
    public void testAnnotation_Isolation_Serializable() throws Exception {
        service.createUserSerializable(62091, "AnnoSerial");
        assertEquals(1, countById(62091));
    }

    /**
     * isolation = READ_COMMITTED：指定隔离级别正常提交
     */
    @Test
    public void testAnnotation_Isolation_ReadCommitted() throws Exception {
        service.createUserReadCommitted(62092, "AnnoRC");
        assertEquals(1, countById(62092));
    }

    // ==================== 编程式 + 注解混合 ====================

    /**
     * 编程式外层 REQUIRED + 注解内层 REQUIRED：内层加入外层，外层回滚 → 全部回滚
     */
    @Test
    public void testMix_ProgrammaticOuter_AnnotationRequired_Rollback() throws Exception {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        service.createUser(62101, "MixReqInner");
        tm.rollBack(outer);

        assertEquals(0, countById(62101)); // 内层也被外层回滚
    }

    /**
     * 编程式外层 REQUIRED + 注解内层 REQUIRES_NEW + 外层回滚 → 内层独立保留
     */
    @Test
    public void testMix_ProgrammaticOuter_AnnotationRequiresNew() throws Exception {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        service.createUserInNewTx(62102, "MixRNInner");
        tm.rollBack(outer);

        assertEquals(1, countById(62102)); // REQUIRES_NEW 独立提交
    }
}
