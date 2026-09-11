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
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link ZonedDateTime} 类型读写 jdbc {@link OffsetDateTime} 数据。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class OffsetDateTimeAsZonedDateTimeTypeHandler extends AbstractTypeHandler<ZonedDateTime> {
    private static final OffsetDateTimeTypeHandler DELEGATE = new OffsetDateTimeTypeHandler();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ZonedDateTime parameter, Integer jdbcType) throws SQLException {
        ps.setObject(i, parameter.toOffsetDateTime());
    }

    @Override
    public ZonedDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        OffsetDateTime offsetDateTime = DELEGATE.getNullableResult(rs, columnName);
        return (offsetDateTime == null) ? null : offsetDateTime.toZonedDateTime();
    }

    @Override
    public ZonedDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        OffsetDateTime offsetDateTime = DELEGATE.getNullableResult(rs, columnIndex);
        return (offsetDateTime == null) ? null : offsetDateTime.toZonedDateTime();
    }

    @Override
    public ZonedDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        OffsetDateTime offsetDateTime = DELEGATE.getNullableResult(cs, columnIndex);
        return (offsetDateTime == null) ? null : offsetDateTime.toZonedDateTime();
    }
}
