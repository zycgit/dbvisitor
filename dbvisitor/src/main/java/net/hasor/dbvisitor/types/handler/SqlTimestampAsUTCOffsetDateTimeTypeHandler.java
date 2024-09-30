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
package net.hasor.dbvisitor.types.handler;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * 使用时区 {@link OffsetDateTime} 类型读写 jdbc {@link java.sql.Timestamp} 数据。时区的写入和读取会转换为 UTC。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
public class SqlTimestampAsUTCOffsetDateTimeTypeHandler extends AbstractTypeHandler<OffsetDateTime> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OffsetDateTime parameter, Integer jdbcType) throws SQLException {
        ZonedDateTime zonedDateTime = parameter.atZoneSameInstant(ZoneOffset.UTC);
        Timestamp timestamp = Timestamp.from(zonedDateTime.toInstant());
        ps.setTimestamp(i, timestamp);
    }

    @Override
    public OffsetDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return (timestamp == null) ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }

    @Override
    public OffsetDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        return (timestamp == null) ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }

    @Override
    public OffsetDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        return (timestamp == null) ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}