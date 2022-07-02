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
package net.hasor.db.transaction.support;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * Savepoint Manager
 * @version : 2021-12-14
 * @author 赵永春 (zyc@hasor.net)
 */
public interface SavepointManager {
    /** 则表示当前数据库连接是否被打开(被打开的连接一定有引用) */
    public boolean supportSavepoint() throws SQLException;

    /** 使用一个全新的名称创建一个保存点 */
    public Savepoint createSavepoint() throws SQLException;

    /** 释放某个事务的保存点 */
    public void releaseSavepoint(Savepoint savepoint) throws SQLException;

    /** 回滚事务到一个指定的保存点 */
    public void rollback(Savepoint savepoint) throws SQLException;
}
