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
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

import java.io.StringReader;
import java.sql.*;

/**
 * 使用 String 类型读写 jdbc Clob 数据。
 * @author Clinton Begin
 * @author 赵永春 (zyc@hasor.net)
 */
public class ClobAsStringTypeHandler extends AbstractTypeHandler<String> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, Integer jdbcType) throws SQLException {
        ps.setClob(i, new StringReader(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Clob clob = rs.getClob(columnName);
        return toString(clob);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Clob clob = rs.getClob(columnIndex);
        return toString(clob);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Clob clob = cs.getClob(columnIndex);
        return toString(clob);
    }

    protected String toString(Clob clob) throws SQLException {
        if (clob == null) {
            return null;
        }
        try {
            return clob.getSubString(1, (int) clob.length());
        } finally {
            clob.free();
        }
    }
}
