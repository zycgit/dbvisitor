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
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link LocalTime} 类型读写 jdbc {@link LocalDateTime} 数据。缺失的日期使用 0000-01-01 补充。
 * @author 赵永春 (zyc@hasor.net)
 */
public class LocalDateTimeAsLocalTimeTypeHandler extends AbstractTypeHandler<LocalTime> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalTime parameter, Integer jdbcType) throws SQLException {
        ps.setObject(i, LocalDateTime.of(LocalDate.of(0, 1, 1), parameter));
    }

    @Override
    public LocalTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toLocalTime(rs.getObject(columnName));
    }

    @Override
    public LocalTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toLocalTime(rs.getObject(columnIndex));
    }

    @Override
    public LocalTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toLocalTime(cs.getObject(columnIndex));
    }

    private LocalTime toLocalTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalTime) {
            return (LocalTime) value;
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalTime();
        }
        if (value instanceof Time) {
            return ((Time) value).toLocalTime();
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime().toLocalTime();
        }
        String text = value.toString().trim();
        try {
            return LocalTime.parse(text);
        } catch (DateTimeParseException e) {
            return Timestamp.valueOf(text).toLocalDateTime().toLocalTime();
        }
    }
}
