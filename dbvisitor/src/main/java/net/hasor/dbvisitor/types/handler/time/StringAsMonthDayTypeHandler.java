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
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link MonthDay} 类型读写 jdbc string 数据。数据格式为：MM-dd，例如: "01-03"、"12-03"
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class StringAsMonthDayTypeHandler extends AbstractTypeHandler<MonthDay> {
    private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()//
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)  //
            .appendLiteral('-')                   //
            .appendValue(ChronoField.DAY_OF_MONTH, 2)   //
            .toFormatter();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, MonthDay monthDay, Integer jdbcType) throws SQLException {
        String monthValue = String.valueOf(monthDay.getMonthValue());
        String dayValue = String.valueOf(monthDay.getDayOfMonth());
        if (monthValue.length() == 1) {
            monthValue = "0" + monthValue;
        }
        if (dayValue.length() == 1) {
            dayValue = "0" + dayValue;
        }
        ps.setString(i, monthValue + "-" + dayValue);
    }

    @Override
    public MonthDay getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return StringUtils.isBlank(value) ? null : MonthDay.parse(value, PARSER);
    }

    @Override
    public MonthDay getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return StringUtils.isBlank(value) ? null : MonthDay.parse(value, PARSER);
    }

    @Override
    public MonthDay getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return StringUtils.isBlank(value) ? null : MonthDay.parse(value, PARSER);
    }
}
