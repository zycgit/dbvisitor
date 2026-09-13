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

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcPositionalParameterCase;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcPositionalParameterTest extends JdbcPositionalParameterCase {

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
    @Capability(CapabilityId.JDBC_PARAM_POSITIONAL_ARRAY)
    public void positionalArrayParameters_shouldBindObjectArray() throws SQLException {
        String key = fixture.key("positional");
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { key, "hello ' \" ; 世界" });
        assertEquals("hello ' \" ; 世界", jdbcTemplate.queryForString("GET ?", new Object[] { key }));
    }
}
