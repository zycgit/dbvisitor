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
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 使用 bytes 包装类型读写 jdbc bytes 数据。
 * @author 赵永春 (zyc@hasor.net)
 */
public class BytesAsBytesWrapTypeHandler extends AbstractTypeHandler<Byte[]> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Byte[] parameter, Integer jdbcType) throws SQLException {
        ps.setBytes(i, convertToPrimitiveArray(parameter));
    }

    @Override
    public Byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getBytes(rs.getBytes(columnName));
    }

    @Override
    public Byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getBytes(rs.getBytes(columnIndex));
    }

    @Override
    public Byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getBytes(cs.getBytes(columnIndex));
    }

    private Byte[] getBytes(byte[] bytes) {
        return bytes == null ? null : convertToObjectArray(bytes);
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
