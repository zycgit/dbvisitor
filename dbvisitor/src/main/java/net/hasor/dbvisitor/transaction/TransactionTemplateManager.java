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
 * 事务模板管理器，提供声明式事务管理功能
 * 封装了事务的开始、提交、回滚等操作，简化事务使用
 * @author 赵永春 (zyc@hasor.net)
 * @version 2015-10-22
 */
public class TransactionTemplateManager implements TransactionTemplate {
    private final TransactionManager transactionManager;

    /**
     * 构造函数
     * @param transactionManager 事务管理器实例
     */
    public TransactionTemplateManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 在事务中执行回调逻辑
     * @param callBack 事务回调接口
     * @param behavior 事务传播行为
     * @param level 事务隔离级别
     * @return 回调方法的执行结果
     * @throws Throwable 执行过程中可能抛出的异常
     */
    public <T> T execute(TransactionCallback<T> callBack, Propagation behavior, Isolation level) throws Throwable {
        TransactionStatus tranStatus = null;
        try {
            tranStatus = this.transactionManager.begin(behavior, level);
            return callBack.doTransaction(tranStatus);
        } catch (Throwable e) {
            if (tranStatus != null) {
                tranStatus.setRollback();
            }
            throw e;
        } finally {
            if (tranStatus != null && !tranStatus.isCompleted()) {
                this.transactionManager.commit(tranStatus);
            }
        }
    }
}