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
package net.hasor.dbvisitor.types.handler;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.MonthDay;

/**
 * 使用 {@link java.time.MonthDay} 类型读写 jdbc int 数据。格式为 MMdd，例如：523，1123
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
public class IntegerAsMonthDayTypeHandler extends AbstractTypeHandler<MonthDay> {
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
        ps.setInt(i, Integer.parseInt(monthValue + dayValue));
    }

    @Override
    public MonthDay getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int monthDay = rs.getInt(columnName);
        return monthDay == 0 && rs.wasNull() ? null : parseMonthDay(monthDay);
    }

    @Override
    public MonthDay getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int monthDay = rs.getInt(columnIndex);
        return monthDay == 0 && rs.wasNull() ? null : parseMonthDay(monthDay);
    }

    @Override
    public MonthDay getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int monthDay = cs.getInt(columnIndex);
        return monthDay == 0 && cs.wasNull() ? null : parseMonthDay(monthDay);
    }

    protected MonthDay parseMonthDay(int monthDay) throws SQLException {
        String mdStr = String.valueOf(monthDay);
        if (mdStr.length() == 3) {
            mdStr = "0" + mdStr;
        }
        if (mdStr.length() != 4) {
            throw new SQLException("JDBC requires that the monthDay value must be 4 Numbers");
        }
        int year = Integer.parseInt(mdStr.substring(0, 2));
        int month = Integer.parseInt(mdStr.substring(2));
        return MonthDay.of(year, month);
    }
}
