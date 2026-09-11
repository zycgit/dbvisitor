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
 * 动态获取 Connection。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-10
 */
public interface DynamicConnection {
    /** 获取数据库连接 */
    Connection getConnection() throws SQLException;

    /** 释放数据库连接 */
    void releaseConnection(Connection conn) throws SQLException;
}
