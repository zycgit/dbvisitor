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
import net.hasor.dbvisitor.test.contract.feature.type.TimePartialJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimestampAsMonthDayTypeHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimestampAsMonthTypeHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimestampAsYearMonthTypeHandler;
import net.hasor.dbvisitor.types.handler.time.SqlTimestampAsYearTypeHandler;
import org.junit.After;
import org.junit.Before;

public class RedisTimePartialJdbcTest extends TimePartialJdbcCase {
    private final RedisTypeCommandFixture fixture = new RedisTypeCommandFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected String insertCommand(String table, String columns, String... parameters) {
        return this.fixture.insertCommand(table, columns, parameters);
    }

    @Override
    protected int executeInsert(String command, Object[] parameters) throws SQLException {
        return this.jdbcTemplate.queryForObject(command, parameters, Integer.class);
    }

    @Override
    protected String selectCommand(String table, String columns) {
        return this.fixture.selectCommand(table, columns);
    }

    @Override
    protected <T> T readPartialValue(int id, Class<T> type) throws SQLException {
        TypeHandler<?> handler;
        if (type == Year.class) {
            handler = new SqlTimestampAsYearTypeHandler();
        } else if (type == YearMonth.class) {
            handler = new SqlTimestampAsYearMonthTypeHandler();
        } else if (type == Month.class) {
            handler = new SqlTimestampAsMonthTypeHandler();
        } else if (type == MonthDay.class) {
            handler = new SqlTimestampAsMonthDayTypeHandler();
        } else {
            throw new IllegalArgumentException("Unexpected partial time type: " + type);
        }
        return this.jdbcTemplate.queryForObject(selectCommand("time_types_explicit_test", "date_value"), selectParameters(id), (rs, row) -> type.cast(handler.getResult(rs, "VALUE")));
    }
}
