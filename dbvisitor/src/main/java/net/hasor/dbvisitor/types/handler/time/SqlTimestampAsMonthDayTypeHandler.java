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
import java.time.MonthDay;

/**
 * 使用 {@link MonthDay} 类型读写 jdbc {@link java.sql.Timestamp} 数据。缺失的时间信息使用 0 补充。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class SqlTimestampAsMonthDayTypeHandler extends AbstractTypeHandler<MonthDay> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, MonthDay parameter, Integer jdbcType) throws SQLException {
        LocalDateTime dateTime = LocalDateTime.of(0, parameter.getMonth(), parameter.getDayOfMonth(), 0, 0);
        ps.setTimestamp(i, Timestamp.valueOf(dateTime));
    }

    @Override
    public MonthDay getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return toMonthDay(timestamp);
    }

    @Override
    public MonthDay getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        return toMonthDay(timestamp);
    }

    @Override
    public MonthDay getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        return toMonthDay(timestamp);
    }

    protected MonthDay toMonthDay(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();
        return MonthDay.of(localDate.getMonth(), localDate.getDayOfMonth());
    }
}
