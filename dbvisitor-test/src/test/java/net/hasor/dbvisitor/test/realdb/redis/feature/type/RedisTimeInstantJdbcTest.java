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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RedisTimeInstantJdbcTest extends RedisNativeTypeSupport {
    @Test
    @Capability(CapabilityId.TYPE_TIME_LOCAL_DATETIME)
    public void timeLocalDateTimeAndTimestamp_shouldRoundTripTimestampColumn() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        String id = key("6");
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_000_000);
        Timestamp timestamp = Timestamp.valueOf(dateTime);

        jdbcTemplate.executeUpdate("SET ? ?", //
                new Object[] { id, timestamp });

        LocalDateTime loadedDateTime = jdbcTemplate.queryForObject("GET ?", //
                new Object[] { id }, LocalDateTime.class);
        Timestamp loadedTimestamp = jdbcTemplate.queryForObject("GET ?", //
                new Object[] { id }, Timestamp.class);

        assertNotNull(loadedDateTime);
        assertNotNull(loadedTimestamp);
        assertEquals(dateTime.getYear(), loadedDateTime.getYear());
        assertEquals(dateTime.getMonth(), loadedDateTime.getMonth());
        assertEquals(dateTime.getDayOfMonth(), loadedDateTime.getDayOfMonth());
        assertEquals(dateTime.getHour(), loadedDateTime.getHour());
        assertEquals(dateTime.getMinute(), loadedDateTime.getMinute());
        assertEquals(dateTime.getSecond(), loadedDateTime.getSecond());
        assertTrue(Math.abs(timestamp.getTime() - loadedTimestamp.getTime()) < 1000);
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_INSTANT)
    public void timeInstantAndUtilDate_shouldRoundTripTimestampColumn() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        String id = key("7");
        Instant instant = Instant.parse("2024-03-15T14:30:45.123Z");

        jdbcTemplate.executeUpdate("SET ? ?", //
                new Object[] { id, Timestamp.from(instant) });

        Instant loadedInstant = jdbcTemplate.queryForObject("GET ?", //
                new Object[] { id }, Instant.class);
        Date loadedDate = jdbcTemplate.queryForObject("GET ?", //
                new Object[] { id }, Date.class);

        assertNotNull(loadedInstant);
        assertNotNull(loadedDate);
        assertTrue(Math.abs(instant.toEpochMilli() - loadedInstant.toEpochMilli()) < 1000);
        assertTrue(Math.abs(instant.toEpochMilli() - loadedDate.getTime()) < 1000);
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_ZONE_INSTANT)
    public void timeZoneValues_shouldRoundTripAsEquivalentInstants() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        String offsetId = key("8");
        String zonedId = key("9");
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2024-03-15T14:30:45+08:00");
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2024-03-15T14:30:45+08:00[Asia/Shanghai]");

        jdbcTemplate.executeUpdate("SET ? ?", //
                new Object[] { offsetId, Timestamp.from(offsetDateTime.toInstant()) });
        jdbcTemplate.executeUpdate("SET ? ?", //
                new Object[] { zonedId, Timestamp.from(zonedDateTime.toInstant()) });

        Timestamp loadedOffset = jdbcTemplate.queryForObject("GET ?", new Object[] { offsetId }, Timestamp.class);
        Timestamp loadedZoned = jdbcTemplate.queryForObject("GET ?", new Object[] { zonedId }, Timestamp.class);

        assertNotNull(loadedOffset);
        assertNotNull(loadedZoned);
        assertTrue(Math.abs(offsetDateTime.toInstant().toEpochMilli() - loadedOffset.toInstant().toEpochMilli()) < 2000);
        assertTrue(Math.abs(zonedDateTime.toInstant().toEpochMilli() - loadedZoned.toInstant().toEpochMilli()) < 2000);
    }
}
