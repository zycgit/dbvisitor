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
import net.hasor.cobble.reflect.TypeReference;
import net.hasor.dbvisitor.types.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The base {@link TypeHandler} for references a generic type.
 * <p>
 * Important: Since 3.5.0, This class never call the {@link ResultSet#wasNull()} and
 * {@link CallableStatement#wasNull()} method for handling the SQL {@code NULL} value.
 * In other words, {@code null} value handling should be performed on subclass.
 * </p>
 *
 * @author Clinton Begin
 * @author Simone Tripodi
 * @author Kzuki Shimizu
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractTypeHandler<T> extends TypeReference<T> implements TypeHandler<T> {
    @Override
    public void setParameter(PreparedStatement ps, int i, T parameter, Integer jdbcType) throws SQLException {
        if (parameter == null) {
            if (jdbcType == null) {
                throw new SQLException("JDBC requires that the JdbcType must be specified for all nullable parameters.");
            }
            try {
                ps.setNull(i, jdbcType);
            } catch (SQLException e) {
                throw new SQLException("Error setting null for parameter #" + i + " with JdbcType " + jdbcType + ", " + //
                        "Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property. Cause: " + e.getMessage(), e);
            }
        } else {
            try {
                setNonNullParameter(ps, i, parameter, jdbcType);
            } catch (Exception e) {
                throw new SQLException("Error setting non null for parameter #" + i + " with JdbcType " + jdbcType + "," +//
                        "Try setting a different JdbcType for this parameter or a different configuration property. Cause: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public T getResult(ResultSet rs, String columnName) throws SQLException {
        try {
            return getNullableResult(rs, columnName);
        } catch (Exception e) {
            throw new SQLException("Error attempting to get column '" + columnName + "' from result set.  Cause: " + e.getMessage(), e);
        }
    }

    @Override
    public T getResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            return getNullableResult(rs, columnIndex);
        } catch (Exception e) {
            throw new SQLException("Error attempting to get column #" + columnIndex + " from result set.  Cause: " + e.getMessage(), e);
        }
    }

    @Override
    public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            return getNullableResult(cs, columnIndex);
        } catch (Exception e) {
            throw new SQLException("Error attempting to get column #" + columnIndex + " from callable statement.  Cause: " + e.getMessage(), e);
        }
    }

    public abstract void setNonNullParameter(PreparedStatement ps, int i, T parameter, Integer jdbcType) throws SQLException;

    /** @param columnName Colunm name, when configuration <code>useColumnLabel</code> is <code>false</code> */
    public abstract T getNullableResult(ResultSet rs, String columnName) throws SQLException;

    public abstract T getNullableResult(ResultSet rs, int columnIndex) throws SQLException;

    public abstract T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException;
}