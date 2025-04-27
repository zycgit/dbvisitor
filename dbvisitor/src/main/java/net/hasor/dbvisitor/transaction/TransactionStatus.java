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
import java.sql.SQLException;

/**
 * 表示一个事务状态
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-30
 */
public interface TransactionStatus {
    /**
     * 获取事务使用的传播行为
     * @return 当前事务的传播行为枚举值
     */
    Propagation getPropagation();

    /**
     * 获取事务的隔离级别
     * @return 当前事务的隔离级别枚举值
     */
    Isolation getIsolationLevel();

    /**
     * 事务是否已经完成。
     * <p>当事务已经递交或者被回滚就标志着已完成
     */
    boolean isCompleted();

    /**
     * 检查事务是否已被标记为回滚
     * @return true表示事务已被标记为回滚，commit 时将执行回滚操作
     */
    boolean isRollbackOnly();

    /** 是否为只读模式。 */
    boolean isReadOnly();

    /**
     * 检查事务是否使用全新数据库连接
     * @return true表示使用新连接，false 表示复用现有连接
     */
    boolean isNewConnection();

    /**
     * 测试该事务是否被挂起
     * @return true表示事务被挂起，false表示事务活跃中
     */
    boolean isSuspend();

    /**
     * 表示事务是否携带了一个保存点，嵌套事务通常会创建一个保存点作为嵌套事务与上一层事务的分界点。
     * <p>注意：如果事务中包含保存点，则在递交事务时只处理这个保存点
     */
    boolean hasSavepoint();

    /**
     * 设置事务状态为回滚，作为替代抛出异常进而触发回滚操作。
     * <p>只有当isCompleted 为 false 时设置该方法才会有效
     */
    void setRollback() throws SQLException;

    /**
     * 设置事务状态为只读，当执行事务commit操作时，回滚该事务。
     * <p>只有当isCompleted 为 false 时设置该方法才会有效
     */
    void setReadOnly() throws SQLException;
}