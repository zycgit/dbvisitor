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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;

/**
 * 使用 {@link java.time.YearMonth} 类型。读写一个数字末尾 2 位表示月份，其余表示年份。不足 2 位数的按照月份处理。如果为 0 表示 0000-01
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class IntegerAsYearMonthTypeHandler extends AbstractTypeHandler<YearMonth> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, YearMonth yearMonth, Integer jdbcType) throws SQLException {
        String monthValue = String.valueOf(yearMonth.getMonthValue());
        if (monthValue.length() == 1) {
            monthValue = "0" + monthValue;
        }
        ps.setInt(i, Integer.parseInt(yearMonth.getYear() + monthValue));
    }

    @Override
    public YearMonth getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int yearMonth = rs.getInt(columnName);
        return yearMonth == 0 && rs.wasNull() ? null : parseYearMonth(yearMonth);
    }

    @Override
    public YearMonth getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int yearMonth = rs.getInt(columnIndex);
        return yearMonth == 0 && rs.wasNull() ? null : parseYearMonth(yearMonth);
    }

    @Override
    public YearMonth getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int yearMonth = cs.getInt(columnIndex);
        return yearMonth == 0 && cs.wasNull() ? null : parseYearMonth(yearMonth);
    }

    protected static YearMonth parseYearMonth(int yearMonth) {
        if (yearMonth == 0) {
            return YearMonth.of(0, 1);
        }

        if (yearMonth <= 99) { // length 2 as month.
            return YearMonth.of(0, yearMonth);
        }

        String ymStr = String.valueOf(yearMonth);
        int ymStrLen = ymStr.length();

        int year = Integer.parseInt(ymStr.substring(0, ymStrLen - 2));
        int month = Integer.parseInt(ymStr.substring(ymStrLen - 2));
        return YearMonth.of(year, month);
    }
}
