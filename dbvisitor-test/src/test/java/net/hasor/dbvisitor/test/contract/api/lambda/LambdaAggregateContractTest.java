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

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class LambdaAggregateContractTest extends LambdaSelectSupport {
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
}
