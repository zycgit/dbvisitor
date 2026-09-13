/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class TimeDateJdbcCase extends TimeTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_TIME_SQL_DATE)
    public void timeSqlDate_shouldRoundTripDateColumn() throws SQLException {
        int id = baseId() + 1;
        LocalDate date = LocalDate.of(2024, 3, 15);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", //
                new Object[] { id, java.sql.Date.valueOf(date) });

        java.sql.Date loadedSql = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { id }, java.sql.Date.class);

        assertNotNull(loadedSql);
        assertEquals(date, loadedSql.toLocalDate());
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_LOCAL_DATE)
    public void timeLocalDate_shouldRoundTripDateColumn() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_LOCAL_DATE);
        int id = baseId() + 4;
        LocalDate date = LocalDate.of(2024, 3, 15);

        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)", //
                new Object[] { id, java.sql.Date.valueOf(date) });

        LocalDate loaded = jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { id }, LocalDate.class);

        assertEquals(date, loaded);
    }
}
