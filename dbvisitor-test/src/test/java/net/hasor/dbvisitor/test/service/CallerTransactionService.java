package net.hasor.dbvisitor.test.service;

import java.sql.SQLException;
import java.util.Date;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.Transactional;

/**
 * 调用方 Service — 用于测试"方法调用方法"的传播属性组合
 * <p>每个方法都标注了 @Transactional 指定外层传播属性，
 * 内部调用 UserTransactionService 的方法（通过代理）触发内层传播属性。
 * <p>代理机制：CallerTransactionService 和 UserTransactionService 都通过
 * TransactionHelper.support() 创建动态代理，拦截 @Transactional 方法。
 * 外层方法通过代理调用内层方法时，内层也会经过自己的事务拦截器，
 * 因此两层的传播属性都会生效。
 */
public class CallerTransactionService {

    private final UserTransactionService userService;
    private final LambdaTemplate lambdaTemplate;

    /** 无参构造器 — Cobble ASM 动态代理创建子类时需要 */
    public CallerTransactionService() {
        this.userService = null;
        this.lambdaTemplate = null;
    }

    public CallerTransactionService(UserTransactionService userService, LambdaTemplate lambdaTemplate) {
        this.userService = userService;
        this.lambdaTemplate = lambdaTemplate;
    }

    // ====================== REQUIRED 外层 ======================

    /** REQUIRED → REQUIRED：正常完成，两层都提交 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callRequired(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        userService.createUser(innerId, "InnerReq");
    }

    /** REQUIRED → REQUIRED：内层抛异常，异常向上传播 → 两层都回滚 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callRequired_fail(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        userService.createUserThenFail(innerId, "InnerReqFail"); // throws RuntimeException
    }

    /**
     * REQUIRED → REQUIRED：内层抛异常，外层捕获异常
     * <p>dbVisitor 特有行为：内层 REQUIRED 加入外层事务，内层拦截器的 rollBack
     * 对加入的事务是空操作（isNewConnection=false），因此外层捕获异常后仍可正常提交。
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callRequired_innerFailCaught(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        try {
            userService.createUserThenFail(innerId, "InnerReqFail");
        } catch (RuntimeException ignored) {
            // 外层捕获内层异常，继续执行
        }
    }

    /** REQUIRED → REQUIRES_NEW：正常完成，两个独立事务都提交 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callRequiresNew(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        userService.createUserInNewTx(innerId, "InnerRN");
    }

    /** REQUIRED → REQUIRES_NEW：外层抛异常 → 内层独立提交，外层回滚 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callRequiresNew_outerFail(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        userService.createUserInNewTx(innerId, "InnerRN");
        throw new RuntimeException("outer fail after REQUIRES_NEW inner");
    }

    /** REQUIRED → REQUIRES_NEW：内层抛异常（独立事务回滚），外层捕获后正常提交 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callRequiresNew_innerFailCaught(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        try {
            userService.createUserInNewTxThenFail(innerId, "InnerRNFail");
        } catch (RuntimeException ignored) {
            // 内层独立事务已回滚，外层继续
        }
    }

    /** REQUIRED → NESTED：正常完成，Savepoint 释放，数据随外层一起提交 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callNested(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        userService.createUserNested(innerId, "InnerNest");
    }

    /** REQUIRED → NESTED：内层抛异常（回滚到 Savepoint），外层捕获后正常提交 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callNested_innerFailCaught(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        try {
            userService.createUserNestedThenFail(innerId, "InnerNestFail");
        } catch (RuntimeException ignored) {
            // Savepoint 回滚后外层继续
        }
    }

    /** REQUIRED → NESTED：外层抛异常 → 两层都回滚（嵌套在外层事务内） */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callNested_outerFail(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        userService.createUserNested(innerId, "InnerNest");
        throw new RuntimeException("outer fail after NESTED inner");
    }

    /** REQUIRED → NOT_SUPPORTED：外层抛异常 → 内层已自动提交不受影响 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callNotSupported_outerFail(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        userService.createUserNotSupported(innerId, "InnerNotSup");
        throw new RuntimeException("outer fail after NOT_SUPPORTED inner");
    }

    /** REQUIRED → SUPPORTS：外层抛异常 → 内层加入外层事务，两层都回滚 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callSupports_outerFail(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        userService.createUserSupports(innerId, "InnerSup");
        throw new RuntimeException("outer fail after SUPPORTS inner");
    }

    /** REQUIRED → MANDATORY：正常完成（MANDATORY 要求有事务，已被外层 REQUIRED 满足） */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callMandatory(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        userService.createUserMandatory(innerId, "InnerMand");
    }

    /** REQUIRED → NEVER：内层在事务环境中执行 → 抛出异常 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void required_callNever(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterReq");
        userService.createUserNever(innerId, "InnerNvr"); // begin(NEVER) throws
    }

    // ====================== REQUIRES_NEW 外层 ======================

    /** REQUIRES_NEW → REQUIRED：正常完成，内层加入外层的新事务 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNew_callRequired(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterRN");
        userService.createUser(innerId, "InnerReq");
    }

    /** REQUIRES_NEW → REQUIRED：外层抛异常 → 两层都回滚（内层加入了外层新事务） */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNew_callRequired_outerFail(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterRN");
        userService.createUser(innerId, "InnerReq");
        throw new RuntimeException("outer fail in REQUIRES_NEW");
    }

    /** REQUIRES_NEW → REQUIRES_NEW：正常完成，两个完全独立的事务 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNew_callRequiresNew(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterRN");
        userService.createUserInNewTx(innerId, "InnerRN");
    }

    /** REQUIRES_NEW → REQUIRES_NEW：外层抛异常 → 内层独立提交不受影响 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNew_callRequiresNew_outerFail(int outerId, int innerId) throws SQLException {
        doInsert(outerId, "OuterRN");
        userService.createUserInNewTx(innerId, "InnerRN");
        throw new RuntimeException("outer fail in REQUIRES_NEW");
    }

    // ====================== 内部工具 ======================

    private int doInsert(int id, String name) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setCreateTime(new Date());
        return lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
    }
}
