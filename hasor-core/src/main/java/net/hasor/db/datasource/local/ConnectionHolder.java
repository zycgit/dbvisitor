/*
 * Copyright 2008-2009 the original 赵永春(zyc@hasor.net).
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
package net.hasor.db.datasource.local;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import javax.sql.DataSource;
import net.hasor.db.datasource.SavepointManager;
/**
 * 
 * @version : 2014-3-29
 * @author 赵永春 (zyc@byshell.org)
 */
public class ConnectionHolder implements SavepointManager {
    private int        referenceCount;
    private DataSource dataSource;
    private Connection connection;
    public ConnectionHolder(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    /**增加引用计数,一个因为持有人已被请求。*/
    public synchronized void requested() {
        this.referenceCount++;
    }
    /**减少引用计数,一个因为持有人已被释放。 */
    public synchronized void released() throws SQLException {
        this.referenceCount--;
        if (!this.isOpen() && this.connection != null)
            try {
                this.savepointCounter = 0;
                this.savepointsSupported = null;
                this.connection.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                this.connection = null;
            }
    }
    //
    public boolean isOpen() {
        if (this.referenceCount == 0)
            return false;
        return true;
    }
    /**获取连接*/
    public synchronized Connection getConnection() throws SQLException {
        if (this.isOpen() == false)
            return null;
        if (this.connection == null)
            this.connection = this.dataSource.getConnection();
        return this.connection;
    }
    /**是否存在事务*/
    public boolean hasTransaction() throws SQLException {
        Connection conn = this.getConnection();
        if (conn == null)
            return false;
        //AutoCommit被标记为 false 表示开启了事务。
        return conn.getAutoCommit() == false ? true : false;
    }
    /**设置事务状态*/
    public void setTransaction() throws SQLException {
        Connection conn = this.getConnection();
        if (conn != null && conn.getAutoCommit() == true)
            conn.setAutoCommit(false);
    }
    /**取消事务状态*/
    public void cancelTransaction() throws SQLException {
        Connection conn = this.getConnection();
        if (conn != null && conn.getAutoCommit() == false)
            conn.setAutoCommit(true);
    }
    //
    //
    //
    //---------------------------------------------------------------------------Savepoint
    private void checkConn(final Connection conn) throws SQLException {
        if (conn == null)
            throw new SQLException("Connection is null.");
    }
    public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";
    private int                savepointCounter      = 0;
    private Boolean            savepointsSupported;
    /**返回 JDBC 驱动是否支持保存点。*/
    public boolean supportsSavepoints() throws SQLException {
        Connection conn = this.getConnection();
        this.checkConn(conn);
        //
        if (this.savepointsSupported == null)
            this.savepointsSupported = conn.getMetaData().supportsSavepoints();
        return this.savepointsSupported;
    }
    /**使用一个全新的名称创建一个保存点。*/
    @Override
    public Savepoint createSavepoint() throws SQLException {
        Connection conn = this.getConnection();
        this.checkConn(conn);
        //
        this.savepointCounter++;
        return conn.setSavepoint(ConnectionHolder.SAVEPOINT_NAME_PREFIX + this.savepointCounter);
    }
    @Override
    public void rollbackToSavepoint(final Savepoint savepoint) throws SQLException {
        Connection conn = this.getConnection();
        this.checkConn(conn);
        //
        conn.rollback(savepoint);
    }
    @Override
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
        Connection conn = this.getConnection();
        this.checkConn(conn);
        //
        conn.releaseSavepoint(savepoint);
    }
    @Override
    public boolean supportSavepoint() throws SQLException {
        Connection conn = this.getConnection();
        this.checkConn(conn);
        return conn.getMetaData().supportsSavepoints();
    };
}
