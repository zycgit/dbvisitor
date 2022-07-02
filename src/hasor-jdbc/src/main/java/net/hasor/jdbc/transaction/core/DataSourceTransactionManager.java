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
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.noe.platform.modules.db.jdbcorm.jdbc.datasource.DataSourceUtils;
import net.hasor.Hasor;
import net.hasor.jdbc.transaction.TransactionSynchronizationManager;
import net.hasor.jdbc.transaction._.ConnectionHolder;
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
    protected Object doGetTransaction() {
        ConnectionHolder connHolder = TransactionSynchronizationManager.getConnectionHolder(this.dataSource);
        DataSourceTransactionObject dtObject = new DataSourceTransactionObject(connHolder);
        return dtObject;
    }
    protected boolean isExistingTransaction(Object transaction) {
        DataSourceTransactionObject dtObject = (DataSourceTransactionObject) transaction;
        return dtObject.getConnectionHolder().hasTransaction();
    }
    //
    //
    //
    protected void doBegin(Object transaction, DefaultTransactionStatus status) throws SQLException {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        /*��ǰ�̰߳󶨵�ConnectionHolder*/
        ConnectionHolder localHolder = txObject.getConnectionHolder();
        /*Ϊ������������� Connection*/
        if (localHolder == null || status.isNew()) {
            Connection newCon = this.dataSource.getConnection();
            if (Hasor.isDebugLogger())
                Hasor.debug("Acquired Connection [" + newCon + "] for JDBC transaction");
            localHolder = new ConnectionHolder(newCon);
            txObject.setConnectionHolder(localHolder, true);
        }
        /*ȡ��������뼶��*/
        Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
        txObject.setPreviousIsolationLevel(previousIsolationLevel);
    }
    protected void doResume(DefaultTransactionStatus defStatus, Object resumeTransaction) throws SQLException {
        DataSourceTransactionObject dtObject = (DataSourceTransactionObject) resumeTransaction;
        // TODO Auto-generated method stub
    }
    protected void doSuspend(Object transaction, DefaultTransactionStatus defStatus) throws SQLException {
        // TODO Auto-generated method stub
    }
    protected void doCommit(Object transaction, DefaultTransactionStatus status) throws SQLException {
        // TODO Auto-generated method stub
    }
    protected void doRollback(Object transaction, DefaultTransactionStatus status) throws SQLException {
        // TODO Auto-generated method stub
    }
}