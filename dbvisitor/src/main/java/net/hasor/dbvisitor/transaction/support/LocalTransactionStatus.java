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
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionStatus;

import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * 表示一个用于管理事务的状态点
 * @version : 2013-10-30
 * @author 赵永春 (zyc@hasor.net)
 */
public class LocalTransactionStatus implements TransactionStatus {
    private       Savepoint         savepoint     = null;  //事务保存点
    private       TransactionObject tranConn      = null;  //当前事务使用的数据库连接
    private       TransactionObject suspendConn   = null;  //当前事务之前挂起的上一个数据库事务
    private final Propagation       propagation;  //传播属性
    private final Isolation         level;  //隔离级别
    private       boolean           completed     = false; //完成（true表示完成）
    private       boolean           rollbackOnly  = false; //要求回滚（true表示回滚）
    private       boolean           newConnection = false; //是否使用了一个全新的数据库连接开启事务（true表示新连接）
    private       boolean           readOnly      = false; //只读模式（true表示只读）

    public LocalTransactionStatus(final Propagation propagation, final Isolation level) {
        this.propagation = propagation;
        this.level = level;
    }

    protected SavepointManager getSavepointManager() {
        return (SavepointManager) this.tranConn.getHolder();
    }

    public void markSavepoint() throws SQLException {
        if (this.hasSavepoint()) {
            throw new SQLException("TransactionStatus has Savepoint");
        }

        SavepointManager manager = getSavepointManager();
        if (!manager.supportSavepoint()) {
            throw new SQLException("Connection does not support Savepoint.");
        }

        this.savepoint = manager.createSavepoint();
    }

    public void releaseSavepoint() throws SQLException {
        if (!this.hasSavepoint()) {
            throw new SQLException("TransactionStatus has not Savepoint");
        }

        SavepointManager manager = getSavepointManager();
        if (!manager.supportSavepoint()) {
            throw new SQLException("Connection does not support Savepoint.");
        }

        manager.releaseSavepoint(this.savepoint);
        this.savepoint = null;
    }

    public void rollbackToSavepoint() throws SQLException {
        if (!this.hasSavepoint()) {
            throw new SQLException("TransactionStatus has not Savepoint");
        }

        SavepointManager manager = getSavepointManager();
        if (!manager.supportSavepoint()) {
            throw new SQLException("Connection does not support Savepoint.");
        }

        manager.rollback(this.savepoint);
    }

    /* 设置完成状态 */
    void setCompleted() {
        this.completed = true;
    }

    /* 标记使用的是全新连接 */
    void markNewConnection() {
        this.newConnection = true;
    }

    TransactionObject getTranConn() {
        return this.tranConn;
    }

    void setTranConn(final TransactionObject tranConn) {
        this.tranConn = tranConn;
    }

    TransactionObject getSuspendConn() {
        return this.suspendConn;
    }

    void setSuspendConn(final TransactionObject suspendConn) {
        this.suspendConn = suspendConn;
    }

    @Override
    public Propagation getPropagation() {
        return this.propagation;
    }

    @Override
    public Isolation getIsolationLevel() {
        return this.level;
    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }

    @Override
    public boolean isRollbackOnly() {
        return this.rollbackOnly;
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public boolean isNewConnection() {
        return this.newConnection;
    }

    @Override
    public boolean isSuspend() {
        return this.suspendConn != null;
    }

    @Override
    public boolean hasSavepoint() {
        return this.savepoint != null;
    }

    @Override
    public void setRollback() throws SQLException {
        if (this.isCompleted()) {
            throw new SQLException("Transaction is already completed.");
        }
        this.rollbackOnly = true;
    }

    @Override
    public void setReadOnly() throws SQLException {
        if (this.isCompleted()) {
            throw new SQLException("Transaction is already completed.");
        }
        this.readOnly = true;
    }
}