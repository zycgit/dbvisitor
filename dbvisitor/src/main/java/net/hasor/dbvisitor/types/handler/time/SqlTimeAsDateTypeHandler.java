/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.time;
import java.sql.*;
import java.util.Date;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link java.util.Date} 类型读写 jdbc {@link java.sql.Time} 数据。
 * @author Clinton Begin
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlTimeAsDateTypeHandler extends AbstractTypeHandler<Date> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, Integer jdbcType) throws SQLException {
        ps.setTime(i, new Time(parameter.getTime()));
    }

    @Override
    public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Time sqlTime = rs.getTime(columnName);
        if (sqlTime != null) {
            return new Date(sqlTime.getTime());
        }
        return null;
    }

    @Override
    public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Time sqlTime = rs.getTime(columnIndex);
        if (sqlTime != null) {
            return new Date(sqlTime.getTime());
        }
        return null;
    }

    @Override
    public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Time sqlTime = cs.getTime(columnIndex);
        if (sqlTime != null) {
            return new Date(sqlTime.getTime());
        }
        return null;
    }
}
