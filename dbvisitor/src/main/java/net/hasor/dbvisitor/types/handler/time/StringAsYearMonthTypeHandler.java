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
import java.time.YearMonth;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link java.time.YearMonth} 类型读写 jdbc string 数据。格式为 yyyy-MM，例如：2007-12
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class StringAsYearMonthTypeHandler extends AbstractTypeHandler<YearMonth> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, YearMonth yearMonth, Integer jdbcType) throws SQLException {
        ps.setString(i, yearMonth.toString());
    }

    @Override
    public YearMonth getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return StringUtils.isBlank(value) ? null : YearMonth.parse(value);
    }

    @Override
    public YearMonth getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return StringUtils.isBlank(value) ? null : YearMonth.parse(value);
    }

    @Override
    public YearMonth getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return StringUtils.isBlank(value) ? null : YearMonth.parse(value);
    }
}
