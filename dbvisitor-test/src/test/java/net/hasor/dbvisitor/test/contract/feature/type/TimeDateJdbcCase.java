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
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class TimeDateJdbcCase extends TimeTypeJdbcSupport {
    // 能力归属：类型处理器 / 日期与时间 / 日期。
    @Test
    @Capability(value = CapabilityId.TYPE_TIME_SQL_DATE, column = "types/dates-and-times/values")
    public void timeSqlDate_shouldRoundTripDateColumn() throws SQLException {
        int id = baseId() + 1;
        LocalDate date = LocalDate.of(2024, 3, 15);

        executeInsert(insertCommand("time_types_explicit_test", "id, date_value"), //
                new Object[] { id, java.sql.Date.valueOf(date) });

        java.sql.Date loadedSql = jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "date_value"), //
                new Object[] { id }, java.sql.Date.class);

        assertNotNull(loadedSql);
        assertEquals(date, loadedSql.toLocalDate());
    }

    // 能力归属：类型处理器 / 日期与时间 / 日期。
    @Test
    @Capability(value = CapabilityId.TYPE_TIME_LOCAL_DATE, column = "types/dates-and-times/values")
    public void timeLocalDate_shouldRoundTripDateColumn() throws SQLException {
        requiresNxnFeature(FeatureId.TIME_LOCAL_DATE);
        int id = baseId() + 4;
        LocalDate date = LocalDate.of(2024, 3, 15);

        executeInsert(insertCommand("time_types_explicit_test", "id, date_value"), //
                new Object[] { id, java.sql.Date.valueOf(date) });

        LocalDate loaded = jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "date_value"), //
                new Object[] { id }, LocalDate.class);

        assertEquals(date, loaded);
    }
}
