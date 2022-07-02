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
package net.hasor.plugins.transaction.core.ds;
import java.sql.SQLException;
import javax.sql.DataSource;
import net.hasor.plugins.transaction.core.AbstractPlatformTransactionManager;
import net.hasor.plugins.transaction.core.AbstractTransactionStatus;
/**
 * ĳһ������Դ�����������
 * @version : 2013-10-30
 * @author ������(zyc@hasor.net)
 */
public class DataSourceTransactionManager extends AbstractPlatformTransactionManager {
    private DataSource dataSource;
    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    //
    //
    //
    //    protected void doBegin(Object transaction, DefaultTransactionStatus status) throws SQLException {
    //        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
    //        /*��ǰ�̰߳󶨵�ConnectionHolder*/
    //        ConnectionHolder localHolder = txObject.getConnectionHolder();
    //        /*Ϊ������������� Connection*/
    //        if (localHolder == null || status.isNew()) {
    //            Connection newCon = this.dataSource.getConnection();
    //            if (Hasor.isDebugLogger())
    //                Hasor.debug("Acquired Connection [" + newCon + "] for JDBC transaction");
    //            localHolder = new ConnectionHolder(newCon);
    //            txObject.setConnectionHolder(localHolder, true);
    //        }
    //        /*ȡ��������뼶��*/
    //        Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
    //        txObject.setPreviousIsolationLevel(previousIsolationLevel);
    //    }
    protected Object doGetTransaction() {
        // TODO Auto-generated method stub
        return new ConnectionHandle();
    }
    protected boolean isExistingTransaction(Object transaction) {
        ConnectionHandle connHandle = (ConnectionHandle) transaction;
        return connHandle.isTransactionActive();
    }
    protected void doBegin(Object transaction, AbstractTransactionStatus defStatus) throws SQLException {
        ConnectionHandle connHandle = (ConnectionHandle) transaction;
        connHandle.getConnection().setAutoCommit(false);
    }
    protected void doCommit(Object transaction, AbstractTransactionStatus defStatus) throws SQLException {
        ConnectionHandle connHandle = (ConnectionHandle) transaction;
        connHandle.getConnection().commit();
        connHandle.getConnection().setAutoCommit(true);
    }
    protected void doRollback(Object transaction, AbstractTransactionStatus defStatus) throws SQLException {
        ConnectionHandle connHandle = (ConnectionHandle) transaction;
        connHandle.getConnection().rollback();
        connHandle.getConnection().setAutoCommit(true);
    }
    protected void doSuspend(Object transaction, AbstractTransactionStatus defStatus) throws SQLException {
        //��յ�ǰ�̵߳� ConnectionHandle
    }
    protected void doResume(Object resumeTransaction, AbstractTransactionStatus defStatus) throws SQLException {
        //�ָ���ǰ�̵߳� ConnectionHandle
    }
}