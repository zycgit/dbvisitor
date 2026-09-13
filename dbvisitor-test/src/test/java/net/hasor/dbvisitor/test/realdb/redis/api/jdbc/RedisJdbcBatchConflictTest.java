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

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcBatchConflictCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;

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
    protected String insertCommand() {
        return "HSET '" + fixture.key("batch") + "' ? ?";
    }

    @Override
    protected String namedInsertCommand() {
        return "HSET '" + fixture.key("batch") + "' :id :val";
    }

    @Override
    protected String updateCommand() {
        return insertCommand();
    }

    @Override
    protected String deleteCommand() {
        return "HDEL '" + fixture.key("batch") + "' ?";
    }

    @Override
    protected String valueCommand() {
        return "HGET '" + fixture.key("batch") + "' ?";
    }

    @Override
    protected String countCommand() {
        return "HEXISTS '" + fixture.key("batch") + "' ?";
    }

    @Override
    protected String invalidCommand() {
        return "NXN_UNKNOWN_COMMAND";
    }

    @Override
    protected Object[] updateArguments(String value, int id) {
        return new Object[] { id, value };
    }

    @Override
    protected String countRangeCommand(boolean inclusiveEnd) {
        String bound = inclusiveEnd ? " <= " : " < ";
        return "EVAL 'local n = 0; for _, k in ipairs(redis.call(\"HKEYS\", KEYS[1])) do "
                + "local id = tonumber(k); if id >= tonumber(ARGV[1]) and id" + bound
                + "tonumber(ARGV[2]) then n = n + 1 end end return n' 1 '" + fixture.key("batch") + "' ? ?";
    }

    @Override
    protected String literalInsertCommand(int id, String value) {
        return "HSET '" + fixture.key("batch") + "' " + id + " '" + value + "'";
    }

    @Override
    protected String literalUpdateCommand(int id, String value) {
        return literalInsertCommand(id, value);
    }
}
