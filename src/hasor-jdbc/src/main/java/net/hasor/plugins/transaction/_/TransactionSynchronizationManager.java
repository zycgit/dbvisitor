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
package net.hasor.plugins.transaction._;
import javax.sql.DataSource;
import net.hasor.plugins.transaction.TransactionManager;
import net.hasor.plugins.transaction.core.ds.ConnectionHandle;
/**
 * 
 * @version : 2013-6-14
 * @author ������ (zyc@byshell.org)
 */
public class TransactionSynchronizationManager {
    //
    /**Ϊĳ������Դ����һ�������������*/
    public static TransactionManager getTransactionManager(DataSource dataSource) {
        // TODO Auto-generated method stub
        return null;
    }
    //
    //
    public static ConnectionHandle getConnectionHandle() {
        return null;
    }
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //    public static ConnectionHolder getConnectionHolder(DataSource dataSource) {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }
    //
    //
    //    public static ConnectionHolder getConnectionHolder(DataSource dataSource) {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }
    //    /**��ǰ����������Դ���Ƿ񼤻�������*/
    //    public static boolean hasTransactionActive() {
    //        Map<DataSource, ConnectionHandle> mapDS = ResourcesLocal.get();
    //        if (mapDS == null || mapDS.isEmpty())
    //            return false;
    //        for (ConnectionHandle ch : mapDS.values())
    //            if (ch.isTransactionActive())
    //                return true;
    //        return false;
    //    }; 
    //    //
    //    /**ָ��������Դ�ڵ�ǰ�߳����Ƿ񼤻�������*/
    //    public static boolean hasTransactionActive(DataSource dataSource) {
    //        Map<DataSource, ConnectionHandle> mapDS = ResourcesLocal.get();
    //        ConnectionHandle ch = mapDS.get(dataSource);
    //        return (ch == null) ? false : ch.isTransactionActive();
    //    };
}