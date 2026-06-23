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
import java.time.LocalTime;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 读写 {@link LocalTime}  类型数据
 * @author 赵永春 (zyc@hasor.net)
 */
public class LocalTimeTypeHandler extends AbstractTypeHandler<LocalTime> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalTime parameter, Integer jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public LocalTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toLocalTime(rs.getTime(columnName));
    }

    @Override
    public LocalTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toLocalTime(rs.getTime(columnIndex));
    }

    @Override
    public LocalTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toLocalTime(cs.getTime(columnIndex));
    }

    private LocalTime toLocalTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalTime) {
            return (LocalTime) value;
        }
        if (value instanceof Time) {
            return ((Time) value).toLocalTime();
        }
        return LocalTime.parse(value.toString().trim());
    }
}
