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

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcBatchErrorContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcBatchErrorContractTest extends JdbcBatchErrorContractTest {

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
    @Capability(CapabilityId.JDBC_BATCH_SQL_ERROR)
    public void jdbcBatchStatements_shouldPropagateSqlErrorWithoutRequiringUniqueConstraints() throws SQLException {
        String old = fixture.key("old"), before = fixture.key("before"), after = fixture.key("after");
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { old, "unchanged" });
        try {
            jdbcTemplate.executeBatch(new String[] { "SET '" + before + "' 'before'", "LPUSH '" + old + "' 'invalid-type'", "SET '" + after + "' 'after'" });
            fail("WRONGTYPE must reach the caller");
        } catch (SQLException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("WRONGTYPE"));
        }
        assertEquals("unchanged", jdbcTemplate.queryForString("GET ?", new Object[] { old }));
        assertEquals("before", jdbcTemplate.queryForString("GET ?", new Object[] { before }));
        assertEquals(Long.valueOf(0), jdbcTemplate.queryForLong("EXISTS ?", new Object[] { after }));
    }
}
