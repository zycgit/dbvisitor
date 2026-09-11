/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.string;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 读写 URI 类型。
 * @author 赵永春 (zyc@hasor.net)
 */
public class StringAsUriTypeHandler extends AbstractTypeHandler<URI> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, URI parameter, Integer jdbcType) throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public URI getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toUrl(rs.getString(columnName));
    }

    @Override
    public URI getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toUrl(rs.getString(columnIndex));
    }

    @Override
    public URI getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toUrl(cs.getString(columnIndex));
    }

    protected URI toUrl(String urlData) throws SQLException {
        try {
            return new URI(urlData);
        } catch (URISyntaxException e) {
            throw new SQLException(e);
        }
    }
}
