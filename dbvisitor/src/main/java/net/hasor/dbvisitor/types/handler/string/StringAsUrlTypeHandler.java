/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.string;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 读写 url 类型。
 * @author 赵永春 (zyc@hasor.net)
 */
public class StringAsUrlTypeHandler extends AbstractTypeHandler<URL> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, URL parameter, Integer jdbcType) throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public URL getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toUrl(rs.getString(columnName));
    }

    @Override
    public URL getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toUrl(rs.getString(columnIndex));
    }

    @Override
    public URL getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toUrl(cs.getString(columnIndex));
    }

    protected URL toUrl(String urlData) throws SQLException {
        try {
            return new URL(urlData);
        } catch (MalformedURLException e) {
            throw new SQLException(e);
        }
    }
}
