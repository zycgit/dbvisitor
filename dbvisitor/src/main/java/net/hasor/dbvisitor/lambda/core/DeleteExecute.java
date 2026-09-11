/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.core;
import java.sql.SQLException;

/**
 * lambda Delete 执行器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-27
 */
public interface DeleteExecute<R> extends BasicFunc<R>, ConditionFunc<R>, BoundSqlBuilder {
    /** 根据 Lambda 构造器的条件执行删除 */
    int doDelete() throws SQLException;
}
