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
import java.io.Closeable;
import java.sql.SQLException;

/**
 * 数据源的事务管理器接口。
 * 提供事务的开启、提交、回滚等核心操作方法。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-30
 */
public interface TransactionManager extends Closeable {
    /**
     * 开启事务，使用默认事务隔离级别。
     * @see Propagation
     * @see TransactionManager#begin(Propagation, Isolation)
     */
    default TransactionStatus begin() throws SQLException {
        return this.begin(Propagation.REQUIRED, Isolation.DEFAULT);
    }

    /**
     * 开启事务，使用默认事务隔离级别。
     * @see Propagation
     * @see TransactionManager#begin(Propagation, Isolation)
     */
    default TransactionStatus begin(Propagation behavior) throws SQLException {
        return this.begin(behavior, null);
    }

    /**
     * 开启事务
     * @see Propagation
     * @see java.sql.Connection#setTransactionIsolation(int)
     */
    TransactionStatus begin(Propagation behavior, Isolation level) throws SQLException;

    /**
     * 递交事务
     * <p>如果递交的事务并不处于事务堆栈顶端，会同时递交该事务的后面其它事务
     */
    void commit(TransactionStatus status) throws SQLException;

    /**
     * 提交最近begin的事务
     * @throws SQLException 如果提交过程中发生错误
     */
    void commit() throws SQLException;

    /**
     * 回滚指定事务状态对应的事务
     * @param status 要回滚的事务状态对象
     * @throws SQLException 如果回滚过程中发生错误
     */
    void rollBack(TransactionStatus status) throws SQLException;

    /**
     * 回滚最近begin的事务
     * @throws SQLException 如果回滚过程中发生错误
     */
    void rollBack() throws SQLException;

    /**
     * 检查当前是否存在活动的事务
     * @return true表示存在活动事务，false表示没有活动事务
     */
    boolean hasTransaction();

    /**
     * 测试事务状态是否位于栈顶
     * @param status 要检查的事务状态对象
     * @return true表示该事务位于栈顶，false表示不是栈顶事务
     */
    boolean isTopTransaction(TransactionStatus status);
}