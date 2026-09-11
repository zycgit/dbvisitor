/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.time;
import java.sql.*;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用时区 {@link OffsetTime} 类型读写 jdbc {@link java.sql.Timestamp} 数据。时区的写入和读取会转换为 UTC。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class SqlTimestampAsUTCOffsetTimeTypeHandler extends AbstractTypeHandler<OffsetTime> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OffsetTime parameter, Integer jdbcType) throws SQLException {
        ZonedDateTime zonedDateTime = parameter.atDate(LocalDate.ofEpochDay(0)).atZoneSameInstant(ZoneOffset.UTC);
        Timestamp timestamp = Timestamp.from(zonedDateTime.toInstant());
        ps.setTimestamp(i, timestamp);
    }

    @Override
    public OffsetTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return (timestamp == null) ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC).toOffsetTime();
    }

    @Override
    public OffsetTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        return (timestamp == null) ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC).toOffsetTime();
    }

    @Override
    public OffsetTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        return (timestamp == null) ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC).toOffsetTime();
    }
}
