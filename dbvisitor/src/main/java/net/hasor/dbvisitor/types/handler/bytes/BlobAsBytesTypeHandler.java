/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.bytes;
import java.io.ByteArrayInputStream;
import java.sql.*;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 bytes 类型读写 jdbc blob 数据。
 * @author 赵永春 (zyc@hasor.net)
 */
public class BlobAsBytesTypeHandler extends AbstractTypeHandler<byte[]> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, byte[] parameter, Integer jdbcType) throws SQLException {
        ps.setBlob(i, new ByteArrayInputStream(parameter));
    }

    @Override
    public byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toBytes(rs.getBlob(columnName));
    }

    @Override
    public byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toBytes(rs.getBlob(columnIndex));
    }

    @Override
    public byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toBytes(cs.getBlob(columnIndex));
    }

    private byte[] toBytes(Blob blob) throws SQLException {
        if (blob == null) {
            return null;
        }
        try {
            return blob.getBytes(1, (int) blob.length());
        } finally {
            blob.free();
        }
    }
}
