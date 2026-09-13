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
import java.util.LinkedHashMap;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.PairsResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.ColumnMapResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.FilterResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.RowMapperResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.BeanMappingResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.MapMappingResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcResultExtractorContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import static org.junit.Assert.*;

public class RedisJdbcResultExtractorContractTest extends JdbcResultExtractorContractTest {

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
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_CUSTOM)
    public void resultSetExtractor_shouldSupportCustomAggregation() throws SQLException {
        fixture.seedScores();
        Map<String, Integer> rows = jdbcTemplate.query("ZRANGE ? 0 2 WITHSCORES", new Object[] { fixture.key("scores") },
            (ResultSetExtractor<Map<String, Integer>>) rs -> {
                Map<String, Integer> values = new LinkedHashMap<>();
                while (rs.next()) { values.put(rs.getString("ELEMENT"), rs.getInt("SCORE")); }
                return values;
            });
        assertEquals(3, rows.size());
        assertEquals(Integer.valueOf(21), rows.get("member-1"));
        assertEquals(Integer.valueOf(23), rows.get("member-3"));
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_BUILTIN)
    public void resultSetExtractor_shouldSupportBuiltInListExtractors() throws SQLException {
        fixture.seedScores();
        Object[] args = { fixture.key("scores") };
        assertEquals(10, jdbcTemplate.query("ZRANGE ? 0 -1 WITHSCORES", args, new ColumnMapResultSetExtractor()).size());
        RowMapper<RedisJdbcFixture.ScoredMember> mapper = new BeanMappingRowMapper<>(RedisJdbcFixture.ScoredMember.class);
        List<RedisJdbcFixture.ScoredMember> mapped = jdbcTemplate.query("ZRANGE ? 0 0 WITHSCORES", args, new RowMapperResultSetExtractor<>(mapper));
        assertEquals("member-1", mapped.get(0).getElement());
        List<RedisJdbcFixture.ScoredMember> filtered = jdbcTemplate.query("ZRANGE ? 0 -1 WITHSCORES", args, new FilterResultSetExtractor<>(mapper, row -> row.getScore() > 24));
        assertEquals(6, filtered.size());
        assertTrue(filtered.stream().allMatch(row -> row.getScore() > 24));
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_PAIRS)
    public void resultSetExtractor_shouldSupportPairsExtractor() throws SQLException {
        fixture.seedScores();
        Map<String, Double> rows = jdbcTemplate.query("ZRANGE ? 0 2 WITHSCORES", new Object[] { fixture.key("scores") },
            new PairsResultSetExtractor<>(TypeHandlerRegistry.DEFAULT, String.class, Double.class));
        assertEquals(3, rows.size());
        assertEquals(Double.valueOf(21), rows.get("member-1"));
        assertEquals(Double.valueOf(23), rows.get("member-3"));
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_MAPPING)
    public void resultSetExtractor_shouldSupportMappingExtractors() throws SQLException {
        fixture.seedScores();
        Object[] args = { fixture.key("scores") };
        List<RedisJdbcFixture.ScoredMember> beans = jdbcTemplate.query("ZRANGE ? 0 0 WITHSCORES", args,
            new BeanMappingResultSetExtractor<>(RedisJdbcFixture.ScoredMember.class, MappingRegistry.DEFAULT));
        assertEquals("member-1", beans.get(0).getElement());
        List<Map<String, Object>> maps = jdbcTemplate.query("ZRANGE ? 0 0 WITHSCORES", args,
            new MapMappingResultSetExtractor(RedisJdbcFixture.ScoredMember.class, MappingRegistry.DEFAULT));
        assertEquals("member-1", maps.get(0).get("element"));
        assertEquals(Double.valueOf(21), maps.get(0).get("score"));
    }
}
