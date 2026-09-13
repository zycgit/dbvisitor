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
public abstract class TimePartialJdbcCase extends TimeTypeJdbcSupport {
    protected <T> T readPartialValue(int id, Class<T> type) throws SQLException {
        return jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "date_value"), selectParameters(id), type);
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL)
    public void timePartialYear_shouldReadFromDateValue() throws SQLException {
        int id = baseId() + 10;
        Year year = Year.of(2024);
        executeInsert(insertCommand("time_types_explicit_test", "id, date_value"),
                new Object[] { id, java.sql.Date.valueOf(year.atMonth(1).atDay(1)) });

        assertEquals(year, readPartialValue(id, Year.class));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL_YEAR_MONTH)
    public void timePartialYearMonth_shouldReadFromDateValue() throws SQLException {
        int id = baseId() + 11;
        YearMonth yearMonth = YearMonth.of(2024, 3);
        executeInsert(insertCommand("time_types_explicit_test", "id, date_value"),
                new Object[] { id, java.sql.Date.valueOf(yearMonth.atDay(1)) });

        assertEquals(yearMonth, readPartialValue(id, YearMonth.class));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL_MONTH)
    public void timePartialMonth_shouldReadFromDateValue() throws SQLException {
        int id = baseId() + 12;
        Month month = Month.MARCH;
        executeInsert(insertCommand("time_types_explicit_test", "id, date_value"),
                new Object[] { id, java.sql.Date.valueOf(Year.of(2024).atMonth(month).atDay(1)) });

        assertEquals(month, readPartialValue(id, Month.class));
    }

    @Test
    @Capability(CapabilityId.TYPE_TIME_PARTIAL_MONTH_DAY)
    public void timePartialMonthDay_shouldReadFromDateValue() throws SQLException {
        int id = baseId() + 13;
        MonthDay monthDay = MonthDay.of(3, 15);
        executeInsert(insertCommand("time_types_explicit_test", "id, date_value"),
                new Object[] { id, java.sql.Date.valueOf(monthDay.atYear(2024)) });

        assertEquals(monthDay, readPartialValue(id, MonthDay.class));
    }
}
