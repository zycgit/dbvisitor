/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.type;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler;

import static org.junit.Assert.assertEquals;

public class RedisTimeJulianDayJdbcTest extends RedisNativeTypeSupport {
    @Test
    @Capability(CapabilityId.TYPE_TIME_JULIAN_DAY)
    public void timeJulianDay_shouldRoundTripBceDatesWithoutDatabaseCalendarConversion() throws SQLException {
        String bcId = key("22");
        String ancientId = key("23");
        LocalDate bcDate = LocalDate.of(-99, 1, 1);
        LocalDate ancientDate = LocalDate.of(-499, 6, 1);
        Map<String, Object> bcParams = new HashMap<>();
        bcParams.put("id", bcId);
        bcParams.put("julianDay", bcDate);
        Map<String, Object> ancientParams = new HashMap<>();
        ancientParams.put("id", ancientId);
        ancientParams.put("julianDay", ancientDate);

        jdbcTemplate.executeUpdate(//
                "SET #{id} #{julianDay, typeHandler=net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler}", //
                bcParams);
        jdbcTemplate.executeUpdate(//
                "SET #{id} #{julianDay, typeHandler=net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler}", //
                ancientParams);

        LocalDate loadedBc = jdbcTemplate.queryForObject("GET ?", //
                new Object[] { bcId }, (rs, rowNum) -> new JulianDayTypeHandler().getResult(rs, "VALUE"));
        LocalDate loadedAncient = jdbcTemplate.queryForObject("GET ?", //
                new Object[] { ancientId }, (rs, rowNum) -> new JulianDayTypeHandler().getResult(rs, "VALUE"));

        assertEquals(bcDate, loadedBc);
        assertEquals(ancientDate, loadedAncient);
    }
}
