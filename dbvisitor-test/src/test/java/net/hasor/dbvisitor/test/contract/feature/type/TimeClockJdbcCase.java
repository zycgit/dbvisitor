/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class TimeClockJdbcCase extends TimeTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_TIME_SQL_STANDARD)
    public void timeSqlStandardTypes_shouldRoundTripTimeAndTimestampColumns() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        int timeId = baseId() + 2;
        int timestampId = baseId() + 3;
        Time time = Time.valueOf("14:30:45");
        Timestamp timestamp = Timestamp.valueOf("2024-03-15 14:30:45.123");

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, time_value) VALUES (?, ?)", new Object[] { timeId, time });
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, timestamp_value) VALUES (?, ?)", new Object[] { timestampId, timestamp });

        Time loadedTime = jdbcTemplate.queryForObject("SELECT time_value FROM time_types_explicit_test WHERE id = ?", new Object[] { timeId }, Time.class);
        Timestamp loadedTimestamp = jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { timestampId }, Timestamp.class);

        assertNotNull(loadedTime);
        assertNotNull(loadedTimestamp);
        assertEquals(time.toString(), loadedTime.toString());
        assertTrue(Math.abs(timestamp.getTime() - loadedTimestamp.getTime()) < 1000);
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_LOCAL_TIME)
    public void timeLocalTime_shouldRoundTripTimeColumn() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_ZONE_STABLE_ROUND_TRIP);

        int id = baseId() + 5;
        LocalTime time = LocalTime.of(14, 30, 45);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, time_value) VALUES (?, ?)", //
                new Object[] { id, java.sql.Time.valueOf(time) });

        LocalTime loaded = jdbcTemplate.queryForObject("SELECT time_value FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { id }, LocalTime.class);

        assertEquals(time, loaded);
    }
}
