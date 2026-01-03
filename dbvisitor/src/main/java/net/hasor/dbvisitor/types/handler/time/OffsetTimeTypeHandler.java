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
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 读写 {@link OffsetTime} 类型数据。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class OffsetTimeTypeHandler extends AbstractTypeHandler<OffsetTime> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OffsetTime parameter, Integer jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public OffsetTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return readOffsetTime(() -> rs.getObject(columnName, OffsetTime.class), () -> rs.getObject(columnName), () -> rs.getString(columnName));
    }

    @Override
    public OffsetTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return readOffsetTime(() -> rs.getObject(columnIndex, OffsetTime.class), () -> rs.getObject(columnIndex), () -> rs.getString(columnIndex));
    }

    @Override
    public OffsetTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return readOffsetTime(() -> cs.getObject(columnIndex, OffsetTime.class), () -> cs.getObject(columnIndex), () -> cs.getString(columnIndex));
    }

    private OffsetTime readOffsetTime(SqlSupplier<OffsetTime> typedReader, SqlSupplier<Object> rawReader, SqlSupplier<String> stringReader) throws SQLException {
        try {
            return typedReader.get();
        } catch (SQLException | AbstractMethodError primaryEx) {
            Object raw = readRaw(rawReader, primaryEx);
            if (raw == null) {
                String text = readString(stringReader, primaryEx);
                return parseOffsetTime(text, primaryEx);
            }
            if (raw instanceof OffsetTime) {
                return (OffsetTime) raw;
            }
            if (raw instanceof OffsetDateTime) {
                return ((OffsetDateTime) raw).toOffsetTime();
            }
            if (raw instanceof Timestamp) {
                return fromTimestamp((Timestamp) raw, primaryEx);
            }
            if (raw instanceof Time) {
                return fromTimestamp(new Timestamp(((Time) raw).getTime()), primaryEx);
            }
            if (raw instanceof LocalTime) {
                return fromLocalTime((LocalTime) raw);
            }
            if (raw instanceof LocalDateTime) {
                return fromLocalDateTime((LocalDateTime) raw);
            }
            if (raw instanceof Date) {
                return fromInstant(((Date) raw).toInstant());
            }
            if (raw instanceof CharSequence) {
                return parseOffsetTime(raw.toString(), primaryEx);
            }
            return parseOffsetTime(raw.toString(), primaryEx);
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

    private OffsetTime fromTimestamp(Timestamp timestamp, Throwable suppressed) throws SQLException {
        if (timestamp == null) {
            return null;
        }
        try {
            return parseOffsetTime(timestamp.toString(), suppressed);
        } catch (SQLException ex) {
            if (suppressed != null) {
                ex.addSuppressed(suppressed);
            }
            return fromInstant(timestamp.toInstant());
        }
    }

    private OffsetTime fromLocalTime(LocalTime localTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZoneOffset offset = zoneId.getRules().getOffset(LocalDate.ofEpochDay(0).atTime(localTime));
        return localTime.atOffset(offset);
    }

    private OffsetTime fromLocalDateTime(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        return localDateTime.atZone(zoneId).toOffsetDateTime().toOffsetTime();
    }

    private OffsetTime fromInstant(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC).toOffsetTime();
    }

    private OffsetTime parseOffsetTime(String text, Throwable suppressed) throws SQLException {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = normalizeOffset(trimmed);
        try {
            return OffsetTime.parse(normalized, DateTimeFormatter.ISO_OFFSET_TIME);
        } catch (DateTimeParseException ex) {
            try {
                String dateTimeCandidate = ensureDatePart(normalized);
                return OffsetDateTime.parse(dateTimeCandidate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toOffsetTime();
            } catch (DateTimeParseException ignored) {
                try {
                    LocalTime localCandidate = LocalTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_TIME);
                    return fromLocalTime(localCandidate);
                } catch (DateTimeParseException ignoredLocal) {
                    SQLException sqlException = new SQLException("unable to parse OffsetTime value: " + text, ex);
                    if (suppressed != null) {
                        sqlException.addSuppressed(suppressed);
                    }
                    throw sqlException;
                }
            }
        }
    }

    private String ensureDatePart(String text) {
        if (text.indexOf('T') >= 0) {
            return normalizeOffset(text);
        }
        return "1970-01-01T" + text;
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