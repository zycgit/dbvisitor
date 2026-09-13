/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperProjectionResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisAnnotationMapperProjectionResultTest extends AnnotationMapperProjectionResultCase {
    private final RedisMapperFixture fixture = new RedisMapperFixture();
    private AggregateMapper aggregates;
    private String members;
    private String first;
    private String second;

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        fixture.open();
    }

    @Override
    public void createAnnotationMapper() throws Exception {
        fixture.open();
        aggregates = fixture.session().createMapper(AggregateMapper.class);
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }

    @Override
    protected List<Integer> distinctValues() throws Exception {
        String key = fixture.key("distinct");
        fixture.session().jdbc().executeUpdate("SADD ? 23 23 28", key);
        List<Integer> values = fixture.session().createMapper(RedisCoverageMapper.class).distinct(key);
        return values;
    }

    @Override
    protected List<Integer> expectedDistinctValues() { return Arrays.asList(23, 28); }

    @Override
    protected void prepareAggregateRows() throws Exception {
        members = fixture.key("members");
        first = fixture.key("scores-first");
        second = fixture.key("scores-second");
        fixture.session().jdbc().executeUpdate("SADD ? alice bob alice", members);
        fixture.session().jdbc().queryForObject("ZADD ? 10 alice 20 bob", first, Long.class);
        fixture.session().jdbc().queryForObject("ZADD ? 3 alice 40 bob", second, Long.class);
    }

    @Override
    protected Number aggregateScalar() { return aggregates.count(members); }
    @Override
    protected double minimumAggregateScalar() { return 2; }
    @Override
    protected Number expectedAggregateScalar() { return 2; }
    @Override
    protected Map<String, Object> aggregateMap() { return aggregates.countRow(members); }
    @Override
    protected List<String> aggregateMapColumns() { return List.of("RESULT"); }
    @Override
    protected Map<String, Object> expectedAggregateMap() { return Map.of("RESULT", 2); }
    @Override
    protected List<List<Map<String, Object>>> aggregateGroups() {
        return List.of(aggregates.sum(first, second), aggregates.min(first, second), aggregates.max(first, second));
    }
    @Override
    protected List<String> aggregateGroupColumns() { return List.of("ELEMENT", "SCORE"); }
    @Override
    protected List<List<Map<String, Object>>> expectedAggregateGroups() {
        return List.of(
                List.of(Map.of("ELEMENT", "alice", "SCORE", 13.0), Map.of("ELEMENT", "bob", "SCORE", 60.0)),
                List.of(Map.of("ELEMENT", "alice", "SCORE", 3.0), Map.of("ELEMENT", "bob", "SCORE", 20.0)),
                List.of(Map.of("ELEMENT", "alice", "SCORE", 10.0), Map.of("ELEMENT", "bob", "SCORE", 40.0)));
    }

    @SimpleMapper
    public interface AggregateMapper {
        @Query("SCARD #{key}")
        Integer count(@Param("key") String key);

        @Query("SCARD #{key}")
        Map<String, Object> countRow(@Param("key") String key);

        @Query("ZUNION 2 #{first} #{second} AGGREGATE SUM WITHSCORES")
        List<Map<String, Object>> sum(@Param("first") String first, @Param("second") String second);

        @Query("ZUNION 2 #{first} #{second} AGGREGATE MIN WITHSCORES")
        List<Map<String, Object>> min(@Param("first") String first, @Param("second") String second);

        @Query("ZUNION 2 #{first} #{second} AGGREGATE MAX WITHSCORES")
        List<Map<String, Object>> max(@Param("first") String first, @Param("second") String second);
    }
}
