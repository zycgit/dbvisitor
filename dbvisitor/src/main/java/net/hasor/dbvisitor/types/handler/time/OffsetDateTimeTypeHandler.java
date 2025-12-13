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
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 读写 {@link OffsetDateTime} 类型数据。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class OffsetDateTimeTypeHandler extends AbstractTypeHandler<OffsetDateTime> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OffsetDateTime parameter, Integer jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public OffsetDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return readOffsetDateTime(() -> rs.getObject(columnName, OffsetDateTime.class), () -> rs.getObject(columnName), () -> rs.getString(columnName));
    }

    @Override
    public OffsetDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return readOffsetDateTime(() -> rs.getObject(columnIndex, OffsetDateTime.class), () -> rs.getObject(columnIndex), () -> rs.getString(columnIndex));
    }

    @Override
    public OffsetDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return readOffsetDateTime(() -> cs.getObject(columnIndex, OffsetDateTime.class), () -> cs.getObject(columnIndex), () -> cs.getString(columnIndex));
    }

    private OffsetDateTime readOffsetDateTime(SqlSupplier<OffsetDateTime> typedReader, SqlSupplier<Object> rawReader, SqlSupplier<String> stringReader) throws SQLException {
        try {
            return typedReader.get();
        } catch (SQLException | AbstractMethodError primaryEx) {
            Object raw = readRaw(rawReader, primaryEx);
            if (raw == null) {
                String text = readString(stringReader, primaryEx);
                return parseOffsetDateTime(text, primaryEx);
            }
            if (raw instanceof OffsetDateTime) {
                return (OffsetDateTime) raw;
            }
            if (raw instanceof Timestamp) {
                return fromTimestamp((Timestamp) raw, primaryEx);
            }
            if (raw instanceof LocalDateTime) {
                return fromLocalDateTime((LocalDateTime) raw);
            }
            if (raw instanceof Date) {
                return fromInstant(((Date) raw).toInstant());
            }
            if (raw instanceof CharSequence) {
                return parseOffsetDateTime(raw.toString(), primaryEx);
            }
            return parseOffsetDateTime(raw.toString(), primaryEx);
        }
    }

    private Object readRaw(SqlSupplier<Object> rawReader, Throwable suppressed) throws SQLException {
        try {
            return rawReader.get();
        } catch (SQLException ex) {
            if (suppressed != null) {
                ex.addSuppressed(suppressed);
            }
            throw ex;
        }
    }

    private String readString(SqlSupplier<String> stringReader, Throwable suppressed) throws SQLException {
        try {
            return stringReader.get();
        } catch (SQLException ex) {
            if (suppressed != null) {
                ex.addSuppressed(suppressed);
            }
            throw ex;
        }
    }

    private OffsetDateTime fromTimestamp(Timestamp timestamp, Throwable suppressed) throws SQLException {
        if (timestamp == null) {
            return null;
        }
        try {
            return parseOffsetDateTime(timestamp.toString(), suppressed);
        } catch (SQLException ex) {
            if (suppressed != null) {
                ex.addSuppressed(suppressed);
            }
            return fromInstant(timestamp.toInstant());
        }
    }

    private OffsetDateTime fromLocalDateTime(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        return localDateTime.atZone(zoneId).toOffsetDateTime();
    }

    private OffsetDateTime fromInstant(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private OffsetDateTime parseOffsetDateTime(String text, Throwable suppressed) throws SQLException {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = normalizeOffset(trimmed.replace(' ', 'T'));
        try {
            return OffsetDateTime.parse(normalized, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException ex) {
            try {
                return OffsetDateTime.parse(normalized, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            } catch (DateTimeParseException ignored) {
                try {
                    LocalDateTime localCandidate = LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    return fromLocalDateTime(localCandidate);
                } catch (DateTimeParseException ignoredLocal) {
                    try {
                        return fromInstant(Instant.parse(normalized));
                    } catch (DateTimeParseException ignoredInstant) {
                        SQLException sqlException = new SQLException("unable to parse OffsetDateTime value: " + text, ex);
                        if (suppressed != null) {
                            sqlException.addSuppressed(suppressed);
                        }
                        throw sqlException;
                    }
                }
            }
        }
    }

    private String normalizeOffset(String text) {
        String trimmed = text.trim();
        if (trimmed.endsWith("Z")) {
            return trimmed;
        }
        int plusIndex = trimmed.lastIndexOf('+');
        int minusIndex = trimmed.lastIndexOf('-');
        int idx = Math.max(plusIndex, minusIndex);
        if (idx > 0) {
            String prefix = trimmed.substring(0, idx);
            String offset = trimmed.substring(idx);
            if (offset.length() == 3) {
                return prefix + offset + ":00";
            }
            if (offset.length() == 5 && offset.charAt(3) != ':') {
                return prefix + offset.substring(0, 3) + ':' + offset.substring(3);
            }
        }
        return trimmed;
    }

    @FunctionalInterface
    private interface SqlSupplier<T> {
        T get() throws SQLException;
    }
}