/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertNull;

@NxnContract
public abstract class TimeNullJdbcCase extends TimeTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_TIME_NULL)
    public void timeNulls_shouldRemainNull() throws SQLException {
        int id = baseId() + 26;

        jdbcTemplate.executeUpdate(//
                "INSERT INTO time_types_explicit_test (id, date_value, time_value, timestamp_value, local_date_ts, local_time_ts, local_datetime_ts) VALUES (?, ?, ?, ?, ?, ?, ?)", //
                new Object[] { id, null, null, null, null, null, null });

        assertNull(jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", new Object[] { id }, LocalDate.class));
        assertNull(jdbcTemplate.queryForObject("SELECT time_value FROM time_types_explicit_test WHERE id = ?", new Object[] { id }, LocalTime.class));
        assertNull(jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { id }, LocalDateTime.class));
        assertNull(jdbcTemplate.queryForObject("SELECT timestamp_value FROM time_types_explicit_test WHERE id = ?", new Object[] { id }, Instant.class));

        Map<String, Object> row = jdbcTemplate.queryForMap(//
                "SELECT date_value, time_value, timestamp_value, local_date_ts, local_time_ts, local_datetime_ts FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { id });

        assertNull(value(row, "date_value"));
        assertNull(value(row, "time_value"));
        assertNull(value(row, "timestamp_value"));
        assertNull(value(row, "local_date_ts"));
        assertNull(value(row, "local_time_ts"));
        assertNull(value(row, "local_datetime_ts"));
    }
}
