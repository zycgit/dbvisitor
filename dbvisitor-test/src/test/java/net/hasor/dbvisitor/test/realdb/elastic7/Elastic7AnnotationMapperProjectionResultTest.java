/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperProjectionResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import org.junit.After;
import org.junit.Before;

public class Elastic7AnnotationMapperProjectionResultTest extends AnnotationMapperProjectionResultCase {
    private final Elastic7SessionMapperFixture fixture = new Elastic7SessionMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
        for (int i = 1; i <= 10; i++) {
            fixture.insert(baseId() + i, "AnnoResult" + i, 20 + i, "anno-result" + i + "@nxn.test");
        }
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        this.mapper = fixture.session().createMapper(NativeProjection.class);
    }

    @Override
    protected List<Integer> expectedDistinctValues() {
        return List.of(21, 22, 23, 24, 25, 26, 27, 28, 29, 30);
    }

    @Override
    protected Number expectedAggregateScalar() {
        return 30;
    }

    @Override
    protected Map<String, Object> expectedAggregateMap() {
        return Map.of("minAge", 21, "maxAge", 30, "avgAge", 25.5);
    }

    @Override
    protected List<List<Map<String, Object>>> expectedAggregateGroups() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int age = 21; age <= 30; age++) {
            rows.add(Map.of("age", age, "cnt", 1));
        }
        return List.of(rows);
    }

    @After
    public void closeProjectionFixture() throws Exception {
        fixture.close();
    }

    @SimpleMapper
    public interface NativeProjection extends Elastic7SessionMapperFixture.NativeResultMapping {
        @Override
        @Query("""
                @{macro, esSessionPath}/_search {
                  "query": {"wildcard": {"name": #{pattern.replace('%', '*')}}},
                  "aggs": {"rows": {"composite": {"size": 2,
                    "sources": [{"age": {"terms": {"field": "age"}}}]}}}
                }
                """)
        List<Integer> selectDistinctAges(@Param("pattern") String pattern);

        @Override
        @Query("""
                @{macro, esSessionPath}/_search {"aggs": {"maxAge": {"max": {"field": "age"}}}}
                """)
        Integer selectMaxAge();

        @Override
        @Query("""
                @{macro, esSessionPath}/_search {
                  "query": {"wildcard": {"name": #{pattern.replace('%', '*')}}},
                  "aggs": {"minAge": {"min": {"field": "age"}},
                           "maxAge": {"max": {"field": "age"}},
                           "avgAge": {"avg": {"field": "age"}}}
                }
                """)
        Map<String, Object> selectAgeStats(@Param("pattern") String pattern);

        @Override
        @Query("""
                @{macro, esSessionPath}/_search {
                  "query": {"wildcard": {"name": #{pattern.replace('%', '*')}}},
                  "aggs": {"rows": {"composite": {"size": 2,
                    "sources": [{"age": {"terms": {"field": "age", "order": "asc"}}}]},
                    "aggs": {"cnt": {"filter": {"match_all": {}}}}}}
                }
                """)
        List<Map<String, Object>> selectCountByAge(@Param("pattern") String pattern);
    }
}
