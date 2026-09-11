/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.io;
import java.io.InputStream;
import java.sql.*;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link InputStream} 类型读写 jdbc blob 数据。
 * @author Kazuki Shimizu
 * @author 赵永春 (zyc@hasor.net)
 * @since 3.4.0
 */
public class BlobAsInputStreamTypeHandler extends AbstractTypeHandler<InputStream> {
    /**
     * Set an {@link InputStream} into {@link PreparedStatement}.
     * @see PreparedStatement#setBlob(int, InputStream)
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, InputStream parameter, Integer jdbcType) throws SQLException {
        ps.setBlob(i, parameter);
    }

    /**
     * Get an {@link InputStream} that corresponds to a specified column name from {@link ResultSet}.
     * @see ResultSet#getBlob(String)
     */
    @Override
    public InputStream getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toInputStream(rs.getBlob(columnName));
    }

    /**
     * Get an {@link InputStream} that corresponds to a specified column index from {@link ResultSet}.
     * @see ResultSet#getBlob(int)
     */
    @Override
    public InputStream getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toInputStream(rs.getBlob(columnIndex));
    }

    /**
     * Get an {@link InputStream} that corresponds to a specified column index from {@link CallableStatement}.
     * @see CallableStatement#getBlob(int)
     */
    @Override
    public InputStream getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toInputStream(cs.getBlob(columnIndex));
    }

    protected InputStream toInputStream(Blob blob) throws SQLException {
        if (blob == null) {
            return null;
        } else {
            return blob.getBinaryStream();
        }
    }
}
