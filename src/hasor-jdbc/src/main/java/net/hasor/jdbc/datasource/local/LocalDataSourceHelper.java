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
package net.hasor.jdbc.datasource.local;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import net.hasor.jdbc.datasource.DataSourceHelper;
/**
 * 
 * @version : 2013-12-2
 * @author ������(zyc@hasor.net)
 */
public class LocalDataSourceHelper implements DataSourceHelper {
    private static final ThreadLocal<Map<DataSource, ConnectionSequence>> ResourcesLocal;
    static {
        ResourcesLocal = new ThreadLocal<Map<DataSource, ConnectionSequence>>() {
            protected Map<DataSource, ConnectionSequence> initialValue() {
                return new HashMap<DataSource, ConnectionSequence>();
            }
        };
    }
    /**�������ӣ������ǰ���Ӵ����򷵻ص�ǰ����*/
    public Connection getConnection(DataSource dataSource) throws SQLException {
        ConnectionSequence conSeq = getConnectionSequence(dataSource);/*��ȡ����*/
        ConnectionHolder connHolder = conSeq.currentHolder();/*��ȡ��ǰHolder*/
        connHolder.requested();/*���ü���+1*/
        return connHolder.getConnection();/*��������*/
    };
    /**�ͷ�����*/
    public void releaseConnection(Connection con, DataSource dataSource) throws SQLException {
        ConnectionSequence conSeq = getConnectionSequence(dataSource);//��ȡ����
        ConnectionHolder holder = conSeq.currentHolder();/*��ȡ��ǰHolder*/
        if (holder != null)
            holder.released();/*���ü���-1*/
    };
    public Connection currentConnection(DataSource dataSource) throws SQLException {
        ConnectionSequence conSeq = getConnectionSequence(dataSource);//��ȡ����
        ConnectionHolder holder = conSeq.currentHolder();/*��ȡ��ǰHolder*/
        return holder.getConnection();/*��������*/
    }
    /**��ȡConnectionSequence*/
    public ConnectionSequence getConnectionSequence(DataSource dataSource) {
        ConnectionSequence conSeq = ResourcesLocal.get().get(dataSource);
        /*��������*/
        if (conSeq == null) {
            conSeq = createConnectionSequence();
            ResourcesLocal.get().put(dataSource, conSeq);
        }
        /*�½�ConnectionHolder*/
        if (conSeq.currentHolder() == null) {
            conSeq.currentHolder(this.createConnectionHolder(dataSource));
        }
        return conSeq;
    }
    /**����ConnectionSequence����*/
    protected ConnectionSequence createConnectionSequence() {
        return new ConnectionSequence();
    }
    /**����ConnectionHolder����*/
    protected ConnectionHolder createConnectionHolder(DataSource dataSource) {
        return new ConnectionHolder(dataSource);
    }
}