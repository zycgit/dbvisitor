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
import net.hasor.cobble.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractJsonTypeHandler<T> extends AbstractTypeHandler<T> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, Integer jdbcType) throws SQLException {
        ps.setString(i, toJson(parameter));
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final String json = rs.getString(columnName);
        return StringUtils.isBlank(json) && rs.wasNull() ? null : parse(json);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final String json = rs.getString(columnIndex);
        return StringUtils.isBlank(json) && rs.wasNull() ? null : parse(json);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        final String json = cs.getString(columnIndex);
        return StringUtils.isBlank(json) && cs.wasNull() ? null : parse(json);
    }

    @Override
    public abstract String toString();

    protected abstract T parse(String json);

    protected abstract String toJson(T obj);
}
