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
import java.time.YearMonth;

/**
 * 使用 {@link java.time.YearMonth} 类型读写 jdbc string 数据。格式为 uuuuMM，例如：201608
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
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

    protected YearMonth parseYearMonth(int yearMonth) throws SQLException {
        String ymStr = String.valueOf(yearMonth);
        if (ymStr.length() != 6) {
            throw new SQLException("JDBC requires that the yearMonth value must be 6 Numbers");
        }
        int year = Integer.parseInt(ymStr.substring(0, 4));
        int month = Integer.parseInt(ymStr.substring(4));
        return YearMonth.of(year, month);
    }
}
