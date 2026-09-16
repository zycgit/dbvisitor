/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class LambdaAggregateCase extends LambdaSelectSupport {

    protected String groupCountSelect() {
        return "age, count(*) as cnt";
    }

    protected String aggregateAgeSelect(String function) {
        return function + "(age)";
    }

    // 能力归属：构造器 API / 分组。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SELECT_GROUP_BY, column = "builder/grouping-and-ordering/grouping")
    public void lambdaSelect_shouldGroupRowsAndReturnAggregateMapResults() throws Exception {
        insert(baseId() + 40, "GroupOne", 10, "group1@nxn.test");
        insert(baseId() + 41, "GroupTwo", 20, "group2@nxn.test");
        insert(baseId() + 42, "GroupThree", 20, "group3@nxn.test");
        insert(baseId() + 43, "GroupFour", 30, "group4@nxn.test");

        List<Map<String, Object>> result = lambdaTemplate.query(UserInfo.class)//
                .applySelect(groupCountSelect())//
                .like(UserInfo::getName, "Group%")//
                .groupBy("age")//
                .orderBy("age")//
                .queryForMapList();

        assertEquals(3, result.size());
        Map<String, Object> group20 = findByInt(result, "age", 20);
        assertNotNull(group20);
        assertEquals(2, ((Number) getVal(group20, "cnt")).intValue());
    }

    // 能力归属：构造器 API / 分组。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SELECT_AGGREGATE, column = "builder/grouping-and-ordering/grouping")
    public void lambdaSelect_shouldReturnAggregateScalarValues() throws Exception {
        insert(baseId() + 50, "AggregateOne", 10, "agg1@nxn.test");
        insert(baseId() + 51, "AggregateTwo", 20, "agg2@nxn.test");

        Long sumAge = lambdaTemplate.query(UserInfo.class)//
                .applySelect(aggregateAgeSelect("sum"))//
                .like(UserInfo::getName, "Aggregate%")//
                .queryForObject(Long.class);
        assertEquals(30L, sumAge.longValue());

        Integer maxAge = lambdaTemplate.query(UserInfo.class)//
                .applySelect(aggregateAgeSelect("max"))//
                .like(UserInfo::getName, "Aggregate%")//
                .queryForObject(Integer.class);
        assertEquals(20, maxAge.intValue());
    }

    // 能力归属：构造器 API / 分组。
    @Test
    @Capability(value = CapabilityId.LAMBDA_RESULT_AGGREGATE_SCALAR, column = "builder/grouping-and-ordering/grouping")
    public void lambdaResult_shouldMapAggregateScalarValue() throws SQLException {
        insertUsers("LRMap", new int[] { 21, 22, 23, 24, 25 }, baseId() + 10);
        Integer maxAge = queryRows("LRMap")//
                .applySelect(aggregateAgeSelect("MAX"))//
                .queryForObject(Integer.class);
        assertEquals(Integer.valueOf(25), maxAge);
    }

    // 能力归属：构造器 API / 分组。
    @Test
    @Capability(value = CapabilityId.LAMBDA_RESULT_AGGREGATE_ROW_MAPPER, column = "builder/grouping-and-ordering/grouping")
    public void lambdaResult_shouldMapServerAggregateRows() throws SQLException {
        insertByJdbc(baseId() + 161, "LRGroup1", 30, "lr-group1@test.com");
        insertByJdbc(baseId() + 162, "LRGroup2", 30, "lr-group2@test.com");
        insertByJdbc(baseId() + 163, "LRGroup3", 31, "lr-group3@test.com");
        RowMapper<AgeGroup> groupMapper = (rs, rowNum) -> new AgeGroup(rs.getInt("age"), rs.getLong("cnt"));
        List<AgeGroup> groups = orderRows(queryRows("LRGroup")//
                .applySelect(groupCountSelect())//
                .groupBy("age"), "age")//
                .queryForList(groupMapper);
        assertEquals(2, groups.size());
        assertEquals(Integer.valueOf(30), groups.get(0).age());
        assertEquals(Long.valueOf(2), groups.get(0).count());
    }

    // 能力归属：构造器 API / 分组。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EMPTY_GROUP_BY, column = "builder/grouping-and-ordering/grouping")
    public void lambdaGroupBy_shouldReturnEmptyListWhenNoRowsMatch() throws SQLException {
        List<Map<String, Object>> result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 31)//
                .applySelect(groupCountSelect())//
                .groupBy("age")//
                .queryForMapList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // 能力归属：构造器 API / 分组。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EMPTY_AGGREGATE, column = "builder/grouping-and-ordering/grouping")
    public void lambdaAggregate_shouldReturnCountZeroAndNullMaxWhenNoRowsMatch() throws SQLException {
        Long countResult = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 41)//
                .applySelect(countSelect())//
                .queryForObject(Long.class);
        Integer maxResult = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 41)//
                .applySelect(maxAgeSelect())//
                .queryForObject(Integer.class);

        assertNotNull(countResult);
        assertEquals(Long.valueOf(0), countResult);
        assertNull(maxResult);
    }

    protected String countSelect() {
        return "count(*)";
    }

    protected String maxAgeSelect() {
        return "max(age)";
    }
}
