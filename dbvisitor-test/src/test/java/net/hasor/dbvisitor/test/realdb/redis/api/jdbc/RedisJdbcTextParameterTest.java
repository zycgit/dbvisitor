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

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcTextParameterCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;

public class RedisJdbcTextParameterTest extends JdbcTextParameterCase {

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
        jdbcTemplate.queryForLong("ZADD ? ? ?", new Object[] { fixture.key(name), age, name });
    }

    @Override
    protected String fixtureTable() {
        return "ZRANGE";
    }

    @Override
    protected String textLookupValue(String value) {
        return fixture.key(value);
    }

    @Override
    protected String command(JdbcParameterCommand command) {
        switch (command) {
            case SELECT_TEXT_VALUE:
                return "${tableName} #{name} 0 0 WITHSCORES";
            case COUNT_TEXT_VALUE:
                return "ZCARD #{name}";
            default:
                throw new IllegalArgumentException("Unexpected text fixture command: " + command);
        }
    }

    @Override
    protected Object value(Map<String, Object> row, String field) {
        return super.value(row, "age".equals(field) ? "SCORE" : "ELEMENT");
    }
}
