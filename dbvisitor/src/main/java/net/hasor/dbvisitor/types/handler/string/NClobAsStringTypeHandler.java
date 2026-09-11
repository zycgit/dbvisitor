/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.string;
import java.io.StringReader;
import java.sql.*;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 string 类型读写 jdbc NClob 数据。
 * @author Clinton Begin
 * @author 赵永春 (zyc@hasor.net)
 */
public class NClobAsStringTypeHandler extends AbstractTypeHandler<String> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, Integer jdbcType) throws SQLException {
        ps.setNClob(i, new StringReader(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        NClob clob = rs.getNClob(columnName);
        return toString(clob);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        NClob clob = rs.getNClob(columnIndex);
        return toString(clob);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        NClob clob = cs.getNClob(columnIndex);
        return toString(clob);
    }

    protected String toString(NClob clob) throws SQLException {
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
