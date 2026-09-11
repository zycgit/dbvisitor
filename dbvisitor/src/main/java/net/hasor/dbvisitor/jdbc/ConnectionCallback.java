/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 通用的回调接口。用来执行基于 JDBC {@link Connection}
 * 上的任意数量任意类型数据库操作。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-9
 */
@FunctionalInterface
public interface ConnectionCallback<T> {
    /**
     * 执行一个 JDBC 操作。开发者不需要关心数据库连接的状态和事务。
     * @param con 一个可用的 JDBC 数据库连接
     * @return 返回操作执行的最终结果。
     */
    T doInConnection(Connection con) throws SQLException;
}
