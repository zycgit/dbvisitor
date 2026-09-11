/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.time;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link java.time.Year} 类型读写 jdbc {@link java.sql.Timestamp} 数据。缺失的时间信息使用 0 补充，月份/日期使用 1。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class SqlTimestampAsYearTypeHandler extends AbstractTypeHandler<Year> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Year parameter, Integer jdbcType) throws SQLException {
        LocalDateTime dateTime = LocalDateTime.of(parameter.getValue(), Month.JANUARY, 1, 0, 0);
        ps.setTimestamp(i, Timestamp.valueOf(dateTime));
    }

    @Override
    public Year getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return toYear(timestamp);
    }

    @Override
    public Year getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        return toYear(timestamp);
    }

    @Override
    public Year getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        return toYear(timestamp);
    }

    protected Year toYear(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();
        return Year.of(localDate.getYear());
    }
}
