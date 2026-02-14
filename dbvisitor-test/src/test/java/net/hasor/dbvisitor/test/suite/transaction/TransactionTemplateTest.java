package net.hasor.dbvisitor.test.suite.transaction;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.transaction.*;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * TransactionTemplate 模板式事务测试
 * <p>覆盖：
 * <ul>
 *   <li>基本提交/回滚</li>
 *   <li>显式 setRollback / setReadOnly</li>
 *   <li>CallbackWithoutResult 无返回值模式</li>
 *   <li>模板嵌套 + 不同传播行为组合</li>
 *   <li>模板指定隔离级别</li>
 * </ul>
 * <p>数据操作使用 LambdaTemplate。
 */
public class TransactionTemplateTest extends AbstractOneApiTest {

    private TransactionManager getTxManager() {
        return TransactionHelper.txManager(dataSource);
    }

    private TransactionTemplate getTxTemplate() {
        return new TransactionTemplateManager(getTxManager());
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

    // ==================== 基本提交/回滚 ====================

    /**
     * 模板回调正常返回 → 事务自动提交
     */
    @Test
    public void testTemplate_NormalCommit() throws Throwable {
        TransactionTemplate template = getTxTemplate();

        int result = template.execute(new TransactionCallback<Integer>() {
            @Override
            public Integer doTransaction(TransactionStatus status) throws Throwable {
                insertUser(61001, "TplCommit");
                return 1;
            }
        });

        assertEquals(1, result);
        assertEquals(1, countById(61001));
    }

    /**
     * 模板回调抛出异常 → 事务自动回滚
     */
    @Test
    public void testTemplate_ExceptionRollback() throws Throwable {
        TransactionTemplate template = getTxTemplate();

        try {
            template.execute(new TransactionCallback<Object>() {
                @Override
                public Object doTransaction(TransactionStatus status) throws Throwable {
                    insertUser(61002, "TplRollback");
                    throw new RuntimeException("Intentional error");
                }
            });
            fail("应抛出异常");
        } catch (RuntimeException e) {
            // 期望
        }

        assertEquals(0, countById(61002));
    }

    /**
     * 回调中显式 setRollback → 事务标记为回滚，正常返回后仍执行回滚
     */
    @Test
    public void testTemplate_SetRollbackExplicitly() throws Throwable {
        TransactionTemplate template = getTxTemplate();

        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doTransaction(TransactionStatus status) throws Throwable {
                insertUser(61003, "TplSetRollback");
                status.setRollback(); // 显式标记回滚
                return null;
            }
        });

        assertEquals(0, countById(61003));
    }

    /**
     * 回调中 setReadOnly → 提交时自动转为回滚
     */
    @Test
    public void testTemplate_SetReadOnly() throws Throwable {
        TransactionTemplate template = getTxTemplate();

        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doTransaction(TransactionStatus status) throws Throwable {
                insertUser(61004, "TplReadOnly");
                status.setReadOnly(); // 标记只读
                return null;
            }
        });

        assertEquals(0, countById(61004));
    }

    /**
     * CallbackWithoutResult 无返回值模式
     */
    @Test
    public void testTemplate_CallbackWithoutResult() throws Throwable {
        TransactionTemplate template = getTxTemplate();

        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doTransactionWithoutResult(TransactionStatus status) throws Throwable {
                insertUser(61005, "TplVoid");
            }
        });

        assertEquals(1, countById(61005));
    }

    // ==================== 模板嵌套 + 传播行为 ====================

    /**
     * 外层 REQUIRED 模板 + 内层 REQUIRES_NEW 模板：内层提交后外层回滚 → 内层数据保留
     */
    @Test
    public void testTemplate_Nested_RequiresNew() throws Throwable {
        TransactionTemplate template = getTxTemplate();
        final TransactionManager tm = getTxManager();

        // 用编程式开启外层事务，然后在模板中使用 REQUIRES_NEW
        TransactionStatus outer = tm.begin(Propagation.REQUIRED);

        insertUser(61010, "TplOuterRN");

        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doTransaction(TransactionStatus status) throws Throwable {
                insertUser(61011, "TplInnerRN");
                return null;
            }
        }, Propagation.REQUIRES_NEW); // 独立事务

        tm.rollBack(outer);

        assertEquals(0, countById(61010)); // 外层回滚
        assertEquals(1, countById(61011)); // 内层独立提交
    }

    /**
     * 外层 REQUIRED 模板 + 内层 NESTED 模板（Savepoint）：内层异常回滚到 Savepoint，外层正常提交
     */
    @Test
    public void testTemplate_Nested_Savepoint() throws Throwable {
        TransactionTemplate template = getTxTemplate();
        final TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        insertUser(61012, "TplOuterNested");

        try {
            template.execute(new TransactionCallback<Object>() {
                @Override
                public Object doTransaction(TransactionStatus status) throws Throwable {
                    insertUser(61013, "TplInnerNested");
                    throw new RuntimeException("Nested error");
                }
            }, Propagation.NESTED);
        } catch (RuntimeException e) {
            // 嵌套事务异常回滚到 Savepoint
        }

        tm.commit(outer);

        assertEquals(1, countById(61012)); // 外层数据保留
        assertEquals(0, countById(61013)); // 嵌套数据回滚
    }

    /**
     * 模板指定隔离级别 READ_COMMITTED：正常提交
     */
    @Test
    public void testTemplate_WithIsolation() throws Throwable {
        TransactionTemplate template = getTxTemplate();

        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doTransaction(TransactionStatus status) throws Throwable {
                insertUser(61020, "TplIsolation");
                // 验证隔离级别已设置
                assertEquals(Isolation.READ_COMMITTED, status.getIsolationLevel());
                return null;
            }
        }, Propagation.REQUIRED, Isolation.READ_COMMITTED);

        assertEquals(1, countById(61020));
    }
}
