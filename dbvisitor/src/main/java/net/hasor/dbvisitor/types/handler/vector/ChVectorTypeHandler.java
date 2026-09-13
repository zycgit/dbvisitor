/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.vector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.types.TypeHandler;

/** Maps a float vector to ClickHouse Array(Float32), using JDBC array binding. */
public class ChVectorTypeHandler implements TypeHandler<List<Float>> {
    @Override
    public void setParameter(PreparedStatement ps, int i, List<Float> parameter, Integer jdbcType) throws SQLException {
        if (parameter == null) {
            throw new SQLException("ClickHouse Array(Float32) does not support a null vector.");
        }
        Array array = ps.getConnection().createArrayOf("Float32", parameter.toArray(new Float[0]));
        try {
            ps.setArray(i, array);
        } finally {
            array.free();
        }
    }

    @Override
    public List<Float> getResult(ResultSet rs, String columnName) throws SQLException {
        return readVector(rs.getArray(columnName));
    }

    @Override
    public List<Float> getResult(ResultSet rs, int columnIndex) throws SQLException {
        return readVector(rs.getArray(columnIndex));
    }

    @Override
    public List<Float> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return readVector(cs.getArray(columnIndex));
    }

    private List<Float> readVector(Array array) throws SQLException {
        if (array == null) {
            return null;
        }

        try {
            Object values = array.getArray();
            int size = java.lang.reflect.Array.getLength(values);
            List<Float> vector = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                Object value = java.lang.reflect.Array.get(values, i);
                if (!(value instanceof Number)) {
                    throw new SQLException("ClickHouse vector elements must be numeric and non-null.");
                }
                vector.add(((Number) value).floatValue());
            }
            return vector;
        } finally {
            array.free();
        }
    }
}
