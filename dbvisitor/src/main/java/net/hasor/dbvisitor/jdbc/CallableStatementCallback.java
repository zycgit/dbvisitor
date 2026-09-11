/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc;
import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * 处理存储过程的调用。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-9
 */
@FunctionalInterface
public interface CallableStatementCallback<T> {
    /**
     * 执行存储过程调用并返回所需的结果，开发者不需要关心数据库连接的状态和事务。
     */
    T doInCallableStatement(CallableStatement cs) throws SQLException;
}
