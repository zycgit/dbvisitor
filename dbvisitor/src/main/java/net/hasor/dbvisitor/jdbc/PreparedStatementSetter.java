/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 用于处理 PreparedStatement 接口的动态参数设置。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-9
 */
@FunctionalInterface
public interface PreparedStatementSetter {
    /**
     * Set parameter values on the given PreparedStatement.
     * @param ps the PreparedStatement to invoke setter methods on
     * @throws SQLException if a SQLException is encountered (i.e. there is no need to catch SQLException)
     */
    void setValues(PreparedStatement ps) throws SQLException;
}
