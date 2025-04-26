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
/**
 * 事务模板接口，提供统一的事务执行模板方法。
 * 通过回调机制封装事务管理逻辑，简化编程式事务的使用。
 *
 * @author 赵永春 (zyc@hasor.net)
 * @version 2015-10-22
 */
public interface TransactionTemplate {
    /**
     * 使用默认传播行为(REQUIRED)和默认隔离级别执行事务
     * @param <T> 返回结果类型
     * @param callBack 事务回调接口，包含需要在事务中执行的业务逻辑
     * @return 返回 {@link TransactionCallback} 接口执行的返回值。
     * @throws Throwable 执行过程中可能抛出的异常
     */
    default <T> T execute(TransactionCallback<T> callBack) throws Throwable {
        return this.execute(callBack, Propagation.REQUIRED, null);
    }

    /**
     * 使用指定传播行为和默认隔离级别执行事务
     * @param <T> 返回结果类型
     * @param callBack 事务回调接口
     * @param behavior 事务传播行为，定义事务如何传播(如REQUIRED, REQUIRES_NEW等)
     * @return 返回 {@link TransactionCallback} 接口执行的返回值。
     * @throws Throwable 执行过程中可能抛出的异常
     */
    default <T> T execute(TransactionCallback<T> callBack, Propagation behavior) throws Throwable {
        return this.execute(callBack, behavior, null);
    }

    /**
     * 使用指定传播行为和隔离级别执行事务
     * @param <T> 返回结果类型
     * @param callBack 事务回调接口
     * @param behavior 事务传播行为
     * @param level 事务隔离级别(如READ_COMMITTED, SERIALIZABLE等)，null表示使用默认级别
     * @return 返回 {@link TransactionCallback} 接口执行的返回值。
     * @throws Throwable 执行过程中可能抛出的异常
     */
    <T> T execute(TransactionCallback<T> callBack, Propagation behavior, Isolation level) throws Throwable;
}