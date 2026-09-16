/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class MilvusUtilsTest {
    @Test
    public void javaTimeAndJdbcValuesUseTheSameTextIncludingTimestampNanoseconds() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        LocalTime time = LocalTime.of(8, 9, 10);
        LocalDateTime dateTime = LocalDateTime.of(date, time).withNano(123456789);
        Instant instant = Instant.parse("2026-09-10T00:09:10.123456789Z");

        assertEquals("2026-09-10", MilvusUtils.temporalValue(date));
        assertEquals("08:09:10", MilvusUtils.temporalValue(time));
        assertEquals("2026-09-10 08:09:10.123456789", MilvusUtils.temporalValue(dateTime));
        assertEquals(MilvusUtils.temporalValue(Date.valueOf(date)), MilvusUtils.temporalValue(date));
        assertEquals(MilvusUtils.temporalValue(Time.valueOf(time)), MilvusUtils.temporalValue(time));
        assertEquals(MilvusUtils.temporalValue(Timestamp.valueOf(dateTime)), MilvusUtils.temporalValue(dateTime));
        assertEquals(MilvusUtils.temporalValue(Timestamp.from(instant)), MilvusUtils.temporalValue(instant));
        assertEquals(Timestamp.from(instant).toString(), MilvusUtils.temporalValue(instant));

        java.util.Date legacy = java.util.Date.from(instant);
        assertEquals(new Timestamp(legacy.getTime()).toString(), MilvusUtils.temporalValue(legacy));
    }

    @Test
    public void nonTemporalValuesAreNotConverted() {
        Object[] values = { null, "2026-09-10", 42, true, new float[] { 1, 2 }, Collections.singletonMap("key", "value") };
        for (Object value : values) {
            assertSame(value, MilvusUtils.temporalValue(value));
        }
    }

    @Test
    public void sqlExceptionsKeepTheirIdentityStateAndErrorCode() {
        SQLException failure = new SQLTransientException("retry later", "08001", 42);
        SQLException actual = MilvusUtils.sqlException(failure);
        assertSame(failure, actual);
        assertEquals("08001", actual.getSQLState());
        assertEquals(42, actual.getErrorCode());
    }

    @Test
    public void nonSqlExceptionsKeepTheirCauseAndMessage() {
        IllegalArgumentException failure = new IllegalArgumentException("invalid value");
        SQLException actual = MilvusUtils.sqlException(failure);
        assertSame(failure, actual.getCause());
        assertEquals("invalid value", actual.getMessage());
    }
}
