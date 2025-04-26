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
 * {@link TransactionCallback}接口的无返回值版本。
 * 用于在事务中执行不需要返回结果的操作。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2015-08-11
 */
public interface TransactionCallbackWithoutResult extends TransactionCallback<Void> {
    /**
     * 实现自父接口的方法，调用doTransactionWithoutResult后返回null
     */
    default Void doTransaction(TransactionStatus tranStatus) throws Throwable {
        this.doTransactionWithoutResult(tranStatus);
        return null;
    }

    /**
     * 在事务中执行无返回值的操作
     * @param tranStatus 当前事务状态对象
     * @throws Throwable 执行过程中可能抛出的异常
     */
    void doTransactionWithoutResult(TransactionStatus tranStatus) throws Throwable;
}