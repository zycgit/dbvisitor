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
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 连接管理器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-30
 */
public interface ConnectionHolder {
    /** 增加引用计数,一个因为持有人已被请求 */
    void requested();

    /** 减少引用计数,一个因为持有人已被释放 */
    void released() throws SQLException;

    int getRefCount();

    /** 获取数据库连接 */
    Connection getConnection() throws SQLException;

    /** 获取数据库连接 */
    DataSource getDataSource() throws SQLException;

    /** 则表示当前数据库连接是否被打开(被打开的连接一定有引用) */
    boolean isOpen();

    /** 是否存在事务 */
    default boolean hasTransaction() throws SQLException {
        Connection conn = this.getConnection();
        if (conn == null) {
            return false;
        }
        //AutoCommit被标记为 false 表示开启了事务。
        return !conn.getAutoCommit();
    }

    /** 设置事务状态 */
    default void setTransaction() throws SQLException {
        Connection conn = this.getConnection();
        if (conn == null) {
            throw new IllegalStateException("connection is close.");
        }

        if (conn.getAutoCommit()) {
            conn.setAutoCommit(false);
        }
    }

    /** 取消事务状态,设置为自动递交 */
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