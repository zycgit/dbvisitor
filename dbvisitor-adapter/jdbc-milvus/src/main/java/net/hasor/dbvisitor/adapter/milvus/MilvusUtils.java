/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Shared value and exception conversion, independent of schema and command execution. */
public final class MilvusUtils {
    private MilvusUtils() {
    }

    /** Use the same JDBC temporal representation for VARCHAR writes and filter values; preserve all other values. */
    public static Object temporalValue(Object value) {
        if (value instanceof java.util.Date date) {
            return temporalText(date);
        }
        if (value instanceof LocalDateTime dateTime) {
            return temporalText(java.sql.Timestamp.valueOf(dateTime));
        }
        if (value instanceof LocalDate date) {
            return temporalText(java.sql.Date.valueOf(date));
        }
        if (value instanceof LocalTime time) {
            return temporalText(java.sql.Time.valueOf(time));
        }
        if (value instanceof Instant instant) {
            return temporalText(java.sql.Timestamp.from(instant));
        }
        return value;
    }

    /** Stable JDBC text for VARCHAR storage and filter templates, not a native Milvus temporal type. */
    public static String temporalText(java.util.Date value) {
        if (value instanceof java.sql.Date || value instanceof java.sql.Time || value instanceof java.sql.Timestamp) {
            return value.toString();
        }
        return new java.sql.Timestamp(value.getTime()).toString();
    }

    // JDBC exception conversion

    public static SQLException sqlException(Exception failure) {
        return failure instanceof SQLException ? (SQLException) failure : new SQLException(failure.getMessage(), failure);
    }
}
