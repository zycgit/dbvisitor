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