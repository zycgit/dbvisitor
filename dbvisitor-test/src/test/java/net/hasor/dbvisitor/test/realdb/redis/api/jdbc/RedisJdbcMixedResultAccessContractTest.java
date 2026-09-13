/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.SQLException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcMixedResultAccessContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcMixedResultAccessContractTest extends JdbcMixedResultAccessContractTest {

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
    @Capability(CapabilityId.JDBC_RESULT_MAP_AND_SCALAR)
    public void resultShortcuts_shouldReturnMapListAndScalarValues() throws SQLException {
        fixture.seedScores();
        Object[] args = { fixture.key("scores") };
        assertEquals("member-1", jdbcTemplate.queryForMap("ZRANGE ? 0 0 WITHSCORES", args).get("ELEMENT"));
        assertEquals(4, jdbcTemplate.queryForList("ZRANGE ? 0 3 WITHSCORES", args).size());
        assertEquals(Long.valueOf(10), jdbcTemplate.queryForObject("ZCARD ?", args, Long.class));
        assertEquals(Arrays.asList("member-1", "member-2"), jdbcTemplate.queryForList("ZRANGE ? 0 1", args, String.class));
    }
}
