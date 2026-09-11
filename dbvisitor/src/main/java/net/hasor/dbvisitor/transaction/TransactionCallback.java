/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.transaction;
/**
 * 事务回调模板接口，用于在事务环境中执行自定义业务逻辑。
 * 这是一个函数式接口，可通过lambda表达式实现。
 * @param <T> 定义事务执行后返回结果的类型
 * @author 赵永春 (zyc@hasor.net)
 * @version 2015-08-11
 */
@FunctionalInterface
public interface TransactionCallback<T> {
    /**
     * 在事务上下文中执行自定义逻辑
     * @param tranStatus 当前事务状态对象，可通过其setRollbackOnly()方法标记事务回滚
     * @return 事务执行结果
     * @throws Throwable 执行过程中抛出的任何异常都会触发事务回滚
     */
    T doTransaction(TransactionStatus tranStatus) throws Throwable;
}
