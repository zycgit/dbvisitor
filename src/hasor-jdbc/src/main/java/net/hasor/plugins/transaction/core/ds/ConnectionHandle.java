/*
 * Copyright 2002-2005 the original author or authors.
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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import javax.sql.DataSource;
import net.hasor.plugins.transaction.core.SavepointManager;
/**
 * 
 * @version : 2013-10-17
 * @author ������(zyc@hasor.net)
 */
public class ConnectionHandle implements SavepointManager {
    private Connection connection;
    private DataSource useDataSource;
    //
    private boolean    transactionActive;
    private int        referenceCount;
    // 
    public ConnectionHandle(DataSource dataSource) {
        this.useDataSource = useDataSource;
    }
    //
    //
    //
    /**�������ü���,һ����Ϊ�������ѱ�����*/
    public void requested() {
        this.referenceCount++;
    }
    /**�������ü���,һ����Ϊ�������ѱ��ͷš�*/
    public void released() {
        this.referenceCount--;
        if (!isOpen() && this.connection != null) {
            this.connection.close();
            this.connection = null;
        }
    }
    /**�����ü������� 0 ʱ������ true��*/
    public boolean isOpen() {
        return (this.referenceCount > 0);
    }
    //
    //---------------------------------------------------------------------------Savepoint
    public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";
    private int                savepointCounter      = 0;
    private Boolean            savepointsSupported;
    /**���� JDBC �����Ƿ�֧�ֱ���㡣*/
    public boolean supportsSavepoints() throws SQLException {
        if (this.savepointsSupported == null)
            this.savepointsSupported = getConnection().getMetaData().supportsSavepoints();
        return this.savepointsSupported;
    }
    /**ʹ��һ��ȫ�µ����ƴ���һ������㡣*/
    public Savepoint createSavepoint() throws SQLException {
        this.savepointCounter++;
        return getConnection().setSavepoint(SAVEPOINT_NAME_PREFIX + this.savepointCounter);
    }
    public void rollbackToSavepoint(Savepoint savepoint) throws SQLException {
        getConnection().rollback(savepoint);
    }
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        getConnection().releaseSavepoint(savepoint);
    };
    //
    //---------------------------------------------------------------------------Savepoint
    public Connection getConnection() {
        if (this.connection == null)
            this.connection = this.useDataSource.getConnection();
        return this.connection;
    };
    /**��ǰ���ӵ������Ƿ񱻼��*/
    public boolean isTransactionActive() {
        return transactionActive;
    }
}