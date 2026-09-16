/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.type;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.feature.type.TimeInstantJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisTimeInstantJdbcTest extends TimeInstantJdbcCase {
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
}
