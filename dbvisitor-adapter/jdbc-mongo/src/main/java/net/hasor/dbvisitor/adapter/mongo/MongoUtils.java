/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.driver.AdapterType;
import org.bson.types.Binary;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

/** Shared temporal, size and BSON value utilities, independent of individual commands. */
public final class MongoUtils {
    private MongoUtils() {
    }

    // Parameter values

    /** Uses the same BSON encoding and local time zone as the JDBC date/time setters. */
    public static Object normalizeParameter(Object value) {
        if (value instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) value);
        }
        if (value instanceof LocalDate) {
            return java.sql.Date.valueOf((LocalDate) value);
        }
        if (value instanceof LocalTime) {
            return java.sql.Time.valueOf((LocalTime) value);
        }
        if (value instanceof Instant) {
            return Timestamp.from((Instant) value);
        }
        return value;
    }

    // Size configuration

    /** Parses B/KB/MB/GB; values without a unit are megabytes. */
    static long parseSize(String sizeStr, long defaultValue) {
        if (StringUtils.isBlank(sizeStr)) {
            return defaultValue;
        }
        sizeStr = sizeStr.toUpperCase().trim();
        long multiplier = 1;
        if (StringUtils.endsWithIgnoreCase(sizeStr, "KB")) {
            multiplier = 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (StringUtils.endsWithIgnoreCase(sizeStr, "MB")) {
            multiplier = 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (StringUtils.endsWithIgnoreCase(sizeStr, "GB")) {
            multiplier = 1024 * 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (StringUtils.endsWithIgnoreCase(sizeStr, "B")) {
            sizeStr = sizeStr.substring(0, sizeStr.length() - 1);
        } else {
            multiplier = 1024 * 1024;
        }
        try {
            return Long.parseLong(sizeStr.trim()) * multiplier;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // BSON result values

    static Object jdbcValue(Object value) {
        if (value instanceof Decimal128) {
            return ((Decimal128) value).bigDecimalValue();
        }
        if (value instanceof Binary) {
            return ((Binary) value).getData();
        }
        if (value instanceof Date) {
            return new Timestamp(((Date) value).getTime());
        }
        return value;
    }

    static String jdbcType(Object value) {
        if (value == null) {
            return AdapterType.Null;
        }
        if (value instanceof Integer) {
            return AdapterType.Int;
        }
        if (value instanceof Long) {
            return AdapterType.Long;
        }
        if (value instanceof Double || value instanceof Float) {
            return AdapterType.Double;
        }
        if (value instanceof BigDecimal || value instanceof Decimal128) {
            return AdapterType.BigDecimal;
        }
        if (value instanceof Boolean) {
            return AdapterType.Boolean;
        }
        if (value instanceof Date) {
            return AdapterType.SqlTimestamp;
        }
        if (value instanceof byte[] || value instanceof Binary) {
            return AdapterType.Bytes;
        }
        if (value instanceof List) {
            return AdapterType.Array;
        }
        if (value instanceof String || value instanceof ObjectId) {
            return AdapterType.String;
        }
        return AdapterType.Unknown;
    }
}
