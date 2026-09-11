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
 * 使用 bytes 包装类型读写 jdbc blob 数据。
 * @author 赵永春 (zyc@hasor.net)
 */
public class BlobAsBytesWrapTypeHandler extends AbstractTypeHandler<Byte[]> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Byte[] parameter, Integer jdbcType) throws SQLException {
        ps.setBlob(i, new ByteArrayInputStream(convertToPrimitiveArray(parameter)));
    }

    @Override
    public Byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getBytes(rs.getBlob(columnName));
    }

    @Override
    public Byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getBytes(rs.getBlob(columnIndex));
    }

    @Override
    public Byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getBytes(cs.getBlob(columnIndex));
    }

    private Byte[] getBytes(Blob blob) throws SQLException {
        if (blob == null) {
            return null;
        }
        try {
            return convertToObjectArray(blob.getBytes(1, (int) blob.length()));
        } finally {
            blob.free();
        }
    }

    protected byte[] convertToPrimitiveArray(Byte[] objects) {
        final byte[] bytes = new byte[objects.length];
        for (int i = 0; i < objects.length; i++) {
            bytes[i] = objects[i];
        }
        return bytes;
    }

    protected Byte[] convertToObjectArray(byte[] bytes) {
        final Byte[] objects = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            objects[i] = bytes[i];
        }
        return objects;
    }
}
