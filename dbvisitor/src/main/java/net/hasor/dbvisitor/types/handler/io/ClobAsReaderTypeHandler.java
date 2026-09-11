/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.io;
import java.io.Reader;
import java.sql.*;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link Reader} 类型读写 jdbc Clob 数据。
 * @author Kazuki Shimizu
 * @author 赵永春 (zyc@hasor.net)
 * @since 3.4.0
 */
public class ClobAsReaderTypeHandler extends AbstractTypeHandler<Reader> {
    /**
     * Set a {@link Reader} into {@link PreparedStatement}.
     * @see PreparedStatement#setClob(int, Reader)
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Reader parameter, Integer jdbcType) throws SQLException {
        ps.setClob(i, parameter);
    }

    /**
     * Get a {@link Reader} that corresponds to a specified column name from {@link ResultSet}.
     * @see ResultSet#getClob(String)
     */
    @Override
    public Reader getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toReader(rs.getClob(columnName));
    }

    /**
     * Get a {@link Reader} that corresponds to a specified column index from {@link ResultSet}.
     * @see ResultSet#getClob(int)
     */
    @Override
    public Reader getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toReader(rs.getClob(columnIndex));
    }

    /**
     * Get a {@link Reader} that corresponds to a specified column index from {@link CallableStatement}.
     * @see CallableStatement#getClob(int)
     */
    @Override
    public Reader getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toReader(cs.getClob(columnIndex));
    }

    protected Reader toReader(Clob clob) throws SQLException {
        if (clob == null) {
            return null;
        } else {
            return clob.getCharacterStream();
        }
    }
}
