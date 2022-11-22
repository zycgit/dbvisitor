/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.transaction.support;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.transaction.*;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

import static net.hasor.dbvisitor.transaction.Propagation.*;

/**
 * 某一个数据源的事务管理器
 *
 * <p><b><i>事务栈：</i></b>
 * <p>事务管理器允许使用不同的传播属性反复开启新的事务。所有被开启的事务在正确处置（commit,rollback）
 * 它们之前都会按照先后顺序依次压入事务管理器的“事务栈”中。一旦有事务被处理（commit,rollback）这个事务才会被从事务栈中弹出。
 * <p>倘若被弹出的事务(A)并不是栈顶的事务，那么在事务(A)被处理（commit,rollback）时会优先处理自事务(A)以后开启的其它事务。
 *
 * @version : 2013-10-30
 * @author 赵永春 (zyc@hasor.net)
 */
public class LocalTransactionManager implements TransactionManager, Closeable {
    private static final Logger                        logger       = LoggerFactory.getLogger(LocalTransactionManager.class);
    private final        Deque<LocalTransactionStatus> tStatusStack = new LinkedBlockingDeque<>();
    private final        DataSource                    dataSource;

    public LocalTransactionManager(final DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        this.dataSource = dataSource;
    }

    /** 获取当前事务管理器管理的数据源对象 */
    public DataSource getDataSource() {
        return this.dataSource;
    }

    /** 是否存在未处理完的事务（包括被挂起的事务） */
    @Override
    public boolean hasTransaction() {
        return !this.tStatusStack.isEmpty();
    }

    /** 测试事务状态是否位于栈顶 */
    @Override
    public boolean isTopTransaction(final TransactionStatus status) {
        if (this.tStatusStack.isEmpty() || status == null) {
            return false;
        }
        return this.tStatusStack.peek() == status;
    }

    @Override
    public void commit() throws SQLException {
        LocalTransactionStatus last = this.tStatusStack.peek();
        if (last != null) {
            commit(last);
        }
    }

    @Override
    public void rollBack() throws SQLException {
        LocalTransactionStatus last = this.tStatusStack.peek();
        if (last != null) {
            rollBack(last);
        }
    }

    /** 开启事务 */
    @Override
    public final TransactionStatus begin(final Propagation behavior, final Isolation level) throws SQLException {
        Objects.requireNonNull(behavior);
        //1.获取连接
        LocalTransactionStatus defStatus = new LocalTransactionStatus(behavior, level);
        defStatus.setTranConn(this.doGetConnection(defStatus));
        this.tStatusStack.addFirst(defStatus);/*入栈*/
        /*-------------------------------------------------------------
        |                      环境已经存在事务
        |
        | REQUIRED     ：加入已有事务（不处理）
        | REQUIRES_NEW ：独立事务（挂起当前事务，开启新事务）
        | NESTED       ：嵌套事务（设置保存点）
        | SUPPORTS     ：跟随环境（不处理）
        | NOT_SUPPORTED：非事务方式（仅挂起当前事务）
        | NEVER        ：排除事务（异常）
        | MANDATORY    ：强制要求事务（不处理）
        ===============================================================*/
        if (this.isExistingTransaction(defStatus)) {
            /*REQUIRES_NEW：独立事务*/
            if (behavior == REQUIRES_NEW) {
                this.suspend(defStatus);/*挂起当前事务*/
                this.doBegin(defStatus);/*开启新事务*/
            }
            /*NESTED：嵌套事务*/
            if (behavior == NESTED) {
                defStatus.markSavepoint();/*设置保存点*/
            }
            /*NOT_SUPPORTED：非事务方式*/
            if (behavior == NOT_SUPPORTED) {
                this.suspend(defStatus);/*挂起事务*/
            }
            /*NEVER：排除事务*/
            if (behavior == NEVER) {
                this.cleanupAfterCompletion(defStatus);
                throw new SQLException("existing transaction found for transaction marked with propagation 'never'");
            }
            return defStatus;
        }
        /*-------------------------------------------------------------
        |                      环境不经存在事务
        |
        | REQUIRED     ：加入已有事务（开启新事务）
        | REQUIRES_NEW ：独立事务（开启新事务）
        | NESTED       ：嵌套事务（开启新事务）
        | SUPPORTS     ：跟随环境（不处理）
        | NOT_SUPPORTED：非事务方式（不处理）
        | NEVER        ：排除事务（不处理）
        | MANDATORY    ：强制要求事务（异常）
        ===============================================================*/
        /*REQUIRED：加入已有事务*/
        if (behavior == REQUIRED ||
                /*REQUIRES_NEW：独立事务*/
                behavior == REQUIRES_NEW ||
                /*NESTED：嵌套事务*/
                behavior == NESTED) {
            this.doBegin(defStatus);/*开启新事务*/
        }
        /*MANDATORY：强制要求事务*/
        if (behavior == MANDATORY) {
            this.cleanupAfterCompletion(defStatus);
            throw new SQLException("no existing transaction found for transaction marked with propagation 'mandatory'");
        }
        return defStatus;
    }

    /** 判断连接对象是否处于事务中，该方法会用于评估事务传播属性的处理方式 */
    private boolean isExistingTransaction(final LocalTransactionStatus defStatus) throws SQLException {
        return defStatus.getTranConn().hasTransaction();
    }

    /** 初始化一个新的连接，并开启事务 */
    protected void doBegin(final LocalTransactionStatus defStatus) throws SQLException {
        TransactionObject tranConn = defStatus.getTranConn();
        tranConn.beginTransaction();
    }

    /** 递交事务 */
    @Override
    public final void commit(final TransactionStatus status) throws SQLException {
        LocalTransactionStatus defStatus = (LocalTransactionStatus) status;
        /*已完毕，不需要处理*/
        if (defStatus.isCompleted()) {
            throw new SQLException("Transaction is already completed - do not call commit or rollback more than once per transaction");
        }
        /*回滚情况*/
        if (defStatus.isReadOnly() || defStatus.isRollbackOnly()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Transactional code has requested rollback");
            }
            this.rollBack(defStatus);
            return;
        }
        /*-------------------------------------------------------------
        | 1.无论何种传播形式，递交事务操作都会将 isCompleted 属性置为 true。
        | 2.如果事务状态中包含一个未处理的保存点。仅递交保存点，而非递交整个事务。
        | 3.事务 isNew 只有为 true 时才真正触发递交事务操作。
        ===============================================================*/
        try {
            this.prepareCommit(defStatus);
            /*如果包含保存点，在递交事务时只处理保存点*/
            if (defStatus.hasSavepoint()) {
                defStatus.releaseSavepoint();
            } else if (defStatus.isNewConnection()) {
                this.doCommit(defStatus);
            }

        } catch (SQLException ex) {
            this.doRollback(defStatus);/*递交失败，回滚*/
            throw ex;
        } finally {
            this.cleanupAfterCompletion(defStatus);
        }
    }

    /** 递交前的预处理 */
    private void prepareCommit(final LocalTransactionStatus defStatus) throws SQLException {
        /*首先预处理的事务必须存在于管理器的事务栈内某一位置中，否则要处理的事务并非来源于该事务管理器。*/
        if (!this.tStatusStack.contains(defStatus)) {
            throw new SQLException("This transaction is not derived from this Manager.");
        }
        /*-------------------------------------------------------------
        | 如果预处理的事务并非位于栈顶，则进行弹栈操作。
        |--------------------------\
        | T5  ^   <-- pop-up       | 假定预处理的事务为 T4，那么：
        | T4  ^   <-- pop-up       | T5 事务会被先递交，然后是 T4
        | T3  .   <-- defStatus    | 接下来就完成了预处理。
        | T2                       |
        | T1                       |
        |--------------------------/

        ===============================================================*/
        //
        TransactionStatus inStackStatus = null;
        while ((inStackStatus = this.tStatusStack.peek()) != defStatus) {
            this.commit(inStackStatus);
        }
    }

    /** 处理当前底层数据库连接的事务递交操作 */
    protected void doCommit(final LocalTransactionStatus defStatus) throws SQLException {
        TransactionObject tranObject = defStatus.getTranConn();
        tranObject.commit();
    }

    /** 回滚事务 */
    @Override
    public final void rollBack(final TransactionStatus status) throws SQLException {
        LocalTransactionStatus defStatus = (LocalTransactionStatus) status;
        /*已完毕，不需要处理*/
        if (defStatus.isCompleted()) {
            throw new SQLException("Transaction is already completed - do not call commit or rollback more than once per transaction");
        }
        /*-------------------------------------------------------------
        | 1.无论何种传播形式，递交事务操作都会将 isCompleted 属性置为 true。
        | 2.如果事务状态中包含一个未处理的保存点。仅回滚保存点，而非回滚整个事务。
        | 3.事务 isNew 只有为 true 时才真正触发回滚事务操作。
        ===============================================================*/
        try {
            this.prepareRollback(defStatus);
            /*如果包含保存点，在递交事务时只处理保存点*/
            if (defStatus.hasSavepoint()) {
                defStatus.rollbackToSavepoint();
            } else if (defStatus.isNewConnection()) {
                this.doRollback(defStatus);
            }

        } catch (SQLException ex) {
            this.doRollback(defStatus);
            throw ex;
        } finally {
            this.cleanupAfterCompletion(defStatus);
        }
    }

    /** 回滚前的预处理 */
    private void prepareRollback(final LocalTransactionStatus defStatus) throws SQLException {
        /*首先预处理的事务必须存在于管理器的事务栈内某一位置中，否则要处理的事务并非来源于该事务管理器。*/
        if (!this.tStatusStack.contains(defStatus)) {
            throw new SQLException("This transaction is not derived from this Manager.");
        }
        /*-------------------------------------------------------------
        | 如果预处理的事务并非位于栈顶，则进行弹栈操作。
        |--------------------------\
        | T5  ^   <-- pop-up       | 假定预处理的事务为 T4，那么：
        | T4  ^   <-- pop-up       | T5 事务会被先回滚，然后是 T4
        | T3  .   <-- defStatus    | 接下来就完成了预处理。
        | T2                       |
        | T1                       |
        |--------------------------/
        |
        ===============================================================*/

        TransactionStatus inStackStatus = null;
        while ((inStackStatus = this.tStatusStack.peek()) != defStatus) {
            this.rollBack(inStackStatus);
        }
    }

    /** 处理当前底层数据库连接的事务回滚操作 */
    protected void doRollback(final LocalTransactionStatus defStatus) throws SQLException {
        TransactionObject tranObject = defStatus.getTranConn();
        tranObject.rollback();
    }

    /** 挂起事务 */
    protected final void suspend(final LocalTransactionStatus defStatus) throws SQLException {
        /*事务已经被挂起*/
        if (defStatus.isSuspend()) {
            throw new SQLException("the Transaction has Suspend.");
        }

        /*是否为栈顶事务*/
        this.prepareCheckStack(defStatus);
        /*挂起事务*/
        TransactionObject tranConn = defStatus.getTranConn();
        defStatus.setSuspendConn(tranConn);/*挂起*/
        SyncManager.clearSync(this.getDataSource());/*清除线程上的同步事务*/
        defStatus.setTranConn(this.doGetConnection(defStatus));/*重新申请数据库连接*/
    }

    /** 恢复被挂起的事务 */
    protected final void resume(final LocalTransactionStatus defStatus) throws SQLException {
        if (!defStatus.isCompleted()) {
            throw new SQLException("the Transaction has not completed.");
        }
        if (!defStatus.isSuspend()) {
            throw new SQLException("the Transaction has not Suspend.");
        }

        /*检查事务是否为栈顶事务*/
        this.prepareCheckStack(defStatus);

        /*恢复挂起的事务*/
        if (defStatus.isSuspend()) {
            TransactionObject tranConn = defStatus.getSuspendConn();/*取得挂起的数据库连接*/
            SyncManager.setSync(tranConn);/*设置线程的数据库连接*/
            defStatus.setTranConn(tranConn);
            defStatus.setSuspendConn(null);
            tranConn.getHolder().released();
        }
    }

    /** 检查正在处理的事务状态是否位于栈顶，否则抛出异常 */
    private void prepareCheckStack(final LocalTransactionStatus defStatus) throws SQLException {
        if (!this.isTopTransaction(defStatus)) {
            throw new SQLException("the Transaction Status is not top in stack.");
        }
    }

    /** commit,rollback。之后的清理工作，同时也负责恢复事务和操作事务堆栈 */
    private void cleanupAfterCompletion(final LocalTransactionStatus defStatus) throws SQLException {
        /*清理的事务必须是位于栈顶*/
        this.prepareCheckStack(defStatus);

        /*标记完成*/
        defStatus.setCompleted();
        /*恢复当时的隔离级别*/
        TransactionObject tranObj = defStatus.getTranConn();
        Isolation transactionIsolation = tranObj.getRecoverIsolation();
        if (transactionIsolation != null) {
            tranObj.getHolder().getConnection().setTransactionIsolation(transactionIsolation.getValue());
        }
        tranObj.stopTransaction();
        tranObj.getHolder().released();//ref--
        /*恢复挂起的事务*/
        if (defStatus.isSuspend()) {
            this.resume(defStatus);
        }
        /*清理defStatus*/
        this.tStatusStack.removeFirst();

        defStatus.setTranConn(null);
        defStatus.setSuspendConn(null);
    }

    /** 获取数据库连接（线程绑定的）*/
    protected TransactionObject doGetConnection(final LocalTransactionStatus defStatus) throws SQLException {
        ConnectionHolder holder = SyncManager.getHolder(this.dataSource);
        if (!holder.isOpen() || !holder.hasTransaction()) {
            defStatus.markNewConnection();/*新事物，新连接*/
        }
        holder.requested();//ref++
        Connection conn = holder.getConnection();

        // 当设置了隔离级别则设置 recoverIsolation
        if (defStatus.getIsolationLevel() == null) {
            return new TransactionObject(holder, null, this.getDataSource());
        } else {
            Isolation recoverIsolation = Isolation.valueOf(conn.getTransactionIsolation());
            if (defStatus.getIsolationLevel() != recoverIsolation) {
                conn.setTransactionIsolation(defStatus.getIsolationLevel().getValue());
            }
            return new TransactionObject(holder, recoverIsolation, this.getDataSource());
        }
    }

    /** 获取最后一个事务 {@link LocalTransactionStatus} */
    public LocalTransactionStatus lastTransaction() {
        return this.tStatusStack.peek();
    }

    @Override
    public void close() throws IOException {
        if (this.tStatusStack.isEmpty()) {
            return;
        }

        try {
            this.commit(this.tStatusStack.getLast());
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
