/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.scenario.query.jdbc.JdbcCommandLifecycleCase;
import org.junit.After;
import org.junit.Before;

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
    protected void resetLifecycleFixture() {
        // The fixture owns fresh private keys for this test.
    }

    @Override
    protected String createCommand() {
        return "HSET '" + fixture.key("source") + "' marker created";
    }

    @Override
    protected String insertCommand() {
        return "HSET '" + fixture.key("source") + "' id ? name ? age ? created ?";
    }

    @Override
    protected int expectedInsertCount() {
        return 4;
    }

    @Override
    protected String alterCommand() {
        return "RENAME '" + fixture.key("source") + "' '" + fixture.key("renamed") + "'";
    }

    @Override
    protected String updateCommand() {
        return "HSET '" + fixture.key("renamed") + "' email ? id ?";
    }

    @Override
    protected String readCommand(String column) {
        return "HGET '" + fixture.key("renamed") + "' " + column;
    }

    @Override
    protected Object[] readArguments() {
        return new Object[0];
    }

    @Override
    protected String retiredObjectQuery() {
        return "EXISTS '" + fixture.key("source") + "'";
    }

    @Override
    protected String dropCommand() {
        return "DEL '" + fixture.key("renamed") + "'";
    }

    @Override
    protected String missingObjectQuery() {
        return "EXISTS '" + fixture.key("renamed") + "'";
    }

    @Override
    protected boolean missingObjectRaisesError() {
        return false;
    }
}
