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

@NxnContract
public abstract class LambdaDistinctCase extends LambdaSelectSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_SELECT_DISTINCT)
    public void lambdaSelect_shouldApplyDistinctToSingleAndMultipleColumns() throws Exception {
        insert(baseId() + 20, "DistinctOne", 20, "same@nxn.test");
        insert(baseId() + 21, "DistinctTwo", 20, "same@nxn.test");
        insert(baseId() + 22, "DistinctThree", 25, "same@nxn.test");
        insert(baseId() + 23, "DistinctFour", 25, "other@nxn.test");
        insert(baseId() + 24, "DistinctFive", 30, "other@nxn.test");

        List<Map<String, Object>> ages = lambdaTemplate.query(UserInfo.class)//
                .applySelect("distinct age")//
                .like(UserInfo::getName, "Distinct%")//
                .orderBy("age")//
                .queryForMapList();

        assertEquals(3, ages.size());
        assertEquals(20, ((Number) getVal(ages.get(0), "age")).intValue());
        assertEquals(25, ((Number) getVal(ages.get(1), "age")).intValue());
        assertEquals(30, ((Number) getVal(ages.get(2), "age")).intValue());

        List<Map<String, Object>> ageEmail = lambdaTemplate.query(UserInfo.class)//
                .applySelect("distinct age, email")//
                .like(UserInfo::getName, "Distinct%")//
                .queryForMapList();
        assertEquals(4, ageEmail.size());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SELECT_DISTINCT_COUNT)
    public void lambdaSelect_shouldCountDistinctValues() throws Exception {
        insert(baseId() + 30, "DistinctCountOne", 20, "dc1@nxn.test");
        insert(baseId() + 31, "DistinctCountTwo", 20, "dc2@nxn.test");
        insert(baseId() + 32, "DistinctCountThree", 25, "dc3@nxn.test");
        insert(baseId() + 33, "DistinctCountFour", 30, "dc4@nxn.test");

        List<Map<String, Object>> result = lambdaTemplate.query(UserInfo.class)//
                .applySelect("count(distinct age) as distinct_count")//
                .like(UserInfo::getName, "DistinctCount%")//
                .queryForMapList();

        assertEquals(1, result.size());
        assertEquals(3, ((Number) getVal(result.get(0), "distinct_count")).intValue());
    }
}
