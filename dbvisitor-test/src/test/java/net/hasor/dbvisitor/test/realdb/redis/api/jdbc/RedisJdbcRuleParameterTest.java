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

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcRuleParameterCase;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcRuleParameterTest extends JdbcRuleParameterCase {

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
    @Capability(CapabilityId.JDBC_PARAM_RULE_AND_IN_SET)
    public void ruleParameters_shouldExpandAndInClauses() throws SQLException {
        Assume.assumeTrue("SQL AND/IN clauses do not exist in Redis native commands", false);
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_PARAM_RULE_SET)
    public void ruleParameters_shouldExpandSetClauses() throws SQLException {
        Assume.assumeTrue("SQL SET clauses do not exist in Redis native commands", false);
    }
}
