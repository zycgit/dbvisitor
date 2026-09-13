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
import java.util.List;
import java.util.Arrays;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.SingleColumnRowMapper;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcRowMapperContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcRowMapperContractTest extends JdbcRowMapperContractTest {

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
    @Capability(CapabilityId.JDBC_RESULT_ROW_MAPPER_CUSTOM)
    public void rowMapper_shouldSupportCustomMappingAndDtoProjection() throws SQLException {
        fixture.seedScores();
        List<String> rows = jdbcTemplate.queryForList("ZRANGE ? 0 2 WITHSCORES", new Object[] { fixture.key("scores") },
            (rs, n) -> rs.getString("ELEMENT").toUpperCase() + ":" + rs.getInt("SCORE"));
        assertEquals(Arrays.asList("MEMBER-1:21", "MEMBER-2:22", "MEMBER-3:23"), rows);
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_RESULT_ROW_MAPPER_BUILTIN)
    public void rowMapper_shouldSupportBuiltInMappers() throws SQLException {
        fixture.seedScores();
        Object[] args = { fixture.key("scores") };
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("ZRANGE ? 0 0 WITHSCORES", args, new ColumnMapRowMapper());
        assertEquals("member-1", maps.get(0).get("ELEMENT"));
        List<String> values = jdbcTemplate.queryForList("ZRANGE ? 0 1", args, new SingleColumnRowMapper<>(String.class));
        assertEquals(Arrays.asList("member-1", "member-2"), values);
        List<RedisJdbcFixture.ScoredMember> beans = jdbcTemplate.queryForList("ZRANGE ? 0 0 WITHSCORES", args, new BeanMappingRowMapper<>(RedisJdbcFixture.ScoredMember.class));
        assertEquals("member-1", beans.get(0).getElement());
        assertEquals(Double.valueOf(21), beans.get(0).getScore());
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_RESULT_IGNORE_FIELD_MAPPING)
    public void beanMapping_shouldHonorIgnoredFields() throws SQLException {
        fixture.seedScores();
        RedisJdbcFixture.IgnoredScore bean = jdbcTemplate.queryForObject("ZRANGE ? 0 0 WITHSCORES",
            new Object[] { fixture.key("scores") }, RedisJdbcFixture.IgnoredScore.class);
        assertEquals("member-1", bean.getElement());
        assertNull(bean.getScore());
    }
}
