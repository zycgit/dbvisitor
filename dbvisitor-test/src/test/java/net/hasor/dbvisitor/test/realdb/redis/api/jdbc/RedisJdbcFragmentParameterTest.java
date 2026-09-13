/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterCommand;
import java.util.Map;
import org.junit.Before;
import org.junit.After;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcFragmentParameterCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;

public class RedisJdbcFragmentParameterTest extends JdbcFragmentParameterCase {

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
    protected void insert(int id, String name, int age, String email) throws SQLException {
        jdbcTemplate.queryForLong("ZADD ? ? ?", new Object[] { fixture.key("fragment"), age, name });
    }

    @Override
    protected String fixtureTable() {
        return fixture.key("fragment");
    }

    @Override
    protected String orderFragment() {
        return "REV WITHSCORES";
    }

    @Override
    protected String command(JdbcParameterCommand command) {
        switch (command) {
            case SELECT_TEXT_ORDER:
                return "ZRANGE ${tableName} 0 1 ${orderBy}";
            case COUNT_TEXT_COLUMN:
                return "EVAL 'if redis.call(\"ZSCORE\", KEYS[1], ARGV[1]) then return 1 else return 0 end' 1 ${tableName} #{name}";
            default:
                throw new IllegalArgumentException("Unexpected fragment fixture command: " + command);
        }
    }

    @Override
    protected Object value(Map<String, Object> row, String field) {
        return super.value(row, "age".equals(field) ? "SCORE" : "ELEMENT");
    }
}
