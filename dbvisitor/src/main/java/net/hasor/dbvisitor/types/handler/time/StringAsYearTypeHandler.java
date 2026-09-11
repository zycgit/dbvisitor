/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.time;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link java.time.Year} 类型读写 jdbc string 数据。格式为 yyyy，数值范围 0000 to 9999
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class StringAsYearTypeHandler extends AbstractTypeHandler<Year> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Year year, Integer jdbcType) throws SQLException {
        ps.setString(i, String.valueOf(year.getValue()));
    }

    @Override
    public Year getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String year = rs.getString(columnName);
        return StringUtils.isBlank(year) ? null : Year.parse(year);
    }

    @Override
    public Year getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String year = rs.getString(columnIndex);
        return StringUtils.isBlank(year) ? null : Year.parse(year);
    }

    @Override
    public Year getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String year = cs.getString(columnIndex);
        return StringUtils.isBlank(year) ? null : Year.parse(year);
    }
}
