/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.time;
import java.sql.*;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 读写 jdbc {@link Time} 数据。
 * @author Clinton Begin
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlTimeTypeHandler extends AbstractTypeHandler<Time> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Time parameter, Integer jdbcType) throws SQLException {
        ps.setTime(i, parameter);
    }

    @Override
    public Time getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getTime(columnName);
    }

    @Override
    public Time getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getTime(columnIndex);
    }

    @Override
    public Time getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getTime(columnIndex);
    }
}
