/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.db.transaction;

import net.hasor.db.transaction.support.SavepointManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * Connection 引用计数器
 * @version : 2014-3-29
 * @author 赵永春 (zyc@hasor.net)
 */
class ConnectionHolderImpl implements ConnectionHolder, SavepointManager {
    private       int        referenceCount;
    private final DataSource dataSource;
    private       Connection connection;

    ConnectionHolderImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** 增加引用计数,一个因为持有人已被请求 */
    public synchronized void requested() {
        this.referenceCount++;
    }

    /** 减少引用计数,一个因为持有人已被释放 */
    public synchronized void released() throws SQLException {
        this.referenceCount--;
        if (!this.isOpen() && this.connection != null) {
            try {
                this.savepointCounter = 0;
                this.connection.close();
            } finally {
                this.connection = null;
            }
        }
    }

    @Override
    public int getRefCount() {
        return this.referenceCount;
    }

    /** 获取数据库连接 */
    public synchronized Connection getConnection() throws SQLException {
        if (!this.isOpen()) {
            return null;
        }
        if (this.connection == null) {
            this.connection = this.dataSource.getConnection();
        }
        return this.connection;
    }

    /** 则表示当前数据库连接是否被打开，被打开的连接一定有引用 */
    public boolean isOpen() {
        return this.referenceCount != 0;
    }

    /** 则表示当前数据库连接是否有被引用 */
    public DataSource getDataSource() {
        return dataSource;
    }

    //---------------------------------------------------------------------------Savepoint

    private Connection checkConn(final Connection conn) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection is null.");
        }
        return conn;
    }

    private static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";
    private              int    savepointCounter      = 0;

    /** 则表示当前数据库连接是否被打开(被打开的连接一定有引用) */
    public boolean supportSavepoint() throws SQLException {
        Connection conn = this.getConnection();
        if (conn == null) {
            throw new IllegalStateException("connection is close.");
        }
        return conn.getMetaData().supportsSavepoints();
    }

    /** 使用一个全新的名称创建一个保存点 */
    public Savepoint createSavepoint() throws SQLException {
        Connection conn = this.checkConn(this.getConnection());
        this.savepointCounter++;
        return conn.setSavepoint(SAVEPOINT_NAME_PREFIX + this.savepointCounter);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        Connection conn = this.checkConn(this.getConnection());
        conn.releaseSavepoint(savepoint);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        Connection conn = this.checkConn(this.getConnection());
        conn.rollback(savepoint);
    }

}
