/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.util.Date;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class TimePartialJdbcContractTest extends TimeTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL)
    public void timePartialYear_shouldReadFromDateValue() throws SQLException {
        int id = baseId() + 10;
        Year year = Year.of(2024);
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",
                new Object[] { id, java.sql.Date.valueOf(year.atMonth(1).atDay(1)) });

        assertEquals(year, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",
                new Object[] { id }, Year.class));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL_YEAR_MONTH)
    public void timePartialYearMonth_shouldReadFromDateValue() throws SQLException {
        int id = baseId() + 11;
        YearMonth yearMonth = YearMonth.of(2024, 3);
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",
                new Object[] { id, java.sql.Date.valueOf(yearMonth.atDay(1)) });

        assertEquals(yearMonth, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",
                new Object[] { id }, YearMonth.class));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL_MONTH)
    public void timePartialMonth_shouldReadFromDateValue() throws SQLException {
        int id = baseId() + 12;
        Month month = Month.MARCH;
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",
                new Object[] { id, java.sql.Date.valueOf(Year.of(2024).atMonth(month).atDay(1)) });

        assertEquals(month, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",
                new Object[] { id }, Month.class));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL_MONTH_DAY)
    public void timePartialMonthDay_shouldReadFromDateValue() throws SQLException {
        int id = baseId() + 13;
        MonthDay monthDay = MonthDay.of(3, 15);
        jdbcTemplate.executeUpdate("INSERT INTO time_types_explicit_test (id, date_value) VALUES (?, ?)",
                new Object[] { id, java.sql.Date.valueOf(monthDay.atYear(2024)) });

        assertEquals(monthDay, jdbcTemplate.queryForObject("SELECT date_value FROM time_types_explicit_test WHERE id = ?",
                new Object[] { id }, MonthDay.class));
    }
}
