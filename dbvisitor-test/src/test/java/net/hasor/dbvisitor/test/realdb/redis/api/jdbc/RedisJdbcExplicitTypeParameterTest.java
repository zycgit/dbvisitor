/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcExplicitTypeParameterCase;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.string.StringTypeHandler;
import net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler;
import static org.junit.Assert.*;

public class RedisJdbcExplicitTypeParameterTest extends JdbcExplicitTypeParameterCase {

    private final RedisJdbcFixture fixture = new RedisJdbcFixture();

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
    public void closeRedisFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_PARAM_SQLARG)
    public void sqlArgParameters_shouldUseExplicitTypeHandlers() throws SQLException {
        String key = fixture.key("explicit");
        jdbcTemplate.executeUpdate("SET ? ?", new SqlArg[] {
            SqlArg.valueOf(key, new StringTypeHandler()),
            SqlArg.valueOf(123, new IntegerTypeHandler()) });
        assertEquals("123", jdbcTemplate.queryForString("GET ?", new Object[] { key }));
    }
}
