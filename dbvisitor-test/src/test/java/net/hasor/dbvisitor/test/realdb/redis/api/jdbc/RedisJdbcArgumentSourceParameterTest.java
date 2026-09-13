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
import org.junit.Before;
import org.junit.After;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcArgumentSourceParameterCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;

public class RedisJdbcArgumentSourceParameterTest extends JdbcArgumentSourceParameterCase {

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
        String key = "'" + fixture.key("sources") + "'";
        String insert = "EVAL 'return redis.call(\"HSET\", KEYS[1], ARGV[1] .. \":name\", ARGV[2], "
                + "ARGV[2] .. \":age\", ARGV[3], ARGV[1] .. \":email\", ARGV[4], ARGV[1] .. \":created\", ARGV[5])' 1 " + key;
        switch (command) {
            case INSERT_ARRAY_SOURCE:
                return insert + " :arg0 :arg1 :arg2 :arg3 :arg4";
            case INSERT_COLON:
                return insert + " :id :name :age :email :createTime";
            case INSERT_BRACE:
                return insert + " #{id} #{name} #{age} #{email} #{createTime}";
            case COUNT_SOURCE_NAMES:
                return "EVAL 'local n = 0; for i = 1, 3 do local age = redis.call(\"HGET\", KEYS[1], ARGV[i] .. \":age\"); "
                        + "if age and tonumber(age) > tonumber(ARGV[4]) then n = n + 1 end end return n' 1 "
                        + key + " #{names[0]} #{names[1]} #{names[2]} :minAge";
            case SELECT_EMAIL_BY_ID:
                return "EVAL 'return redis.call(\"HGET\", KEYS[1], ARGV[1] .. \":email\")' 1 " + key + " ?";
            default:
                throw new IllegalArgumentException("Unexpected source fixture command: " + command);
        }
    }
    @Override
    protected boolean parameterWriteReturnsRows() {
        return true;
    }
}
