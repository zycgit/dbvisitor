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
import net.hasor.cobble.NumberUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link Month} 类型读写 jdbc string 数据。可以是数字形式 1～12，可以是 {@link Month} 枚举所表示的月名
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class StringAsMonthTypeHandler extends AbstractTypeHandler<Month> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Month month, Integer jdbcType) throws SQLException {
        ps.setString(i, month.name().toUpperCase());
    }

    @Override
    public Month getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String month = rs.getString(columnName);
        if (StringUtils.isBlank(month)) {
            return null;
        }
        month = month.trim().toUpperCase();
        return NumberUtils.isNumber(month) ? Month.of(Integer.parseInt(month)) : Month.valueOf(month);
    }

    @Override
    public Month getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String month = rs.getString(columnIndex);
        if (StringUtils.isBlank(month)) {
            return null;
        }
        month = month.trim().toUpperCase();
        return NumberUtils.isNumber(month) ? Month.of(Integer.parseInt(month)) : Month.valueOf(month);
    }

    @Override
    public Month getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String month = cs.getString(columnIndex);
        if (StringUtils.isBlank(month)) {
            return null;
        }
        month = month.trim().toUpperCase();
        return NumberUtils.isNumber(month) ? Month.of(Integer.parseInt(month)) : Month.valueOf(month);
    }
}
