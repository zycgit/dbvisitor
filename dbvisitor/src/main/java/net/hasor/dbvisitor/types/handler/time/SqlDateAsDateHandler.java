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
import java.util.Date;

/**
 * 使用 {@link java.util.Date} 类型读写 jdbc {@link java.sql.Date} 数据。
 * @author Clinton Begin
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlDateAsDateHandler extends AbstractTypeHandler<Date> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, Integer jdbcType) throws SQLException {
        ps.setDate(i, new java.sql.Date(parameter.getTime()));
    }

    @Override
    public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
        java.sql.Date sqlDate = rs.getDate(columnName);
        if (sqlDate != null) {
            return new Date(sqlDate.getTime());
        }
        return null;
    }

    @Override
    public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        java.sql.Date sqlDate = rs.getDate(columnIndex);
        if (sqlDate != null) {
            return new Date(sqlDate.getTime());
        }
        return null;
    }

    @Override
    public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        java.sql.Date sqlDate = cs.getDate(columnIndex);
        if (sqlDate != null) {
            return new Date(sqlDate.getTime());
        }
        return null;
    }
}