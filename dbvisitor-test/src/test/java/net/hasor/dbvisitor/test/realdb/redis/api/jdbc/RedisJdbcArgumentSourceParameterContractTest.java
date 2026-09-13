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
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcArgumentSourceParameterContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.dynamic.args.ArraySqlArgSource;
import net.hasor.dbvisitor.dynamic.args.BeanSqlArgSource;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import static org.junit.Assert.*;

public class RedisJdbcArgumentSourceParameterContractTest extends JdbcArgumentSourceParameterContractTest {

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
    @Test
    @Capability(CapabilityId.JDBC_PARAM_ARG_SOURCE)
    public void sqlArgSources_shouldBindArrayBeanAndMapSources() throws SQLException {
        String a = fixture.key("array"), b = fixture.key("bean"), c = fixture.key("map");
        jdbcTemplate.executeUpdate("SET ? ?", new ArraySqlArgSource(new Object[] { a, "array" }));
        RedisJdbcFixture.ScoredMember bean = new RedisJdbcFixture.ScoredMember();
        bean.setElement(b); bean.setScore(25d);
        jdbcTemplate.executeUpdate("SET :element :score", new BeanSqlArgSource(bean));
        jdbcTemplate.executeUpdate("SET :key :value", new MapSqlArgSource(Map.of("key", c, "value", "map")));
        assertEquals("array", jdbcTemplate.queryForString("GET ?", new Object[] { a }));
        assertEquals(Double.valueOf(25), jdbcTemplate.queryForObject("GET ?", new Object[] { b }, Double.class));
        assertEquals("map", jdbcTemplate.queryForString("GET ?", new Object[] { c }));
    }
}
