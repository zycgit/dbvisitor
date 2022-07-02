/*
 * Copyright 2008-2009 the original ������(zyc@hasor.net).
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
package net.hasor.jdbc.transaction.core;
import java.sql.Savepoint;
import net.hasor.jdbc.IllegalTransactionStateException;
import net.hasor.jdbc.transaction.TransactionBehavior;
import net.hasor.jdbc.transaction.TransactionLevel;
import net.hasor.jdbc.transaction.TransactionStatus;
/**
 * ��ʾһ�����ڹ��������״̬��
 * @version : 2013-10-30
 * @author ������(zyc@hasor.net)
 */
public abstract class AbstractTransactionStatus implements TransactionStatus {
    private Savepoint           savepoint;
    private Object              suspendHolder;
    private TransactionBehavior behavior;
    private TransactionLevel    level;
    private boolean             completed    = false;
    private boolean             rollbackOnly = false;
    //
    public AbstractTransactionStatus(TransactionBehavior behavior, TransactionLevel level, Object transaction) {
        this.behavior = behavior;
        this.level = level;
    }
    /**�趨һ�����ݿ����񱣴�㡣*/
    public void markHeldSavepoint() {
        if (this.hasSavepoint())
            throw new IllegalTransactionStateException("TransactionStatus has Savepoint");
        this.savepoint = this.getSavepointManager().createSavepoint();
    }
    /***/
    public void releaseHeldSavepoint() {
        if (this.hasSavepoint() == false)
            throw new IllegalTransactionStateException("TransactionStatus has not Savepoint");
        this.getSavepointManager().releaseSavepoint(this.savepoint);
    }
    public void rollbackToHeldSavepoint() {
        if (this.hasSavepoint() == false)
            throw new IllegalTransactionStateException("TransactionStatus has not Savepoint");
        this.getSavepointManager().rollbackToSavepoint(this.savepoint);
    }
    public void setSuspendHolder(Object suspendHolder) {
        this.suspendHolder = suspendHolder;
    }
    public Object getSuspendedTransactionHolder() {
        return this.suspendHolder;
    }
    public void setCompleted() {
        this.completed = true;
    }
    public TransactionBehavior getTransactionBehavior() {
        return this.behavior;
    }
    public TransactionLevel getIsolationLevel() {
        return this.level;
    }
    public boolean isCompleted() {
        return this.completed;
    }
    public boolean hasSavepoint() {
        return this.savepoint != null;
    }
    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }
    public boolean isRollbackOnly() {
        return this.rollbackOnly;
    }
    //
    protected abstract SavepointManager getSavepointManager();
    public abstract boolean isNewConnection();
}