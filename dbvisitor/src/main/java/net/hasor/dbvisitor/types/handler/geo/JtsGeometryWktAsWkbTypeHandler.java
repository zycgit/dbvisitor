/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.geo;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 以 WKB 形式，读写 WKT 数据（数据库存储读使用 String）
 * @author 赵永春 (zyc@hasor.net)
 */
public class JtsGeometryWktAsWkbTypeHandler extends AbstractJtsGeometryTypeHandler<byte[]> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, byte[] parameter, Integer jdbcType) throws SQLException {
        ps.setString(i, toWKT(parameter));
    }

    @Override
    public byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toWKB(rs.getString(columnName));
    }

    @Override
    public byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toWKB(rs.getString(columnIndex));
    }

    @Override
    public byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toWKB(cs.getString(columnIndex));
    }
}
