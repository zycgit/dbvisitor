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
import java.sql.Savepoint;
import javax.sql.DataSource;
import net.hasor.jdbc.datasource.SavepointManager;
import net.hasor.jdbc.exceptions.DataAccessException;
/**
 * 
 * @version : 2013-12-10
 * @author ������(zyc@hasor.net)
 */
public class ConnectionHolder implements SavepointManager {
    private int        referenceCount;
    private DataSource dataSource;
    private Connection connection;
    public ConnectionHolder(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    /**�������ü���,һ����Ϊ�������ѱ�����*/
    public synchronized void requested() {
        this.referenceCount++;
    }
    /**�������ü���,һ����Ϊ�������ѱ��ͷš�*/
    public synchronized void released() {
        this.referenceCount--;
        if (!isOpen() && this.connection != null) {
            try {
                this.savepointCounter = 0;
                this.savepointsSupported = null;
                this.connection.close();
            } catch (SQLException e) {
                throw new DataAccessException("cant not close connection.", e);
            } finally {
                this.connection = null;
            }
        }
    }
    public boolean isOpen() {
        if (referenceCount == 0)
            return false;
        return true;
    }
    /**��ȡ����*/
    public synchronized Connection getConnection() throws SQLException {
        if (this.isOpen() == false)
            return null;
        if (this.connection == null) {
            this.connection = this.dataSource.getConnection();
        }
        return this.connection;
    }
    //
    //
    //
    //---------------------------------------------------------------------------Savepoint
    private void checkConn(Connection conn) throws SQLException {
        if (conn == null)
            throw new SQLException("Connection is null.");
    }
    public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";
    private int                savepointCounter      = 0;
    private Boolean            savepointsSupported;
    /**���� JDBC �����Ƿ�֧�ֱ���㡣*/
    public boolean supportsSavepoints() throws SQLException {
        Connection conn = this.getConnection();
        checkConn(conn);
        //
        if (this.savepointsSupported == null)
            this.savepointsSupported = conn.getMetaData().supportsSavepoints();
        return this.savepointsSupported;
    }
    /**ʹ��һ��ȫ�µ����ƴ���һ������㡣*/
    public Savepoint createSavepoint() throws SQLException {
        Connection conn = this.getConnection();
        checkConn(conn);
        //
        this.savepointCounter++;
        return conn.setSavepoint(SAVEPOINT_NAME_PREFIX + this.savepointCounter);
    }
    public void rollbackToSavepoint(Savepoint savepoint) throws SQLException {
        Connection conn = this.getConnection();
        checkConn(conn);
        //
        conn.rollback(savepoint);
    }
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        Connection conn = this.getConnection();
        checkConn(conn);
        //
        conn.releaseSavepoint(savepoint);
    };
}
