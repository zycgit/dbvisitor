/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.type;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RedisTimeBoundaryJdbcTest extends RedisNativeTypeSupport {
    @Test
    @Capability(CapabilityId.TYPE_TIME_BOUNDARY)
    public void timeBoundaryValues_shouldRoundTripLeapDayMillenniumEpochAndDayEdges() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        String leapId = key("14");
        String y1999Id = key("15");
        String y2000Id = key("16");
        String epochId = key("17");
        String midnightId = key("18");
        String endOfDayId = key("19");
        LocalDate leapDay = LocalDate.of(2024, 2, 29);
        LocalDate y1999 = LocalDate.of(1999, 12, 31);
        LocalDate y2000 = LocalDate.of(2000, 1, 1);
        LocalDateTime midnight = LocalDateTime.of(2024, 3, 15, 0, 0, 0);
        LocalDateTime endOfDay = LocalDateTime.of(2024, 3, 15, 23, 59, 59);

        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { leapId, java.sql.Date.valueOf(leapDay) });
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { y1999Id, java.sql.Date.valueOf(y1999) });
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { y2000Id, java.sql.Date.valueOf(y2000) });
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { epochId, Timestamp.from(Instant.EPOCH) });
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { midnightId, Timestamp.valueOf(midnight) });
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { endOfDayId, Timestamp.valueOf(endOfDay) });

        assertEquals(leapDay, jdbcTemplate.queryForObject("GET ?", new Object[] { leapId }, java.sql.Date.class).toLocalDate());
        assertEquals(y1999, jdbcTemplate.queryForObject("GET ?", new Object[] { y1999Id }, java.sql.Date.class).toLocalDate());
        assertEquals(y2000, jdbcTemplate.queryForObject("GET ?", new Object[] { y2000Id }, java.sql.Date.class).toLocalDate());
        assertEquals(0L, jdbcTemplate.queryForObject("GET ?", new Object[] { epochId }, Instant.class).getEpochSecond());
        assertEquals(midnight, jdbcTemplate.queryForObject("GET ?", new Object[] { midnightId }, LocalDateTime.class));
        assertEquals(endOfDay, jdbcTemplate.queryForObject("GET ?", new Object[] { endOfDayId }, LocalDateTime.class));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_EXTREME_DATE)
    public void timeExtremeDates_shouldRoundTripSupportedHistoricalAndFutureDates() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_EXTREME_DATE);
        String earlyId = key("20");
        String lateId = key("21");
        LocalDate earlyDate = LocalDate.of(1900, 1, 1);
        LocalDate lateDate = LocalDate.of(2100, 12, 31);

        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { earlyId, java.sql.Date.valueOf(earlyDate) });
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { lateId, java.sql.Date.valueOf(lateDate) });

        assertEquals(earlyDate, jdbcTemplate.queryForObject("GET ?", new Object[] { earlyId }, java.sql.Date.class).toLocalDate());
        assertEquals(lateDate, jdbcTemplate.queryForObject("GET ?", new Object[] { lateId }, java.sql.Date.class).toLocalDate());
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PRECISION)
    public void timePrecision_shouldPreserveMillisecondAndTimestampPrecisionWithinDatabaseLimits() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        String localDateTimeId = key("24");
        String timestampId = key("25");
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_000_000);
        Timestamp original = new Timestamp(System.currentTimeMillis());
        original.setNanos(123456789);

        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { localDateTimeId, Timestamp.valueOf(dateTime) });
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { timestampId, original });

        LocalDateTime loadedDateTime = jdbcTemplate.queryForObject("GET ?", new Object[] { localDateTimeId }, LocalDateTime.class);
        Timestamp loadedTimestamp = jdbcTemplate.queryForObject("GET ?", new Object[] { timestampId }, Timestamp.class);

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
