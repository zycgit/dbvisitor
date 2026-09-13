/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.util.List;
import java.util.Map;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.RowMapper;
import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class LambdaAggregateCase extends LambdaSelectSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_SELECT_GROUP_BY)
    public void lambdaSelect_shouldGroupRowsAndReturnAggregateMapResults() throws Exception {
        insert(baseId() + 40, "GroupOne", 10, "group1@nxn.test");
        insert(baseId() + 41, "GroupTwo", 20, "group2@nxn.test");
        insert(baseId() + 42, "GroupThree", 20, "group3@nxn.test");
        insert(baseId() + 43, "GroupFour", 30, "group4@nxn.test");

        List<Map<String, Object>> result = lambdaTemplate.query(UserInfo.class)//
                .applySelect("age, count(*) as cnt")//
                .like(UserInfo::getName, "Group%")//
                .groupBy("age")//
                .orderBy("age")//
                .queryForMapList();

        assertEquals(3, result.size());
        Map<String, Object> group20 = findByInt(result, "age", 20);
        assertNotNull(group20);
        assertEquals(2, ((Number) getVal(group20, "cnt")).intValue());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SELECT_AGGREGATE)
    public void lambdaSelect_shouldReturnAggregateScalarValues() throws Exception {
        insert(baseId() + 50, "AggregateOne", 10, "agg1@nxn.test");
        insert(baseId() + 51, "AggregateTwo", 20, "agg2@nxn.test");

        Long sumAge = lambdaTemplate.query(UserInfo.class)//
                .applySelect("sum(age)")//
                .like(UserInfo::getName, "Aggregate%")//
                .queryForObject(Long.class);
        assertEquals(30L, sumAge.longValue());

        Integer maxAge = lambdaTemplate.query(UserInfo.class)//
                .applySelect("max(age)")//
                .like(UserInfo::getName, "Aggregate%")//
                .queryForObject(Integer.class);
        assertEquals(20, maxAge.intValue());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_AGGREGATE_SCALAR)
    public void lambdaResult_shouldMapAggregateScalarValue() throws SQLException {
        insertUsers("LRMap", new int[] { 21, 22, 23, 24, 25 }, baseId() + 10);
        Integer maxAge = queryRows("LRMap")//
                .applySelect("MAX(age)")//
                .queryForObject(Integer.class);
        assertEquals(Integer.valueOf(25), maxAge);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_AGGREGATE_ROW_MAPPER)
    public void lambdaResult_shouldMapServerAggregateRows() throws SQLException {
        insertByJdbc(baseId() + 161, "LRGroup1", 30, "lr-group1@test.com");
        insertByJdbc(baseId() + 162, "LRGroup2", 30, "lr-group2@test.com");
        insertByJdbc(baseId() + 163, "LRGroup3", 31, "lr-group3@test.com");
        RowMapper<AgeGroup> groupMapper = (rs, rowNum) -> new AgeGroup(rs.getInt("age"), rs.getLong("cnt"));
        List<AgeGroup> groups = orderRows(queryRows("LRGroup")//
                .applySelect("age, count(*) as cnt")//
                .groupBy("age"), "age")//
                .queryForList(groupMapper);
        assertEquals(2, groups.size());
        assertEquals(Integer.valueOf(30), groups.get(0).age);
        assertEquals(Long.valueOf(2), groups.get(0).count);
    }
}
