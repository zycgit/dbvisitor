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