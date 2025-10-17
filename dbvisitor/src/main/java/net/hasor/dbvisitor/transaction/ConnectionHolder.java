/*
 * Copyright 2015-2022 the original author or authors.
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
package net.hasor.dbvisitor.transaction;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * 数据库连接持有者接口，管理数据库连接的生命周期和事务状态
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-30
 */
public interface ConnectionHolder {
    /**
     * 增加连接引用计数
     */
    void requested();

    /**
     * 减少连接引用计数
     * @throws SQLException 如果释放连接时发生错误
     */
    void released() throws SQLException;

    /**
     * 获取当前连接引用计数
     * @return 当前引用计数值
     */
    int getRefCount();

    /**
     * 获取数据库连接对象
     * @return 数据库连接
     * @throws SQLException 如果获取连接时发生错误
     */
    Connection getConnection() throws SQLException;

    /**
     * 获取数据源对象
     * @return 数据源
     * @throws SQLException 如果获取数据源时发生错误
     */
    DataSource getDataSource() throws SQLException;

    /**
     * 检查连接是否处于打开状态
     * @return true 表示连接已打开，false 表示连接已关闭
     */
    boolean isOpen();

    /**
     * 检查当前是否存在活动事务
     * @return true表示存在活动事务，false表示没有活动事务
     * @throws SQLException 如果检查过程中发生数据库错误
     */
    default boolean hasTransaction() throws SQLException {
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        //AutoCommit被标记为 false 表示开启了事务。
        return !conn.getAutoCommit();
    }

    /**
     * 开启事务（关闭自动提交模式）
     * @throws SQLException 如果设置事务状态时发生数据库错误
     * @throws IllegalStateException 如果连接已关闭
     */
    default void setTransaction() throws SQLException {
        Connection conn = this.getConnection();
        if (conn == null) {
            throw new IllegalStateException("connection is close.");
        }

        if (conn.getAutoCommit()) {
            conn.setAutoCommit(false);
        }
    }

    /**
     * 关闭事务（开启自动提交模式）
     * @throws SQLException 如果设置事务状态时发生数据库错误
     * @throws IllegalStateException 如果连接已关闭
     */
    default void cancelTransaction() throws SQLException {
        Connection conn = this.getConnection();
        if (conn == null) {
            throw new IllegalStateException("connection is close.");
        }

        if (!conn.getAutoCommit()) {
            conn.setAutoCommit(true);
        }
    }
}