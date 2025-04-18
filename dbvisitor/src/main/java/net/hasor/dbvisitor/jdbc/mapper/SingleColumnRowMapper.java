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
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author 赵永春 (zyc@byshell.org)
 * @version 2014-05-23
 */
public class SingleColumnRowMapper<T> extends AbstractRowMapper<T> {
    private final Class<T> requiredType;

    /**
     * Create a new SingleColumnRowMapper.
     * @param requiredType the type that each result object is expected to match
     */
    public SingleColumnRowMapper(Class<T> requiredType) {
        this(requiredType, TypeHandlerRegistry.DEFAULT);
    }

    /**
     * Create a new SingleColumnRowMapper.
     * @param requiredType the type that each result object is expected to match
     */
    public SingleColumnRowMapper(Class<T> requiredType, TypeHandlerRegistry typeHandler) {
        super(typeHandler);
        this.requiredType = requiredType;
    }

    /** 将当前行的第一列的值转换为指定的类型。 */
    @Override
    public T mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();
        if (nrOfColumns != 1) {
            throw new SQLException("Incorrect column count: expected 1, actual " + nrOfColumns);
        }
        return (T) getResultSetValue(rs, 1, this.requiredType);
    }
}