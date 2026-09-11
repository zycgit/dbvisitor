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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class TimeJulianDayJdbcContractTest extends TimeTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_TIME_JULIAN_DAY)
    public void timeJulianDay_shouldRoundTripBceDatesWithoutDatabaseCalendarConversion() throws SQLException {
        int bcId = baseId() + 22;
        int ancientId = baseId() + 23;
        LocalDate bcDate = LocalDate.of(-99, 1, 1);
        LocalDate ancientDate = LocalDate.of(-499, 6, 1);
        Map<String, Object> bcParams = new HashMap<>();
        bcParams.put("id", bcId);
        bcParams.put("julianDay", bcDate);
        Map<String, Object> ancientParams = new HashMap<>();
        ancientParams.put("id", ancientId);
        ancientParams.put("julianDay", ancientDate);

        jdbcTemplate.executeUpdate(//
                "INSERT INTO time_types_explicit_test (id, julian_day) VALUES (#{id}, #{julianDay, typeHandler=net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler})", //
                bcParams);
        jdbcTemplate.executeUpdate(//
                "INSERT INTO time_types_explicit_test (id, julian_day) VALUES (#{id}, #{julianDay, typeHandler=net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler})", //
                ancientParams);

        LocalDate loadedBc = jdbcTemplate.queryForObject("SELECT julian_day FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { bcId }, (rs, rowNum) -> new JulianDayTypeHandler().getResult(rs, "julian_day"));
        LocalDate loadedAncient = jdbcTemplate.queryForObject("SELECT julian_day FROM time_types_explicit_test WHERE id = ?", //
                new Object[] { ancientId }, (rs, rowNum) -> new JulianDayTypeHandler().getResult(rs, "julian_day"));

        assertEquals(bcDate, loadedBc);
        assertEquals(ancientDate, loadedAncient);
    }
}
