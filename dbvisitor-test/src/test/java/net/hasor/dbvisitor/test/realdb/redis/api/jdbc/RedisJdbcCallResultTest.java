/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.SQLException;
import java.util.Map;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCallResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisJdbcCallResultTest extends JdbcCallResultCase {
    private final RedisJdbcFixture fixture = new RedisJdbcFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        if (jdbcTemplate == null) {
            jdbcTemplate = fixture.open();
        }
    }

    @Override
    protected void createCallFixture() throws SQLException {
        setup();
        jdbcTemplate.queryForLong("ZADD ? ? ?", new Object[] { fixture.key("call-users"), 25, "ProcAlice" });
    }

    @Override
    protected String callCommand() {
        return "ZRANGEBYSCORE '" + fixture.key("call-users") + "' #{p_id} #{p_id} WITHSCORES";
    }

    @Override
    protected Map<String, Object> callParameters() {
        return Map.of("p_id", 25);
    }

    @Override
    protected String nameColumn() {
        return "ELEMENT";
    }

    @Override
    protected String ageColumn() {
        return "SCORE";
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
