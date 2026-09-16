/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class TimeBoundaryJdbcCase extends TimeTypeJdbcSupport {
    // 能力归属：类型处理器 / 日期与时间 / 精度与边界。
    @Test
    @Capability(value = CapabilityId.TYPE_TIME_BOUNDARY, column = "types/dates-and-times/values")
    public void timeBoundaryValues_shouldRoundTripLeapDayMillenniumEpochAndDayEdges() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        int leapId = baseId() + 14;
        int y1999Id = baseId() + 15;
        int y2000Id = baseId() + 16;
        int epochId = baseId() + 17;
        int midnightId = baseId() + 18;
        int endOfDayId = baseId() + 19;
        LocalDate leapDay = LocalDate.of(2024, 2, 29);
        LocalDate y1999 = LocalDate.of(1999, 12, 31);
        LocalDate y2000 = LocalDate.of(2000, 1, 1);
        LocalDateTime midnight = LocalDateTime.of(2024, 3, 15, 0, 0, 0);
        LocalDateTime endOfDay = LocalDateTime.of(2024, 3, 15, 23, 59, 59);

        executeInsert(insertCommand("time_types_explicit_test", "id, date_value"), new Object[] { leapId, java.sql.Date.valueOf(leapDay) });
        executeInsert(insertCommand("time_types_explicit_test", "id, date_value"), new Object[] { y1999Id, java.sql.Date.valueOf(y1999) });
        executeInsert(insertCommand("time_types_explicit_test", "id, date_value"), new Object[] { y2000Id, java.sql.Date.valueOf(y2000) });
        executeInsert(insertCommand("time_types_explicit_test", "id, timestamp_value"), new Object[] { epochId, Timestamp.from(Instant.EPOCH) });
        executeInsert(insertCommand("time_types_explicit_test", "id, timestamp_value"), new Object[] { midnightId, Timestamp.valueOf(midnight) });
        executeInsert(insertCommand("time_types_explicit_test", "id, timestamp_value"), new Object[] { endOfDayId, Timestamp.valueOf(endOfDay) });

        assertEquals(leapDay, jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "date_value"), new Object[] { leapId }, java.sql.Date.class).toLocalDate());
        assertEquals(y1999, jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "date_value"), new Object[] { y1999Id }, java.sql.Date.class).toLocalDate());
        assertEquals(y2000, jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "date_value"), new Object[] { y2000Id }, java.sql.Date.class).toLocalDate());
        assertEquals(0L, jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "timestamp_value"), new Object[] { epochId }, Instant.class).getEpochSecond());
        assertEquals(midnight, jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "timestamp_value"), new Object[] { midnightId }, LocalDateTime.class));
        assertEquals(endOfDay, jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "timestamp_value"), new Object[] { endOfDayId }, LocalDateTime.class));
    }

    // 能力归属：类型处理器 / 日期与时间 / 精度与边界。
    @Test
    @Capability(value = CapabilityId.TYPE_TIME_EXTREME_DATE, column = "types/dates-and-times/values")
    public void timeExtremeDates_shouldRoundTripSupportedHistoricalAndFutureDates() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_EXTREME_DATE);
        int earlyId = baseId() + 20;
        int lateId = baseId() + 21;
        LocalDate earlyDate = LocalDate.of(1900, 1, 1);
        LocalDate lateDate = LocalDate.of(2100, 12, 31);

        executeInsert(insertCommand("time_types_explicit_test", "id, date_value"), new Object[] { earlyId, java.sql.Date.valueOf(earlyDate) });
        executeInsert(insertCommand("time_types_explicit_test", "id, date_value"), new Object[] { lateId, java.sql.Date.valueOf(lateDate) });

        assertEquals(earlyDate, jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "date_value"), new Object[] { earlyId }, java.sql.Date.class).toLocalDate());
        assertEquals(lateDate, jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "date_value"), new Object[] { lateId }, java.sql.Date.class).toLocalDate());
    }

    // 能力归属：类型处理器 / 日期与时间 / 精度与边界。
    @Test
    @Capability(value = CapabilityId.TYPE_TIME_PRECISION, column = "types/dates-and-times/values")
    public void timePrecision_shouldPreserveMillisecondAndTimestampPrecisionWithinDatabaseLimits() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        int localDateTimeId = baseId() + 24;
        int timestampId = baseId() + 25;
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_000_000);
        Timestamp original = Timestamp.valueOf("2024-03-15 14:30:45.123456789");
        original.setNanos(123456789);

        executeInsert(insertCommand("time_types_explicit_test", "id, timestamp_value"), new Object[] { localDateTimeId, Timestamp.valueOf(dateTime) });
        executeInsert(insertCommand("time_types_explicit_test", "id, timestamp_value"), new Object[] { timestampId, original });

        LocalDateTime loadedDateTime = jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "timestamp_value"), new Object[] { localDateTimeId }, LocalDateTime.class);
        Timestamp loadedTimestamp = jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "timestamp_value"), new Object[] { timestampId }, Timestamp.class);

        assertEquals(dateTime.getYear(), loadedDateTime.getYear());
        assertEquals(dateTime.getMonth(), loadedDateTime.getMonth());
        assertEquals(dateTime.getDayOfMonth(), loadedDateTime.getDayOfMonth());
        assertEquals(dateTime.getHour(), loadedDateTime.getHour());
        assertEquals(dateTime.getMinute(), loadedDateTime.getMinute());
        assertEquals(dateTime.getSecond(), loadedDateTime.getSecond());
        assertTrue(Math.abs(dateTime.getNano() - loadedDateTime.getNano()) < 1_000_000);
        assertTrue(Math.abs(original.getTime() - loadedTimestamp.getTime()) < 1000);
        assertEquals("Millisecond-aligned LocalDateTime must not lose its fractional second", dateTime, loadedDateTime);
        assertEquals("Timestamp milliseconds must survive the configured timestamp column", original.getTime(), loadedTimestamp.getTime());
        assertTrue("Sub-millisecond rounding or truncation must stay below one millisecond", Math.abs(original.getNanos() - loadedTimestamp.getNanos()) < 1_000_000);
    }
}
