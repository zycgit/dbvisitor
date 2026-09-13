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

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcBatchLargeContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcBatchLargeContractTest extends JdbcBatchLargeContractTest {

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
    @Capability(CapabilityId.JDBC_BATCH_LARGE_INSERT)
    public void jdbcBatchLargeInsert_shouldInsertManyNamedRows() throws SQLException {
        Map[] args = new Map[200];
        for (int i = 0; i < args.length; i++) { args[i] = Map.of("key", fixture.key("large-" + i), "value", "value-" + i); }
        assertEquals(200, jdbcTemplate.executeBatch("SET :key :value", args).length);
        for (int i = 0; i < args.length; i++) { assertEquals("value-" + i, jdbcTemplate.queryForString("GET :key", args[i])); }
    }
}
