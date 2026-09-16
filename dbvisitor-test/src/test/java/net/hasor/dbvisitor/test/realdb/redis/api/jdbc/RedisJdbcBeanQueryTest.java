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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcBeanQueryCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisJdbcBeanQueryTest extends JdbcBeanQueryCase {

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
        jdbcTemplate.queryForLong("ZADD ? ? ?", new Object[] { fixture.key("beans"), age, name });
    }

    @Override
    protected String selectRange(String columns, String lower, String upper, boolean ordered) {
        return "ZRANGE '" + fixture.key("beans") + "' ? ? WITHSCORES";
    }

    @Override
    protected Object[] beanRangeArguments() {
        return new Object[] { 0, 2 };
    }

    @Override
    protected Class<?> queryBeanType() {
        return RedisJdbcFixture.ScoredMember.class;
    }

    @Override
    protected Map<String, Object> beanValues(Object bean) {
        RedisJdbcFixture.ScoredMember member = (RedisJdbcFixture.ScoredMember) bean;
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("element", member.getElement());
        values.put("score", member.getScore());
        return values;
    }

    @Override
    protected Map<String, Object> expectedBeanValues(int offset) {
        return Map.of("element", "NXN-JDBC-Query-" + offset, "score", Double.valueOf(60 + offset));
    }

    @Override
    protected List<?> requiredBeanValues(Object bean) {
        return List.of();
    }
}
