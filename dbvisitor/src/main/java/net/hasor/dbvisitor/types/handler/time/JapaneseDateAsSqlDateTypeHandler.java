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
import java.time.chrono.JapaneseDate;

/**
 * 使用 {@link JapaneseDate} 类型读写 jdbc {@link java.sql.Date} 数据。
 * @author Kazuki Shimizu
 * @author 赵永春 (zyc@hasor.net)
 * @since 3.4.5
 */
public class JapaneseDateAsSqlDateTypeHandler extends AbstractTypeHandler<JapaneseDate> {
    public static JapaneseDate toJapaneseDate(Date date) {
        if (date != null) {
            return JapaneseDate.from(date.toLocalDate());
        }
        return null;
    }

    public static JapaneseDate toJapaneseDate(java.util.Date date) {
        if (date != null) {
            return toJapaneseDate(new Date(date.getTime()));
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JapaneseDate parameter, Integer jdbcType) throws SQLException {
        ps.setDate(i, Date.valueOf(LocalDate.ofEpochDay(parameter.toEpochDay())));
    }

    @Override
    public JapaneseDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Date date = rs.getDate(columnName);
        return toJapaneseDate(date);
    }

    @Override
    public JapaneseDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Date date = rs.getDate(columnIndex);
        return toJapaneseDate(date);
    }

    @Override
    public JapaneseDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Date date = cs.getDate(columnIndex);
        return toJapaneseDate(date);
    }
}
