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
package net.hasor.plugins.transaction.support;
import static net.hasor.plugins.transaction.TransactionBehavior.PROPAGATION_MANDATORY;
import static net.hasor.plugins.transaction.TransactionBehavior.PROPAGATION_NESTED;
import static net.hasor.plugins.transaction.TransactionBehavior.PROPAGATION_NEVER;
import static net.hasor.plugins.transaction.TransactionBehavior.PROPAGATION_NOT_SUPPORTED;
import static net.hasor.plugins.transaction.TransactionBehavior.PROPAGATION_REQUIRED;
import static net.hasor.plugins.transaction.TransactionBehavior.RROPAGATION_REQUIRES_NEW;
import java.sql.SQLException;
import java.util.LinkedList;
import javax.sql.DataSource;
import net.hasor.core.Hasor;
import net.hasor.jdbc.datasource.DataSourceUtils;
import net.hasor.jdbc.datasource.local.ConnectionSequence;
import net.hasor.jdbc.datasource.local.LocalDataSourceHelper;
import net.hasor.jdbc.exceptions.IllegalTransactionStateException;
import net.hasor.jdbc.exceptions.TransactionDataAccessException;
import net.hasor.plugins.transaction.TransactionBehavior;
import net.hasor.plugins.transaction.TransactionLevel;
import net.hasor.plugins.transaction.TransactionManager;
import net.hasor.plugins.transaction.TransactionStatus;
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
public abstract class DefaultTransactionManager implements TransactionManager {
    private int                                  defaultTimeout = -1;
    private LinkedList<DefaultTransactionStatus> tStatusStack   = new LinkedList<DefaultTransactionStatus>();
    private DataSource                           dataSource     = null;
    public DefaultTransactionManager(DataSource dataSource) {
        Hasor.assertIsNotNull(dataSource);
        this.dataSource = dataSource;
    }
    //
    //
    /**��ȡ��ǰ������������������Դ����*/
    public DataSource getDataSource() {
        return this.dataSource;
    };
    /**�Ƿ����δ����������񣨰�������������񣩡�*/
    public boolean hasTransaction() {
        return !tStatusStack.isEmpty();
    }
    /**��������״̬�Ƿ�λ��ջ����*/
    public boolean isTopTransaction(TransactionStatus status) {
        if (tStatusStack.isEmpty())
            return false;
        return this.tStatusStack.peek() == status;
    }
    //
    //
    /**��������*/
    public final TransactionStatus getTransaction(TransactionBehavior behavior) throws TransactionDataAccessException {
        return getTransaction(behavior, TransactionLevel.ISOLATION_DEFAULT);
    };
    /**��������*/
    public final TransactionStatus getTransaction(TransactionBehavior behavior, TransactionLevel level) throws TransactionDataAccessException {
        Hasor.assertIsNotNull(behavior);
        Hasor.assertIsNotNull(level);
        //
        TransactionObject tranConn = doGetConnection();/*��ȡ�µ����ӣ��̰߳󶨵ģ�*/
        DefaultTransactionStatus defStatus = new DefaultTransactionStatus(behavior, level, tranConn);
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
        if (this.isExistingTransaction(tranConn) == true) {
            /*RROPAGATION_REQUIRES_NEW����������*/
            if (behavior == RROPAGATION_REQUIRES_NEW) {
                this.suspend(defStatus);/*��������*/
                tranConn = doGetConnection();/*������������*/
                defStatus.setTranConn(tranConn);
                this.doBegin(defStatus);/*����������*/
            }
            /*PROPAGATION_NESTED��Ƕ������*/
            if (behavior == PROPAGATION_NESTED) {
                defStatus.markHeldSavepoint();/*���ñ����*/
            }
            /*PROPAGATION_NOT_SUPPORTED��������ʽ*/
            if (behavior == PROPAGATION_NOT_SUPPORTED) {
                this.suspend(defStatus);/*��������*/
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
            this.doBegin(defStatus);/*����������*/
        }
        /*PROPAGATION_MANDATORY��ǿ��Ҫ������*/
        if (behavior == PROPAGATION_MANDATORY)
            throw new IllegalTransactionStateException("No existing transaction found for transaction marked with propagation 'mandatory'");
        return defStatus;
    }
    /**ʹ��defStatus�е����ӿ���һ��ȫ�µ�����*/
    protected void doBegin(DefaultTransactionStatus defStatus) {
        try {
            TransactionObject tranConn = defStatus.getTranConn();
            tranConn.beginTransaction();
            defStatus.markNewConnection();/*�����������*/
            this.tStatusStack.push(defStatus);/*��ջ*/
            //
        } catch (Throwable ex) {
            throw new TransactionDataAccessException(ex);
        }
    }
    /**�ж����Ӷ����Ƿ��������У��÷����������������񴫲����ԵĴ���ʽ�� */
    private boolean isExistingTransaction(TransactionObject tranConn) throws TransactionDataAccessException {
        try {
            return tranConn.hasTransaction();
        } catch (Throwable e) {
            throw new TransactionDataAccessException(e);
        }
    };
    //
    //
    /**�ݽ�����*/
    public final void commit(TransactionStatus status) throws TransactionDataAccessException {
        DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
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
                doCommit(defStatus);
            //
        } catch (Throwable ex) {
            rollBack(defStatus);/*�ݽ�ʧ�ܣ��ع�*/
            throw new TransactionDataAccessException(ex);
        } finally {
            cleanupAfterCompletion(defStatus);
        }
    }
    /**�ݽ�ǰ��Ԥ����*/
    private void prepareCommit(DefaultTransactionStatus defStatus) {
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
    protected void doCommit(DefaultTransactionStatus defStatus) throws SQLException {
        TransactionObject tranObject = defStatus.getTranConn();
        tranObject.commit();
    };
    //
    //
    /**�ع�����*/
    public final void rollBack(TransactionStatus status) throws TransactionDataAccessException {
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
            else if (defStatus.isNewConnection())
                doRollback(defStatus);
            //
        } catch (Throwable ex) {
            throw new TransactionDataAccessException("SQL Exception :", ex);
        } finally {
            cleanupAfterCompletion(defStatus);
        }
    }
    /**�ع�ǰ��Ԥ����*/
    private void prepareRollback(DefaultTransactionStatus defStatus) {
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
    protected void doRollback(DefaultTransactionStatus defStatus) throws SQLException {
        TransactionObject tranObject = defStatus.getTranConn();
        tranObject.rollback();
    };
    //
    //
    /**��������*/
    protected final void suspend(DefaultTransactionStatus defStatus) {
        /*��������Ƿ�Ϊջ������*/
        prepareCheckStack(defStatus);
        /*��������*/
        if (defStatus.isSuspend() == false) {
            defStatus.setSuspendConn(defStatus.getTranConn());
            defStatus.setTranConn(null);
            //��ʱ���ص�ǰ�̵߳����ݿ�����
            SyncTransactionManager.inStack(this.getDataSource());
        }
    }
    /**�ָ������������*/
    protected final void resume(DefaultTransactionStatus defStatus) {
        if (defStatus.isCompleted() == false)
            throw new IllegalTransactionStateException("the Transaction has not completed.");
        if (defStatus.isSuspend() == false)
            throw new IllegalTransactionStateException("the Transaction has not Suspend.");
        //
        /*��������Ƿ�Ϊջ������*/
        prepareCheckStack(defStatus);
        /*�ָ����������*/
        if (defStatus.isSuspend() == true) {
            TransactionObject tranConn = defStatus.getSuspendConn();
            defStatus.setTranConn(tranConn);
            defStatus.setSuspendConn(null);
            //�ָ����ص����ݿ�����
            SyncTransactionManager.outStack(this.getDataSource());
        }
    }
    //
    //
    /**������ڴ��������״̬�Ƿ�λ��ջ���������׳��쳣*/
    private void prepareCheckStack(DefaultTransactionStatus defStatus) {
        if (!this.isTopTransaction(defStatus))
            throw new IllegalTransactionStateException("the Transaction Status is not top in stack.");
    }
    /**commit,rollback��֮�����������ͬʱҲ����ָ�����Ͳ��������ջ��*/
    private void cleanupAfterCompletion(DefaultTransactionStatus defStatus) {
        /*��������������λ��ջ��*/
        prepareCheckStack(defStatus);
        /*������*/
        defStatus.setCompleted();
        /*��ջ*/
        this.tStatusStack.pop();
        /*�ָ����������*/
        if (defStatus.isSuspend())
            this.resume(defStatus);
        /*�ͷ���Դ*/
        if (defStatus.isNewConnection())
            this.doReleaseConnection(defStatus.getTranConn());
        /*����defStatus*/
        defStatus.setTranConn(null);
        defStatus.setSuspendConn(null);
    }
    //
    //
    /**��ȡ���ӣ��̰߳󶨵ģ�*/
    protected TransactionObject doGetConnection() {
        LocalDataSourceHelper localHelper = (LocalDataSourceHelper) DataSourceUtils.getDataSourceHelper();
        return SyncTransactionManager.getTransaction(getDataSource());
    };
    /**��ȡ���ӣ��̰߳󶨵ģ�*/
    protected void doReleaseConnection(TransactionObject tranObject) {
        return SyncTransactionManager.getTransaction(getDataSource());
    };
}
/** */
class SyncTransactionManager {
    /**����*/
    public static void inStack(DataSource dataSource) {
        try {
            LocalDataSourceHelper localHelper = (LocalDataSourceHelper) DataSourceUtils.getDataSourceHelper();
            localHelper.getConnectionSequence(dataSource).push(null);/*����߳��ϵ����񣬽��������*/
            //2.����ȡ�õ�ǰ����
            localHelper.getConnection(dataSource);
        } catch (SQLException e) {
            throw new TransactionDataAccessException(e);
        }
    }
    /**�ָ�*/
    public static void outStack(DataSource dataSource) {
        //
        LocalDataSourceHelper localHelper = (LocalDataSourceHelper) DataSourceUtils.getDataSourceHelper();
        ConnectionSequence connSeq = localHelper.getConnectionSequence(dataSource);
        connSeq.pop();/*1.*/
        connSeq.pop();/*2.*/
    }
}