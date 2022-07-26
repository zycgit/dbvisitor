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
import net.hasor.cobble.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class BytesInputStreamTypeHandler extends AbstractTypeHandler<InputStream> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, InputStream parameter, Integer jdbcType) throws SQLException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(parameter, baos);
            ps.setBytes(i, baos.toByteArray());
        } catch (IOException e) {
            throw new SQLException("Error reader to byte[] for parameter #" + i + " with JdbcType " + jdbcType + ", Cause: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getNullableResult(ResultSet rs, String columnName) throws SQLException {
        byte[] bytes = rs.getBytes(columnName);
        return bytes == null ? null : new ByteArrayInputStream(bytes);
    }

    @Override
    public InputStream getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        byte[] bytes = rs.getBytes(columnIndex);
        return bytes == null ? null : new ByteArrayInputStream(bytes);
    }

    @Override
    public InputStream getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        byte[] bytes = cs.getBytes(columnIndex);
        return bytes == null ? null : new ByteArrayInputStream(bytes);
    }
}