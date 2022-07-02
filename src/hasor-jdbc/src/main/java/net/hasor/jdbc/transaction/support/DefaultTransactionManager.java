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
package net.hasor.jdbc.transaction.support;
import static net.hasor.jdbc.transaction.TransactionBehavior.PROPAGATION_MANDATORY;
import static net.hasor.jdbc.transaction.TransactionBehavior.PROPAGATION_NESTED;
import static net.hasor.jdbc.transaction.TransactionBehavior.PROPAGATION_NEVER;
import static net.hasor.jdbc.transaction.TransactionBehavior.PROPAGATION_NOT_SUPPORTED;
import static net.hasor.jdbc.transaction.TransactionBehavior.PROPAGATION_REQUIRED;
import static net.hasor.jdbc.transaction.TransactionBehavior.RROPAGATION_REQUIRES_NEW;
import java.sql.SQLException;
import java.util.LinkedList;
import javax.sql.DataSource;
import net.hasor.core.Hasor;
import net.hasor.jdbc.datasource.DataSourceUtils;
import net.hasor.jdbc.datasource.local.ConnectionHolder;
import net.hasor.jdbc.datasource.local.ConnectionSequence;
import net.hasor.jdbc.datasource.local.LocalDataSourceHelper;
import net.hasor.jdbc.template.exceptions.IllegalTransactionStateException;
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
public class DefaultTransactionManager implements TransactionManager {
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
    public final TransactionStatus getTransaction(TransactionBehavior behavior) throws SQLException {
        return getTransaction(behavior, TransactionLevel.ISOLATION_DEFAULT);
    };
    /**��������*/
    public final TransactionStatus getTransaction(TransactionBehavior behavior, TransactionLevel level) throws SQLException {
        Hasor.assertIsNotNull(behavior);
        Hasor.assertIsNotNull(level);
        //
        DefaultTransactionStatus defStatus = new DefaultTransactionStatus(behavior, level);
        defStatus.setTranConn(doGetConnection(defStatus));
        this.tStatusStack.push(defStatus);/*��ջ*/
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
        if (this.isExistingTransaction(defStatus) == true) {
            /*RROPAGATION_REQUIRES_NEW����������*/
            if (behavior == RROPAGATION_REQUIRES_NEW) {
                this.suspend(defStatus);/*��������*/
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
    /**�ж����Ӷ����Ƿ��������У��÷����������������񴫲����ԵĴ���ʽ�� */
    private boolean isExistingTransaction(DefaultTransactionStatus defStatus) throws SQLException {
        return defStatus.getTranConn().hasTransaction();
    };
    /**��ʼ��һ���µ����ӣ�����������*/
    protected void doBegin(DefaultTransactionStatus defStatus) throws SQLException {
        TransactionObject tranConn = defStatus.getTranConn();
        tranConn.begin();
    }
    //
    //
    /**�ݽ�����*/
    public final void commit(TransactionStatus status) throws SQLException {
        DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
        /*����ϣ�����Ҫ����*/
        if (defStatus.isCompleted())
            throw new IllegalTransactionStateException("Transaction is already completed - do not call commit or rollback more than once per transaction");
        /*-------------------------------------------------------------
        | 1.���ۺ��ִ�����ʽ���ݽ�����������Ὣ isCompleted ������Ϊ true��
        | 2.�������״̬�а���һ��δ����ı���㡣���ݽ�����㣬���ǵݽ���������
        | 3.���� isNew ֻ��Ϊ true ʱ�����������ݽ����������
        ===============================================================*/
        try {
            prepareCommit(defStatus);
            if (defStatus.isReadOnly() || defStatus.isRollbackOnly()) {
                /*�ع����*/
                if (Hasor.isDebugLogger())
                    Hasor.logDebug("Transactional code has requested rollback");
                doRollback(defStatus);
            } else {
                /*�����������㣬�ڵݽ�����ʱֻ�������*/
                if (defStatus.hasSavepoint())
                    defStatus.releaseHeldSavepoint();
                else if (defStatus.isNewConnection())
                    doCommit(defStatus);
            }
            //
        } catch (SQLException ex) {
            doRollback(defStatus);/*�ݽ�ʧ�ܣ��ع�*/
            throw ex;
        } finally {
            cleanupAfterCompletion(defStatus);
        }
    }
    /**�ݽ�ǰ��Ԥ����*/
    private void prepareCommit(DefaultTransactionStatus defStatus) throws SQLException {
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
    public final void rollBack(TransactionStatus status) throws SQLException {
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
        } catch (SQLException ex) {
            throw ex;
        } finally {
            cleanupAfterCompletion(defStatus);
        }
    }
    /**�ع�ǰ��Ԥ����*/
    private void prepareRollback(DefaultTransactionStatus defStatus) throws SQLException {
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
            TransactionObject tranConn = defStatus.getTranConn();
            defStatus.setSuspendConn(tranConn);/*����*/
            SyncTransactionManager.clearSync(this.getDataSource());/*����߳��ϵ�ͬ������*/
            defStatus.setTranConn(doGetConnection(defStatus));/*�����������ݿ�����*/
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
            TransactionObject tranConn = defStatus.getTranConn();
            this.doReleaseConnection(tranConn);/*�ͷ��������*/
            SyncTransactionManager.clearSync(this.getDataSource());/*����߳��ϵ�ͬ������*/
            //
            tranConn = defStatus.getSuspendConn();/*ȡ�ù�������ݿ�����*/
            SyncTransactionManager.setSync(tranConn);/*�����̵߳����ݿ�����*/
            //
            defStatus.setTranConn(tranConn);
            defStatus.setSuspendConn(null);
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
        /*�ͷ���Դ*/
        defStatus.getTranConn().getHolder().released();
        if (defStatus.isNewConnection())
            this.doReleaseConnection(defStatus.getTranConn());
        /*�ָ����������*/
        if (defStatus.isSuspend())
            this.resume(defStatus);
        /*����defStatus*/
        this.tStatusStack.pop();
        defStatus.setTranConn(null);
        defStatus.setSuspendConn(null);
    }
    //
    //
    //
    /**��ȡ���ݿ����ӣ��̰߳󶨵ģ�*/
    protected TransactionObject doGetConnection(DefaultTransactionStatus defStatus) {
        LocalDataSourceHelper localHelper = (LocalDataSourceHelper) DataSourceUtils.getDataSourceHelper();
        ConnectionSequence connSeq = localHelper.getConnectionSequence(getDataSource());
        ConnectionHolder holder = connSeq.currentHolder();
        if (holder.isOpen() == false)
            defStatus.markNewConnection();/*�����������*/
        holder.requested();
        return new TransactionObject(holder, getDataSource());
    };
    /**��ȡ���ӣ��̰߳󶨵ģ�*/
    protected void doReleaseConnection(TransactionObject tranObject) {
        ConnectionHolder holder = tranObject.getHolder();
        holder.released();
    };
}
/** */
class SyncTransactionManager {
    public static void setSync(TransactionObject tranConn) {
        LocalDataSourceHelper localHelper = (LocalDataSourceHelper) DataSourceUtils.getDataSourceHelper();
        ConnectionSequence connSeq = localHelper.getConnectionSequence(tranConn.getDataSource());
        connSeq.pop();
    }
    public static void clearSync(DataSource dataSource) {
        LocalDataSourceHelper localHelper = (LocalDataSourceHelper) DataSourceUtils.getDataSourceHelper();
        ConnectionSequence connSeq = localHelper.getConnectionSequence(dataSource);
        connSeq.push(null);
    }
}