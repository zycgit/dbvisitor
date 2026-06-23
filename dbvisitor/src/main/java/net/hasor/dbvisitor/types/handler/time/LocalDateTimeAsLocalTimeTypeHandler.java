/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
