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
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * 使用 {@link java.time.YearMonth} 类型读写 jdbc {@link java.sql.Timestamp} 数据。缺失的时间信息使用 0 补充，月份/日期使用 1。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
public class SqlTimestampAsYearMonthTypeHandler extends AbstractTypeHandler<YearMonth> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, YearMonth parameter, Integer jdbcType) throws SQLException {
        LocalDateTime dateTime = LocalDateTime.of(parameter.getYear(), parameter.getMonth(), 1, 0, 0);
        ps.setTimestamp(i, Timestamp.valueOf(dateTime));
    }

    @Override
    public YearMonth getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return toYearMonth(timestamp);
    }

    @Override
    public YearMonth getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        return toYearMonth(timestamp);
    }

    @Override
    public YearMonth getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        return toYearMonth(timestamp);
    }

    protected YearMonth toYearMonth(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();
        return YearMonth.of(localDate.getYear(), localDate.getMonthValue());
    }
}
