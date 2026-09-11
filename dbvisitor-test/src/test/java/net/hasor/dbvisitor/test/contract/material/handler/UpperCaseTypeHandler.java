/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.handler;
import java.sql.*;
import net.hasor.dbvisitor.types.TypeHandler;

/** 简单的自定义 TypeHandler：将字符串转为大写存储，读取时保持原样 */
public class UpperCaseTypeHandler implements TypeHandler<String> {
    @Override
    public void setParameter(PreparedStatement ps, int i, String parameter, Integer jdbcType) throws SQLException {
        if (parameter != null) {
            ps.setString(i, parameter.toUpperCase());
        } else {
            ps.setNull(i, Types.VARCHAR);
        }
    }

    @Override
    public String getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    @Override
    public String getResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }

    @Override
    public String getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getString(columnIndex);
    }
}
