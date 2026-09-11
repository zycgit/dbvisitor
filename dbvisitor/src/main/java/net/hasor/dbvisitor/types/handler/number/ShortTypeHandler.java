/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.number;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 读写 short 类型数据。
 * @author Clinton Begin
 * @author 赵永春 (zyc@hasor.net)
 */
public class ShortTypeHandler extends AbstractTypeHandler<Short> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Short parameter, Integer jdbcType) throws SQLException {
        ps.setShort(i, parameter);
    }

    @Override
    public Short getNullableResult(ResultSet rs, String columnName) throws SQLException {
        short result = rs.getShort(columnName);
        return result == 0 && rs.wasNull() ? null : result;
    }

    @Override
    public Short getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        short result = rs.getShort(columnIndex);
        return result == 0 && rs.wasNull() ? null : result;
    }

    @Override
    public Short getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        short result = cs.getShort(columnIndex);
        return result == 0 && cs.wasNull() ? null : result;
    }
}
