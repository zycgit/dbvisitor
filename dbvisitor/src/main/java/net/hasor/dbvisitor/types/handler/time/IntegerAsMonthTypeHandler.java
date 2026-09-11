/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.time;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Month;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link java.time.Month} 类型读写 jdbc int 数据。格式为 MM，例如：01～12
 * @author 赵永春 (zyc@hasor.net)
 */
public class IntegerAsMonthTypeHandler extends AbstractTypeHandler<Month> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Month month, Integer jdbcType) throws SQLException {
        ps.setInt(i, month.getValue());
    }

    @Override
    public Month getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int month = rs.getInt(columnName);
        return month == 0 && rs.wasNull() ? null : Month.of(month);
    }

    @Override
    public Month getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int month = rs.getInt(columnIndex);
        return month == 0 && rs.wasNull() ? null : Month.of(month);
    }

    @Override
    public Month getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int month = cs.getInt(columnIndex);
        return month == 0 && cs.wasNull() ? null : Month.of(month);
    }
}
