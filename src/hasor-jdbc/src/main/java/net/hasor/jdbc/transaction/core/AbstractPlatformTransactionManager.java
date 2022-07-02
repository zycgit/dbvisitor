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
import static net.hasor.jdbc.transaction.TransactionBehavior.PROPAGATION_MANDATORY;
import static net.hasor.jdbc.transaction.TransactionBehavior.PROPAGATION_NESTED;
import static net.hasor.jdbc.transaction.TransactionBehavior.PROPAGATION_NEVER;
import static net.hasor.jdbc.transaction.TransactionBehavior.PROPAGATION_NOT_SUPPORTED;
import static net.hasor.jdbc.transaction.TransactionBehavior.PROPAGATION_REQUIRED;
import static net.hasor.jdbc.transaction.TransactionBehavior.RROPAGATION_REQUIRES_NEW;
import java.sql.SQLException;
import java.util.LinkedList;
import net.hasor.Hasor;
import net.hasor.jdbc.IllegalTransactionStateException;
import net.hasor.jdbc.TransactionDataAccessException;
import net.hasor.jdbc.TransactionSuspensionNotSupportedException;
import net.hasor.jdbc.transaction.TransactionBehavior;
import net.hasor.jdbc.transaction.TransactionLevel;
import net.hasor.jdbc.transaction.TransactionManager;
import net.hasor.jdbc.transaction.TransactionStatus;
import net.hasor.jdbc.transaction.TransactionSynchronizationManager;
/**
 * ĳһ������Դ�����������
 * @version : 2013-10-30
 * @author ������(zyc@hasor.net)
 */
public abstract class AbstractPlatformTransactionManager implements TransactionManager {
    private int                           defaultTimeout = -1;
    private LinkedList<TransactionStatus> tStatusStack   = new LinkedList<TransactionStatus>();
    /**��������*/
    public final TransactionStatus getTransaction(TransactionBehavior behavior) throws TransactionDataAccessException {
        Hasor.assertIsNotNull(behavior);
        return getTransaction(behavior, TransactionLevel.ISOLATION_DEFAULT);
    };
    public final TransactionStatus getTransaction(TransactionBehavior behavior, TransactionLevel level) throws TransactionDataAccessException {
        Hasor.assertIsNotNull(behavior);
        Hasor.assertIsNotNull(level);
        Object transaction = doGetTransaction();//��ȡĿǰ�������
        DefaultTransactionStatus defStatus = new DefaultTransactionStatus(behavior, transaction);
        /*-------------------------------------------------------------
        |                      �����Ѿ���������
        |
        | PROPAGATION_REQUIRED     �������������񣨲�����
        | RROPAGATION_REQUIRES_NEW ���������񣨹���ǰ���񣬿���������
        | PROPAGATION_NESTED       ��Ƕ���������ñ���㣩
        | PROPAGATION_SUPPORTS     �����滷����������
        | PROPAGATION_NOT_SUPPORTED��������ʽ��������ǰ����
        | PROPAGATION_NEVER        ���ų������쳣��
        | PROPAGATION_MANDATORY    ��ǿ��Ҫ�����񣨲�����
        ===============================================================*/
        if (this.isExistingTransaction(transaction) == true) {
            /*RROPAGATION_REQUIRES_NEW����������*/
            if (behavior == RROPAGATION_REQUIRES_NEW) {
                Object suspendHolder = this.suspend(transaction, defStatus);/*����ǰ����*/
                defStatus.setSuspendHolder(suspendHolder);
                this.processBegin(transaction, defStatus);/*����һ���µ�����*/
            }
            /*PROPAGATION_NESTED��Ƕ������*/
            if (behavior == PROPAGATION_NESTED) {
                defStatus.markHeldSavepoint();/*���ñ����*/
            }
            /*PROPAGATION_NOT_SUPPORTED��������ʽ*/
            if (behavior == PROPAGATION_NOT_SUPPORTED) {
                Object suspendHolder = this.suspend(transaction, defStatus);/*����ǰ����*/
                defStatus.setSuspendHolder(suspendHolder);
            }
            /*PROPAGATION_NEVER���ų�����*/
            if (behavior == PROPAGATION_NEVER)
                throw new IllegalTransactionStateException("Existing transaction found for transaction marked with propagation 'never'");
            return defStatus;
        }
        /*-------------------------------------------------------------
        |                      ����������������
        |
        | PROPAGATION_REQUIRED     �������������񣨿���������
        | RROPAGATION_REQUIRES_NEW ���������񣨿���������
        | PROPAGATION_NESTED       ��Ƕ�����񣨿���������
        | PROPAGATION_SUPPORTS     �����滷����������
        | PROPAGATION_NOT_SUPPORTED��������ʽ��������
        | PROPAGATION_NEVER        ���ų����񣨲�����
        | PROPAGATION_MANDATORY    ��ǿ��Ҫ�������쳣��
        ===============================================================*/
        /*PROPAGATION_REQUIRED��������������*/
        if (behavior == PROPAGATION_REQUIRED ||
        /*RROPAGATION_REQUIRES_NEW����������*/
        behavior == RROPAGATION_REQUIRES_NEW ||
        /*PROPAGATION_NESTED��Ƕ������*/
        behavior == PROPAGATION_NESTED) {
            this.processBegin(transaction, defStatus);/*��������*/
        }
        /*PROPAGATION_MANDATORY��ǿ��Ҫ������*/
        if (behavior == PROPAGATION_MANDATORY)
            throw new IllegalTransactionStateException("No existing transaction found for transaction marked with propagation 'mandatory'");
        return defStatus;
    }
    /**�ݽ�����*/
    public final void commit(TransactionStatus status) throws TransactionDataAccessException {
        Object transaction = doGetTransaction();//��ȡĿǰ�������
        DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
        /*����ϣ�����Ҫ����*/
        if (defStatus.isCompleted())
            throw new IllegalTransactionStateException("Transaction is already completed - do not call commit or rollback more than once per transaction");
        /*�ع����*/
        if (defStatus.isRollbackOnly()) {
            if (Hasor.isDebugLogger())
                Hasor.debug("Transactional code has requested rollback");
            rollBack(defStatus);
            return;
        }
        /*-------------------------------------------------------------
        | 1.���ۺ��ִ�����ʽ���ݽ�����������Ὣ isCompleted ������Ϊ true��
        | 2.�������״̬�а���һ��δ����ı���㡣���ݽ�����㣬���ǵݽ���������
        | 3.���� isNew ֻ��Ϊ true ʱ�����������ݽ����������
        ===============================================================*/
        try {
            prepareCommit(defStatus);
            /*�����������㣬�ڵݽ�����ʱֻ�������*/
            if (defStatus.hasSavepoint())
                defStatus.releaseHeldSavepoint();
            else if (defStatus.isNew())
                doCommit(transaction, defStatus);
            //
        } catch (SQLException ex) {
            rollBack(defStatus);/*�ݽ�ʧ�ܣ��ع�*/
            throw new TransactionDataAccessException("SQL Exception :", ex);
        } finally {
            cleanupAfterCompletion(defStatus);
        }
    }
    /**�ݽ�ǰ��Ԥ����*/
    private void prepareCommit(DefaultTransactionStatus defStatus) {
        // TODO Auto-generated method stub
    }
    /**�ع�����*/
    public final void rollBack(TransactionStatus status) throws TransactionDataAccessException {
        Object transaction = doGetTransaction();//��ȡĿǰ�������
        DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
        /*����ϣ�����Ҫ����*/
        if (defStatus.isCompleted())
            throw new IllegalTransactionStateException("Transaction is already completed - do not call commit or rollback more than once per transaction");
        /*-------------------------------------------------------------
        | 1.���ۺ��ִ�����ʽ���ݽ�����������Ὣ isCompleted ������Ϊ true��
        | 2.�������״̬�а���һ��δ����ı���㡣���ع�����㣬���ǻع���������
        | 3.���� isNew ֻ��Ϊ true ʱ�����������ع����������
        ===============================================================*/
        try {
            prepareRollback(defStatus);
            /*�����������㣬�ڵݽ�����ʱֻ�������*/
            if (defStatus.hasSavepoint())
                defStatus.rollbackToHeldSavepoint();
            else if (defStatus.isNew())
                doRollback(transaction, defStatus);
            //
        } catch (SQLException ex) {
            throw new TransactionDataAccessException("SQL Exception :", ex);
        } finally {
            cleanupAfterCompletion(defStatus);
        }
    }
    /**�ع�ǰ��Ԥ����*/
    private void prepareRollback(DefaultTransactionStatus defStatus) {
        // TODO Auto-generated method stub
    }
    //
    //
    //
    /**��������֮���������*/
    private void cleanupAfterCompletion(DefaultTransactionStatus defStatus) {
        defStatus.setCompleted();
        /*�������ͬ��������*/
        if (defStatus.isNew())
            TransactionSynchronizationManager.clear();
        /*�ָ����������*/
        if (defStatus.getSuspendedTransactionHolder() != null) {
            if (Hasor.isDebugLogger())
                Hasor.debug("Resuming suspended transaction after completion of inner transaction");
            resume(defStatus, (SuspendedTransactionHolder) defStatus.getSuspendedTransactionHolder());
        }
    }
    /**ʹ��һ���µ����ӿ���һ���µ�������Ϊ��ǰ������ȷ���ڵ��ø÷���ʱ��ǰ����������*/
    private void processBegin(Object transaction, DefaultTransactionStatus defStatus) {
        try {
            doBegin(transaction, defStatus);
            this.tStatusStack.push(defStatus);/*��ջ*/
        } catch (SQLException ex) {
            throw new TransactionDataAccessException("SQL Exception :", ex);
        }
    }
    /**�ָ���ǰ���������ƴ���ǰ����֮���ڻָ����������*/
    protected final void resume(DefaultTransactionStatus defStatus, SuspendedTransactionHolder transactionHolder) {
        try {
            SuspendedTransactionHolder suspendedHolder = (SuspendedTransactionHolder) transactionHolder;
            doResume(suspendedHolder.transaction, defStatus);
        } catch (SQLException ex) {
            throw new TransactionDataAccessException("SQL Exception :", ex);
        }
    }
    /**����ǰ��������һ�����������յ�ǰ����*/
    protected final SuspendedTransactionHolder suspend(Object transaction, DefaultTransactionStatus defStatus) {
        try {
            doSuspend(transaction, defStatus);
            SuspendedTransactionHolder suspendedHolder = new SuspendedTransactionHolder();
            suspendedHolder.transaction = transaction;
            //
            return suspendedHolder;
        } catch (SQLException ex) {
            throw new TransactionDataAccessException("SQL Exception :", ex);
        }
    }
    //
    //
    //
    //
    //
    //
    private static class SuspendedTransactionHolder {
        private Object transaction;
    }
    //
    //
    //
    /**��ȡ��ǰ����������д��ڵ��������*/
    protected abstract Object doGetTransaction();
    /**�жϵ�ǰ��������Ƿ��Ѿ����������С��÷����������������񴫲����ԵĴ���ʽ��*/
    protected abstract boolean isExistingTransaction(Object transaction);
    /**����һ��ȫ�µ�����*/
    protected abstract void doBegin(Object transaction, DefaultTransactionStatus defStatus) throws SQLException;
    /**�ݽ�����*/
    protected abstract void doCommit(Object transaction, DefaultTransactionStatus defStatus) throws SQLException;
    /**�ع�����*/
    protected abstract void doRollback(Object transaction, DefaultTransactionStatus defStatus) throws SQLException;
    /**�ָ����񣨽��ڶ���������ʾ���������ָ�����ǰ����*/
    protected void doResume(Object resumeTransaction, DefaultTransactionStatus defStatus) throws SQLException {
        throw new TransactionSuspensionNotSupportedException("Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
    }
    /**�������񣨱��浱ǰ���񣬲���յ�ǰ����*/
    protected void doSuspend(Object transaction, DefaultTransactionStatus defStatus) throws SQLException {
        throw new TransactionSuspensionNotSupportedException("Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
    }
}