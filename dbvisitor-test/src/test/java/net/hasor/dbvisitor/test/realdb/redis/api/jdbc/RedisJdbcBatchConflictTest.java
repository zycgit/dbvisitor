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
import org.junit.Assume;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcBatchConflictCase;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcBatchConflictTest extends JdbcBatchConflictCase {

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
    @Capability(CapabilityId.JDBC_BATCH_PARTIAL_FAILURE)
    public void jdbcBatchPartialFailure_shouldPropagateDuplicateKeyError() {
        Assume.assumeTrue("Redis SET overwrites existing keys; SET NX refusal is a normal reply, not a duplicate-key exception", false);
    }
}
