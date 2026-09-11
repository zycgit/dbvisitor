/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 该接口用于创建 {@link CallableStatement} 对象。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-9
 */
@FunctionalInterface
public interface CallableStatementCreator {
    /** 创建 {@link CallableStatement} 对象 */
    CallableStatement createCallableStatement(Connection con) throws SQLException;
}
