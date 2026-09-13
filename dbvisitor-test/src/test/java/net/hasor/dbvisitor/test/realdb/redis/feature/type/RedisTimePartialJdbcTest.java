/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.type;

import java.sql.SQLException;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.util.Date;
import net.hasor.dbvisitor.types.handler.time.SqlTimestampAsYearTypeHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimestampAsYearMonthTypeHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimestampAsMonthTypeHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimestampAsMonthDayTypeHandler;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;

import static org.junit.Assert.assertEquals;

public class RedisTimePartialJdbcTest extends RedisNativeTypeSupport {
    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL)
    public void timePartialYear_shouldReadFromDateValue() throws SQLException {
        String id = key("10");
        Year year = Year.of(2024);
        jdbcTemplate.executeUpdate("SET ? ?",
                new Object[] { id, java.sql.Date.valueOf(year.atMonth(1).atDay(1)) });

        assertEquals(year, jdbcTemplate.queryForObject("GET ?",
                new Object[] { id }, (rs, row) -> new SqlTimestampAsYearTypeHandler().getResult(rs, "VALUE")));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL_YEAR_MONTH)
    public void timePartialYearMonth_shouldReadFromDateValue() throws SQLException {
        String id = key("11");
        YearMonth yearMonth = YearMonth.of(2024, 3);
        jdbcTemplate.executeUpdate("SET ? ?",
                new Object[] { id, java.sql.Date.valueOf(yearMonth.atDay(1)) });

        assertEquals(yearMonth, jdbcTemplate.queryForObject("GET ?",
                new Object[] { id }, (rs, row) -> new SqlTimestampAsYearMonthTypeHandler().getResult(rs, "VALUE")));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL_MONTH)
    public void timePartialMonth_shouldReadFromDateValue() throws SQLException {
        String id = key("12");
        Month month = Month.MARCH;
        jdbcTemplate.executeUpdate("SET ? ?",
                new Object[] { id, java.sql.Date.valueOf(Year.of(2024).atMonth(month).atDay(1)) });

        assertEquals(month, jdbcTemplate.queryForObject("GET ?",
                new Object[] { id }, (rs, row) -> new SqlTimestampAsMonthTypeHandler().getResult(rs, "VALUE")));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL_MONTH_DAY)
    public void timePartialMonthDay_shouldReadFromDateValue() throws SQLException {
        String id = key("13");
        MonthDay monthDay = MonthDay.of(3, 15);
        jdbcTemplate.executeUpdate("SET ? ?",
                new Object[] { id, java.sql.Date.valueOf(monthDay.atYear(2024)) });

        assertEquals(monthDay, jdbcTemplate.queryForObject("GET ?",
                new Object[] { id }, (rs, row) -> new SqlTimestampAsMonthDayTypeHandler().getResult(rs, "VALUE")));
    }
}
