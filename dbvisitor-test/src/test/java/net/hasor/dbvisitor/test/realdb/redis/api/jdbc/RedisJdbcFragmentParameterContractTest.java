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
import java.util.List;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcFragmentParameterContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcFragmentParameterContractTest extends JdbcFragmentParameterContractTest {

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
    @Capability(CapabilityId.JDBC_PARAM_TEXT_FRAGMENT)
    public void textReplacementParameters_shouldInjectSqlIdentifiersAndOrderClauses() throws SQLException {
        fixture.seedScores();
        Map<String, Object> args = Map.of("key", fixture.key("scores"), "options", "REV WITHSCORES");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("ZRANGE :key 0 1 ${options}", args);
        assertEquals(2, rows.size());
        assertEquals("member-10", rows.get(0).get("ELEMENT"));
        assertEquals(Double.valueOf(30), rows.get(0).get("SCORE"));
    }
}
