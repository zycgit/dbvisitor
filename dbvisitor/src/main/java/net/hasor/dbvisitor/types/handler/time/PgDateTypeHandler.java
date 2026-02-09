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
import java.time.format.DateTimeFormatter;
import net.hasor.dbvisitor.types.NoCache;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * PostgreSQL DATE 类型处理器，支持公元前日期（BC 后缀）
 * <p>PostgreSQL 支持使用 BC 后缀表示公元前日期，例如：'0100-01-01 BC' 表示公元前100年1月1日</p>
 * <p>Java {@link LocalDate} 使用 ISO 8601 年份表示法：</p>
 * <ul>
 *   <li>Year 1 = 公元1年 (1 AD)</li>
 *   <li>Year 0 = 公元前1年 (1 BC)</li>
 *   <li>Year -1 = 公元前2年 (2 BC)</li>
 *   <li>Year -99 = 公元前100年 (100 BC)</li>
 * </ul>
 * <p>转换规则：</p>
 * <ul>
 *   <li>Java Year ≤ 0 → PostgreSQL BC（年份取绝对值后加1）</li>
 *   <li>Java Year ≥ 1 → PostgreSQL AD（年份不变）</li>
 * </ul>
 * @author 赵永春 (zyc@hasor.net)
 * @version 2026-02-07
 */
@NoCache
public class PgDateTypeHandler extends AbstractTypeHandler<LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDate parameter, Integer jdbcType) throws SQLException {
        if (parameter.getYear() <= 0) {
            // 公元前日期：ISO 8601 Year 0 = 1 BC, Year -1 = 2 BC, Year -99 = 100 BC
            int bcYear = Math.abs(parameter.getYear()) + 1;
            String bcDate = String.format("%04d-%02d-%02d BC", bcYear, parameter.getMonthValue(), parameter.getDayOfMonth());
            // 使用 setObject 并指定 PostgreSQL DATE 类型
            ps.setObject(i, bcDate, java.sql.Types.DATE);
        } else {
            // 公元后日期：使用标准 java.sql.Date
            ps.setDate(i, java.sql.Date.valueOf(parameter));
        }
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null) {
            return null;
        }
        return parseDate(value);
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return parseDate(value);
    }

    @Override
    public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        if (value == null) {
            return null;
        }
        return parseDate(value);
    }

    /**
     * 解析 PostgreSQL DATE 字符串，支持 BC 后缀
     */
    private LocalDate parseDate(String value) {
        if (value.endsWith(" BC")) {
            // 公元前日期：100 BC = ISO 8601 Year -99
            String dateStr = value.substring(0, value.length() - 3).trim();
            LocalDate bcDate = LocalDate.parse(dateStr, FORMATTER);
            int isoYear = -(bcDate.getYear() - 1);
            return LocalDate.of(isoYear, bcDate.getMonth(), bcDate.getDayOfMonth());
        } else {
            // 公元后日期：标准解析
            return LocalDate.parse(value, FORMATTER);
        }
    }
}
