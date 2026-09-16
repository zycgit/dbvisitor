/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.feature.result.JdbcRowCallbackCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisJdbcRowCallbackTest extends JdbcRowCallbackCase {

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
    protected void insertUser(int id, String name, int age, String email) throws SQLException {
        jdbcTemplate.queryForLong("ZADD ? ? ?", new Object[] { fixture.key("callback"), age, name });
    }

    @Override
    protected String selectSql(String columns, String predicate, boolean ordered) {
        return "ZRANGE '" + fixture.key("callback") + "' ? ? WITHSCORES";
    }

    @Override
    protected String callbackNameColumn() {
        return "ELEMENT";
    }

    @Override
    protected String callbackNumberColumn() {
        return "SCORE";
    }

    @Override
    protected Object[] callbackArguments() {
        return new Object[] { 0, 4 };
    }

    @Override
    protected Object[] emptyResultArguments() {
        return new Object[] { 20, 30 };
    }
}
