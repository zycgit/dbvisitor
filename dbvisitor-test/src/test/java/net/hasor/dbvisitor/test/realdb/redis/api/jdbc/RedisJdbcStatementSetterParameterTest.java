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
import net.hasor.dbvisitor.test.contract.feature.parameter.JdbcStatementSetterParameterCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisJdbcStatementSetterParameterTest extends JdbcStatementSetterParameterCase {

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
    protected String command(JdbcParameterCommand command) {
        String key = "'" + fixture.key("parameters") + "'";
        switch (command) {
            case INSERT_POSITIONAL:
                return "EVAL 'return redis.call(\"HSET\", KEYS[1], ARGV[1] .. \":name\", ARGV[2], " + "ARGV[1] .. \":age\", ARGV[3], ARGV[1] .. \":email\", ARGV[4], " + "ARGV[1] .. \":created\", ARGV[5], ARGV[2] .. \":email\", ARGV[4])' 1 " + key + " ? ? ? ? ?";
            case SELECT_EMAIL_BY_ID:
            case SELECT_EMAIL_BY_NAME:
                return "EVAL 'return redis.call(\"HGET\", KEYS[1], ARGV[1] .. \":email\")' 1 " + key + " ?";
            default:
                throw new IllegalArgumentException("Unexpected parameter fixture command: " + command);
        }
    }

    @Override
    protected boolean parameterWriteReturnsRows() {
        return true;
    }
}
