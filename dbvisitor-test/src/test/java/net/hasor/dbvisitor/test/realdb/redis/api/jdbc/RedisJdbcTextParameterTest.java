/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.SQLException;
import java.util.Map;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcTextParameterCase;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcTextParameterTest extends JdbcTextParameterCase {

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
    @Capability(CapabilityId.JDBC_PARAM_TEXT_REPLACEMENT)
    public void textReplacementParameters_shouldCombineIdentifiersAndBoundValues() throws SQLException {
        String key = fixture.key("text");
        Map<String, Object> args = Map.of("command", "SET", "key", key, "value", "a b; 'quoted'");
        jdbcTemplate.executeUpdate("${command} :key :value", args);
        assertEquals("a b; 'quoted'", jdbcTemplate.queryForString("${command} :key", Map.of("command", "GET", "key", key)));
    }
}
