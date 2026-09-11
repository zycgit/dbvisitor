/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.string;
import java.sql.*;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 string 类型读写 jdbc {@link java.sql.SQLXML} 数据。
 * @author Iwao AVE!
 * @author 赵永春 (zyc@hasor.net)
 * @since 3.5.0
 */
public class SqlXmlTypeHandler extends AbstractTypeHandler<String> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, Integer jdbcType) throws SQLException {
        SQLXML sqlxml = ps.getConnection().createSQLXML();
        try {
            sqlxml.setString(parameter);
            ps.setSQLXML(i, sqlxml);
        } finally {
            sqlxml.free();
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return sqlXmlToStream(rs.getSQLXML(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return sqlXmlToStream(rs.getSQLXML(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return sqlXmlToStream(cs.getSQLXML(columnIndex));
    }

    protected String sqlXmlToStream(SQLXML sqlxml) throws SQLException {
        if (sqlxml == null) {
            return null;
        }
        try {
            return sqlxml.getString();
        } finally {
            sqlxml.free();
        }
    }
}
