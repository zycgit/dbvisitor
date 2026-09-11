/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.core;
/**
 * lambda Delete for Entity.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public interface ConditionFunc<R> {

    /** 允许空 Where条件（注意：空 Where 条件会导致删除或更新整个数据库） */
    R allowEmptyWhere();
}
