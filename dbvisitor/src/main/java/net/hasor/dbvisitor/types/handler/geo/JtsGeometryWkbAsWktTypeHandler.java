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
import net.hasor.cobble.StringUtils;

/**
 * 以 WKT 形式，读写 WKB 数据（数据库存储读使用 bytes）
 * @author 赵永春 (zyc@hasor.net)
 */
public class JtsGeometryWkbAsWktTypeHandler extends AbstractJtsGeometryTypeHandler<String> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, Integer jdbcType) throws SQLException {
        if (StringUtils.isBlank(parameter)) {
            ps.setBytes(i, null);
        } else {
            ps.setBytes(i, toWKB(parameter));
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toWKT(rs.getBytes(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toWKT(rs.getBytes(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toWKT(cs.getBytes(columnIndex));
    }
}
