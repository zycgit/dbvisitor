/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.mapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
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
        return this.handlerRegistry.getResultSetTypeHandler(rs, columnIndex, null).getResult(rs, columnIndex);
    }

    /** 获取列的值 */
    protected Object getResultSetValue(ResultSet rs, int columnIndex, Class<?> targetType) throws SQLException {
        TypeHandler<?> typeHandler = this.handlerRegistry.getResultSetTypeHandler(rs, columnIndex, targetType);
        return typeHandler.getResult(rs, columnIndex);
    }
}
