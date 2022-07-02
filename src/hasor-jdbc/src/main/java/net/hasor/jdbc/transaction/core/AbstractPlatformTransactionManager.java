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
/**
 * ĳһ������Դ�����������
 * 
 * <p><b><i>����ջ��</i></b>
 * <p>�������������ʹ�ò�ͬ�Ĵ������Է��������µ��������б���������������ȷ���ã�commit,rollback��
 * ����֮ǰ���ᰴ���Ⱥ�˳������ѹ������������ġ�����ջ���С�һ�������񱻴���commit,rollback���������Żᱻ������ջ�е�����
 * <p>����������������(A)������ջ����������ô������(A)������commit,rollback��ʱ�����ȴ���������(A)�Ժ�������������
 * 
 * @version : 2013-10-30
 * @author ������(zyc@hasor.net)
 */
public abstract class AbstractPlatformTransactionManager implements TransactionManager {
    private int                           defaultTimeout = -1;
    private LinkedList<TransactionStatus> tStatusStack   = new LinkedList<TransactionStatus>();
    //
    public boolean hasTransaction() {
        return !tStatusStack.isEmpty();
    }
    public boolean isTopTransaction(TransactionStatus status) {
        if (tStatusStack.isEmpty())
            return false;
        return this.tStatusStack.peek() == status;
    }
    //
    //
    //
    /**��������*/
    public final TransactionStatus getTransaction(TransactionBehavior behavior) throws TransactionDataAccessException {
        Hasor.assertIsNotNull(behavior);
        return getTransaction(behavior, TransactionLevel.ISOLATION_DEFAULT);
    };
    public final TransactionStatus getTransaction(TransactionBehavior behavior, TransactionLevel level) throws TransactionDataAccessException {
        Hasor.assertIsNotNull(behavior);
        Hasor.assertIsNotNull(level);
        Object transaction = doGetTransaction();//��ȡĿǰ�������
        AbstractTransactionStatus defStatus = new AbstractTransactionStatus(behavior, level, transaction);
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
                this.suspend(transaction, defStatus);/*����ǰ����*/
                this.processBegin(transaction, defStatus);/*����һ���µ�����*/
            }
            /*PROPAGATION_NESTED��Ƕ������*/
            if (behavior == PROPAGATION_NESTED) {
                defStatus.markHeldSavepoint();/*���ñ����*/
            }
            /*PROPAGATION_NOT_SUPPORTED��������ʽ*/
            if (behavior == PROPAGATION_NOT_SUPPORTED) {
                this.suspend(transaction, defStatus);/*����ǰ����*/
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
    /**ʹ��һ���µ����ӿ���һ���µ�������Ϊ��ǰ������ȷ���ڵ��ø÷���ʱ��ǰ����������*/
    private void processBegin(Object transaction, AbstractTransactionStatus defStatus) {
        try {
            doBegin(transaction, defStatus);
            this.tStatusStack.push(defStatus);/*��ջ*/
        } catch (SQLException ex) {
            throw new TransactionDataAccessException("SQL Exception :", ex);
        }
    }
    /**�жϵ�ǰ��������Ƿ��Ѿ����������С��÷����������������񴫲����ԵĴ���ʽ��*/
    protected abstract boolean isExistingTransaction(Object transaction);
    /**�ڵ�ǰ�����Ͽ���һ��ȫ�µ�����*/
    protected abstract void doBegin(Object transaction, AbstractTransactionStatus defStatus) throws SQLException;
    //
    //
    //
    /**�ݽ�����*/
    public final void commit(TransactionStatus status) throws TransactionDataAccessException {
        Object transaction = doGetTransaction();//��ȡ�ײ�ά���ĵ�ǰ�������
        AbstractTransactionStatus defStatus = (AbstractTransactionStatus) status;
        /*����ϣ�����Ҫ����*/
        if (defStatus.isCompleted())
            throw new IllegalTransactionStateException("Transaction is already completed - do not call commit or rollback more than once per transaction");
        /*�ع����*/
        if (defStatus.isRollbackOnly()) {
            if (Hasor.isDebugLogger())
                Hasor.logDebug("Transactional code has requested rollback");
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
            else if (defStatus.isNewConnection())
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
    private void prepareCommit(AbstractTransactionStatus defStatus) {
        /*����Ԥ����������������ڹ�����������ջ��ĳһλ���У�����Ҫ��������񲢷���Դ�ڸ������������*/
        if (this.tStatusStack.contains(defStatus) == false)
            throw new IllegalTransactionStateException("This transaction is not derived from this Manager.");
        /*-------------------------------------------------------------
        | ���Ԥ��������񲢷�λ��ջ��������е�ջ������
        |--------------------------\
        | T5  ^   <-- pop-up       | �ٶ�Ԥ���������Ϊ T4����ô��
        | T4  ^   <-- pop-up       | T5 ����ᱻ�ȵݽ���Ȼ���� T4
        | T3  .   <-- defStatus    | �������������Ԥ����
        | T2                       |
        | T1                       |
        |--------------------------/
        |
        ===============================================================*/
        //
        TransactionStatus inStackStatus = null;
        while ((inStackStatus = this.tStatusStack.peek()) != defStatus)
            this.commit(inStackStatus);
    }
    /**����ǰ�ײ����ݿ����ӵ�����ݽ�������*/
    protected abstract void doCommit(Object transaction, AbstractTransactionStatus defStatus) throws SQLException;
    //
    //
    //
    /**�ع�����*/
    public final void rollBack(TransactionStatus status) throws TransactionDataAccessException {
        Object transaction = doGetTransaction();//��ȡĿǰ�������
        AbstractTransactionStatus defStatus = (AbstractTransactionStatus) status;
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
            else if (defStatus.isNewConnection())
                doRollback(transaction, defStatus);
            //
        } catch (SQLException ex) {
            throw new TransactionDataAccessException("SQL Exception :", ex);
        } finally {
            cleanupAfterCompletion(defStatus);
        }
    }
    /**�ع�ǰ��Ԥ����*/
    private void prepareRollback(AbstractTransactionStatus defStatus) {
        /*����Ԥ����������������ڹ�����������ջ��ĳһλ���У�����Ҫ��������񲢷���Դ�ڸ������������*/
        if (this.tStatusStack.contains(defStatus) == false)
            throw new IllegalTransactionStateException("This transaction is not derived from this Manager.");
        /*-------------------------------------------------------------
        | ���Ԥ��������񲢷�λ��ջ��������е�ջ������
        |--------------------------\
        | T5  ^   <-- pop-up       | �ٶ�Ԥ���������Ϊ T4����ô��
        | T4  ^   <-- pop-up       | T5 ����ᱻ�Ȼع���Ȼ���� T4
        | T3  .   <-- defStatus    | �������������Ԥ����
        | T2                       |
        | T1                       |
        |--------------------------/
        |
        ===============================================================*/
        //
        TransactionStatus inStackStatus = null;
        while ((inStackStatus = this.tStatusStack.peek()) != defStatus)
            this.rollBack(inStackStatus);
    }
    /**����ǰ�ײ����ݿ����ӵ�����ع�������*/
    protected abstract void doRollback(Object transaction, AbstractTransactionStatus defStatus) throws SQLException;
    //
    //
    //
    private static class SuspendedTransactionHolder {
        public Object transaction = null; /*����ĵײ��������*/
    }
    /**����ǰ����*/
    protected final void suspend(Object transaction, AbstractTransactionStatus defStatus) {
        try {
            /*��������Ƿ�Ϊջ������*/
            prepareCheckStack(defStatus);
            /*���� SuspendedTransactionHolder �������ڱ��浱ǰ�ײ����ݿ������Լ��������*/
            doSuspend(transaction, defStatus);
            SuspendedTransactionHolder suspendedHolder = new SuspendedTransactionHolder();
            suspendedHolder.transaction = transaction;/*�����������������ڵײ㣩*/
            defStatus.setSuspendHolder(suspendedHolder);
            //
        } catch (SQLException ex) {
            throw new TransactionDataAccessException("SQL Exception :", ex);
        }
    }
    /**��������������Ҫ��д�÷�������transaction���񣬲�ͬʱ��յײ㵱ǰ���ݿ����ӣ�*/
    protected void doSuspend(Object transaction, AbstractTransactionStatus defStatus) throws SQLException {
        throw new TransactionSuspensionNotSupportedException("Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
    }
    /**�ָ�����������񣬻ָ����������ʱ�����ǵ�ǰ���������ƴ���ǰ����֮���ڻָ����������*/
    protected final void resume(Object transaction, AbstractTransactionStatus defStatus) {
        if (defStatus.isCompleted() == false)
            throw new IllegalTransactionStateException("the Transaction has not completed.");
        try {
            /*��������Ƿ�Ϊջ������*/
            prepareCheckStack(defStatus);
            SuspendedTransactionHolder suspendedHolder = (SuspendedTransactionHolder) defStatus.getSuspendedTransactionHolder();
            doResume(suspendedHolder.transaction, defStatus);
        } catch (SQLException ex) {
            throw new TransactionDataAccessException("SQL Exception :", ex);
        }
    }
    /**�ָ����񣬻ָ�ԭ����������񣨵�һ������������ʹ�ù����״̬�ָ���ǰ���ݿ����ӡ�*/
    protected void doResume(Object resumeTransaction, AbstractTransactionStatus defStatus) throws SQLException {
        throw new TransactionSuspensionNotSupportedException("Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
    }
    //
    //
    //
    /**������ڴ��������״̬�Ƿ�λ��ջ���������׳��쳣*/
    private void prepareCheckStack(AbstractTransactionStatus defStatus) {
        if (!this.isTopTransaction(defStatus))
            throw new IllegalTransactionStateException("the Transaction Status is not top in stack.");
    }
    /**commit,rollback��֮�����������ͬʱҲ����ָ�����Ͳ��������ջ��*/
    private void cleanupAfterCompletion(AbstractTransactionStatus defStatus) {
        /*��������������λ��ջ��*/
        prepareCheckStack(defStatus);
        /*������*/
        defStatus.setCompleted();
        /*�ָ����������*/
        if (defStatus.getSuspendedTransactionHolder() != null) {
            if (Hasor.isDebugLogger())
                Hasor.logDebug("Resuming suspended transaction after completion of inner transaction");
            resume(defStatus.getSuspendedTransactionHolder(), defStatus);
        }
    }
    /**��ȡ��ǰ����������д��ڵ��������*/
    protected abstract Object doGetTransaction();
}