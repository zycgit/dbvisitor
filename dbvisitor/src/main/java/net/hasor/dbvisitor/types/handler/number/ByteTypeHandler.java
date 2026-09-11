/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.number;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 读写 byte 数据。
 * @author Clinton Begin
 * @author 赵永春 (zyc@hasor.net)
 */
public class ByteTypeHandler extends AbstractTypeHandler<Byte> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Byte parameter, Integer jdbcType) throws SQLException {
        ps.setByte(i, parameter);
    }

    @Override
    public Byte getNullableResult(ResultSet rs, String columnName) throws SQLException {
        byte result = rs.getByte(columnName);
        return result == 0 && rs.wasNull() ? null : result;
    }

    @Override
    public Byte getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        byte result = rs.getByte(columnIndex);
        return result == 0 && rs.wasNull() ? null : result;
    }

    @Override
    public Byte getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        byte result = cs.getByte(columnIndex);
        return result == 0 && cs.wasNull() ? null : result;
    }
}
