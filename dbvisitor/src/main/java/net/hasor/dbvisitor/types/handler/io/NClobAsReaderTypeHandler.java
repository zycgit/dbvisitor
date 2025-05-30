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
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

import java.io.Reader;
import java.sql.*;

/**
 * 使用 {@link Reader} 类型读写 jdbc NClob 数据。
 * @author Kazuki Shimizu
 * @author 赵永春 (zyc@hasor.net)
 * @since 3.4.0
 */
public class NClobAsReaderTypeHandler extends AbstractTypeHandler<Reader> {
    /**
     * Set a {@link Reader} into {@link PreparedStatement}.
     * @see PreparedStatement#setClob(int, Reader)
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Reader parameter, Integer jdbcType) throws SQLException {
        ps.setNClob(i, parameter);
    }

    /**
     * Get a {@link Reader} that corresponds to a specified column name from {@link ResultSet}.
     * @see ResultSet#getClob(String)
     */
    @Override
    public Reader getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toReader(rs.getNClob(columnName));
    }

    /**
     * Get a {@link Reader} that corresponds to a specified column index from {@link ResultSet}.
     * @see ResultSet#getClob(int)
     */
    @Override
    public Reader getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toReader(rs.getNClob(columnIndex));
    }

    /**
     * Get a {@link Reader} that corresponds to a specified column index from {@link CallableStatement}.
     * @see CallableStatement#getClob(int)
     */
    @Override
    public Reader getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toReader(cs.getNClob(columnIndex));
    }

    protected Reader toReader(NClob clob) throws SQLException {
        if (clob == null) {
            return null;
        } else {
            return clob.getCharacterStream();
        }
    }
}
