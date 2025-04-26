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
package net.hasor.dbvisitor.transaction.support;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * 保存点管理器接口，提供保存点相关操作
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-12-14
 */
public interface SavepointManager {
    /**
     * 检查当前连接是否支持保存点
     * @return true表示支持保存点，false表示不支持
     * @throws SQLException 如果检查过程中发生数据库错误
     */
    boolean supportSavepoint() throws SQLException;

    /**
     * 创建新的保存点
     * @return 创建的保存点对象
     * @throws SQLException 如果创建保存点时发生数据库错误
     */
    Savepoint createSavepoint() throws SQLException;

    /**
     * 释放指定的保存点资源
     * @param savepoint 要释放的保存点对象
     * @throws SQLException 如果释放过程中发生数据库错误
     */
    void releaseSavepoint(Savepoint savepoint) throws SQLException;

    /**
     * 回滚到指定保存点
     * @param savepoint 要回滚到的保存点对象
     * @throws SQLException 如果回滚过程中发生数据库错误
     */
    void rollback(Savepoint savepoint) throws SQLException;
}
