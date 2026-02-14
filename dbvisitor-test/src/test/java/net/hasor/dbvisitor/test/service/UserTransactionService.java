package net.hasor.dbvisitor.test.service;

import java.sql.SQLException;
import java.util.Date;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.Transactional;

/**
 * 带有 @Transactional 注解的事务 Service 层
 * <p>用于验证 TransactionHelper.support() 动态代理下各种事务注解配置的正确行为。
 * 每个方法对应一种传播行为/隔离级别/特殊属性的组合。
 */
public class UserTransactionService {

    private final LambdaTemplate lambdaTemplate;

    /** 无参构造器 — Cobble ASM 动态代理创建子类时需要 */
    public UserTransactionService() {
        this.lambdaTemplate = null;
    }

    public UserTransactionService(LambdaTemplate lambdaTemplate) {
        this.lambdaTemplate = lambdaTemplate;
    }

    // ==================== 传播行为方法 ====================

    /** 默认 REQUIRED 传播 — 有事务则加入，没有则新建 */
    @Transactional
    public int createUser(int id, String name) throws SQLException {
        return doInsert(id, name);
    }

    /** REQUIRED 传播 + 插入后抛异常 → 事务应回滚 */
    @Transactional
    public void createUserThenFail(int id, String name) throws SQLException {
        doInsert(id, name);
        throw new RuntimeException("Intentional error for transaction rollback");
    }

    /** REQUIRES_NEW — 始终挂起当前事务并开启独立新事务 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int createUserInNewTx(int id, String name) throws SQLException {
        return doInsert(id, name);
    }

    /** REQUIRES_NEW + 抛异常 → 独立事务应回滚 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createUserInNewTxThenFail(int id, String name) throws SQLException {
        doInsert(id, name);
        throw new RuntimeException("REQUIRES_NEW intentional error for rollback");
    }

    /** NESTED — 在当前事务内通过 Savepoint 创建子事务 */
    @Transactional(propagation = Propagation.NESTED)
    public int createUserNested(int id, String name) throws SQLException {
        return doInsert(id, name);
    }

    /** NESTED + 插入后抛异常 → 嵌套事务回滚到 Savepoint */
    @Transactional(propagation = Propagation.NESTED)
    public void createUserNestedThenFail(int id, String name) throws SQLException {
        doInsert(id, name);
        throw new RuntimeException("Nested intentional error");
    }

    /** SUPPORTS — 有事务就加入，无事务就以非事务执行 */
    @Transactional(propagation = Propagation.SUPPORTS)
    public int createUserSupports(int id, String name) throws SQLException {
        return doInsert(id, name);
    }

    /** NOT_SUPPORTED — 挂起已有事务，以非事务方式执行 */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public int createUserNotSupported(int id, String name) throws SQLException {
        return doInsert(id, name);
    }

    /** MANDATORY — 必须在已有事务环境下调用，否则异常 */
    @Transactional(propagation = Propagation.MANDATORY)
    public int createUserMandatory(int id, String name) throws SQLException {
        return doInsert(id, name);
    }

    /** NEVER — 必须在无事务环境下调用，有事务则异常 */
    @Transactional(propagation = Propagation.NEVER)
    public int createUserNever(int id, String name) throws SQLException {
        return doInsert(id, name);
    }

    // ==================== 特殊属性方法 ====================

    /** readOnly — 提交时自动回滚，数据不会持久化 */
    @Transactional(readOnly = true)
    public int createUserReadOnly(int id, String name) throws SQLException {
        return doInsert(id, name);
    }

    /** noRollbackFor — IllegalArgumentException 不触发回滚 */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void createUserNoRollbackForIAE(int id, String name) throws SQLException {
        doInsert(id, name);
        throw new IllegalArgumentException("Validation error — should NOT rollback");
    }

    /** noRollbackFor=IAE，但抛出 RuntimeException — 仍应回滚 */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void createUserRollbackForRTE(int id, String name) throws SQLException {
        doInsert(id, name);
        throw new RuntimeException("Real error — SHOULD rollback");
    }

    // ==================== 隔离级别方法 ====================

    /** SERIALIZABLE 隔离级别 */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int createUserSerializable(int id, String name) throws SQLException {
        return doInsert(id, name);
    }

    /** READ_COMMITTED 隔离级别 */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int createUserReadCommitted(int id, String name) throws SQLException {
        return doInsert(id, name);
    }

    // ==================== 无注解方法 ====================

    /** 无 @Transactional 注解 — 代理不应拦截，不参与事务管理 */
    public int createUserNoAnnotation(int id, String name) throws SQLException {
        return doInsert(id, name);
    }

    // ==================== 内部工具 ====================

    private int doInsert(int id, String name) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setCreateTime(new Date());
        return lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
    }
}
