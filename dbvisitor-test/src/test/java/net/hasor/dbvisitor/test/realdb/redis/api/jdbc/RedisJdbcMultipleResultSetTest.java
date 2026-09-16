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
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcMultipleResultSetCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisJdbcMultipleResultSetTest extends JdbcMultipleResultSetCase {
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
    protected void seedUsers() throws SQLException {
        this.fixture.seedScores();
    }

    @Override
    protected String literalMultipleCommand() {
        String key = this.fixture.key("scores");
        return "ZRANGE '" + key + "' 0 0\nZRANGE '" + key + "' 1 2";
    }

    @Override
    protected String positionalMultipleCommand() {
        return "ZRANGE ? ? ?\nZRANGE ? ? ?";
    }

    @Override
    protected Object[] positionalParameters() {
        String key = this.fixture.key("scores");
        return new Object[] { key, 0, 1, key, 2, 2 };
    }

    @Override
    protected String namedMultipleCommand() {
        return "ZRANGE :key 0 :end\nZRANGE :key :start 2";
    }

    @Override
    protected Map<String, Object> namedParameters() {
        return Map.of("key", this.fixture.key("scores"), "end", 0, "start", 1);
    }

    @Override
    protected String resultNameColumn() {
        return "ELEMENT";
    }

    @Override
    protected String expectedPositionalName() {
        return "member-3";
    }

    @Override
    protected String ruleMultipleCommand() {
        String key = this.fixture.key("scores");
        String type = mappedResultType().getName();
        return "ZRANGE '" + key + "' 0 0 WITHSCORES @{resultSet,name=youngUsers,javaType=" + type + "}\n" + "ZRANGE '" + key + "' 2 2 WITHSCORES @{resultSet,name=seniorUsers,javaType=" + type + "}";
    }

    @Override
    protected Class<?> mappedResultType() {
        return RedisJdbcFixture.ScoredMember.class;
    }

    @Override
    protected String mappedName(Object row) {
        return ((RedisJdbcFixture.ScoredMember) row).getElement();
    }

    @Override
    protected String expectedMappedName(int index) {
        return "member-" + index;
    }
}
