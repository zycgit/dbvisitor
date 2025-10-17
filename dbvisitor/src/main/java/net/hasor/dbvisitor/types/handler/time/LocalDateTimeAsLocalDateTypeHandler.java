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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link LocalDateTime} 类型读写 jdbc {@link java.time.LocalDate} 数据。缺失的时间信息使用 00:00 补充。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class LocalDateTimeAsLocalDateTypeHandler extends AbstractTypeHandler<LocalDate> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDate parameter, Integer jdbcType) throws SQLException {
        ps.setObject(i, LocalDateTime.of(parameter, LocalTime.of(0, 0, 0, 0)));
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
        LocalDateTime dateTime = rs.getObject(columnName, LocalDateTime.class);
        return (dateTime == null) ? null : dateTime.toLocalDate();
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        LocalDateTime dateTime = rs.getObject(columnIndex, LocalDateTime.class);
        return (dateTime == null) ? null : dateTime.toLocalDate();
    }

    @Override
    public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        LocalDateTime dateTime = cs.getObject(columnIndex, LocalDateTime.class);
        return (dateTime == null) ? null : dateTime.toLocalDate();
    }
}