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

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCommandLifecycleCase;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcCommandLifecycleTest extends JdbcCommandLifecycleCase {

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
    @Capability(CapabilityId.JDBC_CRUD_EXECUTE_DDL)
    public void jdbcExecute_shouldRunDdlAndDmlTableOperations() throws SQLException {
        String source = fixture.key("source");
        String renamed = fixture.key("renamed");
        jdbcTemplate.execute("HSET " + source + " name Alice age 18");
        jdbcTemplate.executeUpdate("HSET ? email ?", new Object[] { source, "alice@example.test" });
        jdbcTemplate.execute("RENAME " + source + " " + renamed);
        assertEquals(Long.valueOf(0), jdbcTemplate.queryForLong("EXISTS ?", new Object[] { source }));
        assertEquals("Alice", jdbcTemplate.queryForString("HGET ? name", new Object[] { renamed }));
        assertEquals("alice@example.test", jdbcTemplate.queryForString("HGET ? email", new Object[] { renamed }));
        jdbcTemplate.execute("DEL " + renamed);
        assertEquals(Long.valueOf(0), jdbcTemplate.queryForLong("EXISTS ?", new Object[] { renamed }));
    }
}
