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
import java.time.Year;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link java.time.Year} 类型读写 jdbc int 数据。数值范围：-999_999_999 ~ 999_999_999
 * @author 赵永春 (zyc@hasor.net)
 */
public class IntegerAsYearTypeHandler extends AbstractTypeHandler<Year> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Year year, Integer jdbcType) throws SQLException {
        ps.setInt(i, year.getValue());
    }

    @Override
    public Year getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int year = rs.getInt(columnName);
        return year == 0 && rs.wasNull() ? null : Year.of(year);
    }

    @Override
    public Year getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int year = rs.getInt(columnIndex);
        return year == 0 && rs.wasNull() ? null : Year.of(year);
    }

    @Override
    public Year getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int year = cs.getInt(columnIndex);
        return year == 0 && cs.wasNull() ? null : Year.of(year);
    }
}
