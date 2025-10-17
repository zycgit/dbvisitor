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
package net.hasor.dbvisitor.types.handler.io;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link Reader} 类型读写 jdbc NString 数据。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class NStringAsReaderTypeHandler extends AbstractTypeHandler<Reader> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Reader parameter, Integer jdbcType) throws SQLException {
        try {
            ps.setNString(i, IOUtils.toString(parameter));
        } catch (IOException e) {
            throw new SQLException("Error reader to string for parameter #" + i + " with JdbcType " + jdbcType + ", Cause: " + e.getMessage(), e);
        }
    }

    @Override
    public Reader getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String string = rs.getNString(columnName);
        return string == null ? null : new StringReader(string);
    }

    @Override
    public Reader getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String string = rs.getNString(columnIndex);
        return string == null ? null : new StringReader(string);
    }

    @Override
    public Reader getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String string = cs.getNString(columnIndex);
        return string == null ? null : new StringReader(string);
    }
}