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
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用儒略日数（Julian Day Number）作为底层存储格式来处理 {@link LocalDate}，包括公元前日期。
 * <p>儒略日数是天文学中常用的日期表示方法，从公元前4713年1月1日开始连续计数。</p>
 * <p>这种方式避免了不同历法系统（格里高利历、儒略历）和年份表示法（ISO 8601 vs 传统纪年）之间的转换问题。</p>
 * <p>数据库存储：BIGINT 类型</p>
 * <p>适用场景：需要处理公元前日期，或跨数据库系统保证日期一致性</p>
 * @author 赵永春 (zyc@hasor.net)
 * @version 2026-02-07
 */
public class JulianDayTypeHandler extends AbstractTypeHandler<LocalDate> {

    /**
     * 将 LocalDate 转换为儒略日数
     * <p>算法来源：Richards (2012) 简化版</p>
     */
    private static long toJulianDay(LocalDate date) {
        int y = date.getYear();
        int m = date.getMonthValue();
        int d = date.getDayOfMonth();

        // Richards 算法
        int a = (14 - m) / 12;
        int y2 = y + 4800 - a;
        int m2 = m + 12 * a - 3;

        return d + (153 * m2 + 2) / 5 + 365 * y2 + y2 / 4 - y2 / 100 + y2 / 400 - 32045;
    }

    /**
     * 将儒略日数转换为 LocalDate
     * <p>算法来源：Richards (2012) 逆向算法</p>
     */
    private static LocalDate fromJulianDay(long julianDay) {
        // Richards 逆向算法
        long a = julianDay + 32044;
        long b = (4 * a + 3) / 146097;
        long c = a - (146097 * b) / 4;

        long d = (4 * c + 3) / 1461;
        long e = c - (1461 * d) / 4;
        long m = (5 * e + 2) / 153;

        int day = (int) (e - (153 * m + 2) / 5 + 1);
        int month = (int) (m + 3 - 12 * (m / 10));
        int year = (int) (100 * b + d - 4800 + m / 10);

        return LocalDate.of(year, month, day);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDate parameter, Integer jdbcType) throws SQLException {
        ps.setLong(i, toJulianDay(parameter));
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
        long julianDay = rs.getLong(columnName);
        return rs.wasNull() ? null : fromJulianDay(julianDay);
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        long julianDay = rs.getLong(columnIndex);
        return rs.wasNull() ? null : fromJulianDay(julianDay);
    }

    @Override
    public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        long julianDay = cs.getLong(columnIndex);
        return cs.wasNull() ? null : fromJulianDay(julianDay);
    }
}
