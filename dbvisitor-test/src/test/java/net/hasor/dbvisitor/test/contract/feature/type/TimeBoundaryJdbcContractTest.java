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
import java.util.Date;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class TimeBoundaryJdbcContractTest extends TimeTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_TIME_BOUNDARY)
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

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { leapId, java.sql.Date.valueOf(leapDay) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { y1999Id, java.sql.Date.valueOf(y1999) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { y2000Id, java.sql.Date.valueOf(y2000) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", new Object[] { epochId, Timestamp.from(Instant.EPOCH) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", new Object[] { midnightId, Timestamp.valueOf(midnight) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", new Object[] { endOfDayId, Timestamp.valueOf(endOfDay) });

        assertEquals(leapDay, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { leapId }, java.sql.Date.class).toLocalDate());
        assertEquals(y1999, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { y1999Id }, java.sql.Date.class).toLocalDate());
        assertEquals(y2000, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { y2000Id }, java.sql.Date.class).toLocalDate());
        assertEquals(0L, jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { epochId }, Instant.class).getEpochSecond());
        assertEquals(midnight, jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { midnightId }, LocalDateTime.class));
        assertEquals(endOfDay, jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { endOfDayId }, LocalDateTime.class));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_EXTREME_DATE)
    public void timeExtremeDates_shouldRoundTripSupportedHistoricalAndFutureDates() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_EXTREME_DATE);
        int earlyId = baseId() + 20;
        int lateId = baseId() + 21;
        LocalDate earlyDate = LocalDate.of(1900, 1, 1);
        LocalDate lateDate = LocalDate.of(2100, 12, 31);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { earlyId, java.sql.Date.valueOf(earlyDate) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", new Object[] { lateId, java.sql.Date.valueOf(lateDate) });

        assertEquals(earlyDate, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { earlyId }, java.sql.Date.class).toLocalDate());
        assertEquals(lateDate, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { lateId }, java.sql.Date.class).toLocalDate());
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PRECISION)
    public void timePrecision_shouldPreserveMillisecondAndTimestampPrecisionWithinDatabaseLimits() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        int localDateTimeId = baseId() + 24;
        int timestampId = baseId() + 25;
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_000_000);
        Timestamp original = new Timestamp(System.currentTimeMillis());
        original.setNanos(123456789);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", new Object[] { localDateTimeId, Timestamp.valueOf(dateTime) });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", new Object[] { timestampId, original });

        LocalDateTime loadedDateTime = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { localDateTimeId }, LocalDateTime.class);
        Timestamp loadedTimestamp = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { timestampId }, Timestamp.class);

        assertEquals(dateTime.getYear(), loadedDateTime.getYear());
        assertEquals(dateTime.getMonth(), loadedDateTime.getMonth());
        assertEquals(dateTime.getDayOfMonth(), loadedDateTime.getDayOfMonth());
        assertEquals(dateTime.getHour(), loadedDateTime.getHour());
        assertEquals(dateTime.getMinute(), loadedDateTime.getMinute());
        assertEquals(dateTime.getSecond(), loadedDateTime.getSecond());
        assertTrue(Math.abs(dateTime.getNano() - loadedDateTime.getNano()) < 1_000_000);
        assertTrue(Math.abs(original.getTime() - loadedTimestamp.getTime()) < 1000);
    }
}
