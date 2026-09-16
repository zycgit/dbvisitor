/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcMapQueryCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisJdbcMapQueryTest extends JdbcMapQueryCase {

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
    protected void insertUser(int id, String name, int age, String email, Date created) throws SQLException {
        jdbcTemplate.queryForLong("ZADD ? ? ?", new Object[] { fixture.key("maps"), age, name });
    }

    @Override
    protected String selectById(String columns, String parameter) {
        return "ZRANGE '" + fixture.key("maps") + "' ? 0 WITHSCORES";
    }

    @Override
    protected String selectRange(String columns, String lower, String upper, boolean ordered) {
        return "ZRANGE '" + fixture.key("maps") + "' ? ? WITHSCORES";
    }

    @Override
    protected Object[] mapSingleArguments() {
        return new Object[] { 0 };
    }

    @Override
    protected Object[] mapRangeArguments() {
        return new Object[] { 0, 2 };
    }

    @Override
    protected Map<String, Object> expectedMapRow(int offset) {
        return Map.of("ELEMENT", "NXN-JDBC-Query-" + offset, "SCORE", Double.valueOf(60 + offset));
    }
}
