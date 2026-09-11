/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.mapper;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Objects;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.types.TypeHandler;

/**
 * @author 赵永春 (zyc@byshell.org)
 * @version 2014-05-23
 */
public class TypeHandlerColumnRowMapper<T> implements RowMapper<T> {
    private final TypeHandler<?> requiredHandler;

    /**
     * Create a new TypeHandlerColumnRowMapper.
     * @param requiredType the type that each result object is expected to match
     */
    public TypeHandlerColumnRowMapper(TypeHandler<?> requiredType) {
        this.requiredHandler = Objects.requireNonNull(requiredType, "TypeHandler must not be null");
    }

    /** 将当前行的第一列的值转换为指定的类型。 */
    @Override
    public T mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();
        if (nrOfColumns != 1) {
            throw new SQLException("Incorrect column count: expected 1, actual " + nrOfColumns);
        }
        return (T) this.requiredHandler.getResult(rs, 1);
    }
}
