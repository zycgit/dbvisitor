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
package net.hasor.dbvisitor.jdbc.mapper;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2014-05-23
 */
public abstract class AbstractRowMapper<T> implements RowMapper<T> {
    private final TypeHandlerRegistry handlerRegistry;

    public AbstractRowMapper() {
        this(TypeHandlerRegistry.DEFAULT);
    }

    public AbstractRowMapper(TypeHandlerRegistry typeHandler) {
        this.handlerRegistry = Objects.requireNonNull(typeHandler, "typeHandler is null.");
    }

    public TypeHandlerRegistry getHandlerRegistry() {
        return this.handlerRegistry;
    }

    /** 获取列的值 */
    protected Object getResultSetValue(ResultSet rs, int columnIndex) throws SQLException {
        return getResultSetTypeHandler(rs, columnIndex, null).getResult(rs, columnIndex);
    }

    /** 获取列的值 */
    protected Object getResultSetValue(ResultSet rs, int columnIndex, Class<?> targetType) throws SQLException {
        TypeHandler<?> typeHandler = getResultSetTypeHandler(rs, columnIndex, targetType);
        return typeHandler.getResult(rs, columnIndex);
    }

    /** 获取读取列用到的那个 TypeHandler */
    public TypeHandler<?> getResultSetTypeHandler(ResultSet rs, int columnIndex, Class<?> targetType) throws SQLException {
        int jdbcType = rs.getMetaData().getColumnType(columnIndex);
        String columnTypeName = rs.getMetaData().getColumnTypeName(columnIndex);
        String columnClassName = rs.getMetaData().getColumnClassName(columnIndex);

        if ("YEAR".equalsIgnoreCase(columnTypeName)) {
            // TODO with mysql `YEAR` type, columnType is DATE. but getDate() throw Long cast Date failed.
            jdbcType = JDBCType.INTEGER.getVendorTypeNumber();
        } else if (StringUtils.isNotBlank(columnClassName) && columnClassName.startsWith("oracle.")) {
            // TODO with oracle columnClassName is specifically customizes standard types, it specializes process.
            jdbcType = TypeHandlerRegistry.toSqlType(columnClassName);
            if (targetType != null) {
                return this.handlerRegistry.getTypeHandler(targetType, jdbcType);
            } else {
                return this.handlerRegistry.getTypeHandler(jdbcType);
            }
        }

        Class<?> columnTypeClass = targetType;
        if (columnTypeClass == null) {
            try {
                columnTypeClass = ResourcesUtils.classForName(columnClassName);
            } catch (ClassNotFoundException e) {
                /**/
            }
        }

        if (this.handlerRegistry.hasTypeHandler(columnTypeClass, jdbcType)) {
            return this.handlerRegistry.getTypeHandler(columnTypeClass, jdbcType);
        } else if (this.handlerRegistry.hasTypeHandler(columnTypeClass)) {
            return this.handlerRegistry.getTypeHandler(columnTypeClass);
        } else {
            TypeHandler<?> typeHandler = this.handlerRegistry.getTypeHandler(columnTypeClass, jdbcType);
            if (typeHandler == null) {
                String message = "jdbcType=" + jdbcType + " ,columnTypeClass=" + columnTypeClass;
                throw new SQLException("no typeHandler is matched to any available " + message);
            }
            return typeHandler;
        }
    }
}
