/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.custom;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

public class MyDateTypeHandler extends AbstractTypeHandler<String> {
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, Integer jdbcType) throws SQLException {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(parameter);
            ps.setTimestamp(i, new Timestamp(date.getTime()));
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return fmtDate(rs.getTimestamp(columnName));
    }

    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return fmtDate(rs.getTimestamp(columnIndex));
    }

    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return fmtDate(cs.getTimestamp(columnIndex));
    }

    private String fmtDate(Timestamp sqlTimestamp) {
        if (sqlTimestamp != null) {
            Date date = new Date(sqlTimestamp.getTime());
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        }
        return null;
    }
}
