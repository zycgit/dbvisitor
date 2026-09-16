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
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class LambdaDistinctCase extends LambdaSelectSupport {

    protected String distinctSelect(String columns) {
        return "distinct " + columns;
    }

    protected String distinctCountSelect() {
        return "count(distinct age) as distinct_count";
    }

    // 能力归属：构造器 API / 查询操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SELECT_DISTINCT, column = "builder/queries/query")
    public void lambdaSelect_shouldApplyDistinctToSingleAndMultipleColumns() throws Exception {
        insert(baseId() + 20, "DistinctOne", 20, "same@nxn.test");
        insert(baseId() + 21, "DistinctTwo", 20, "same@nxn.test");
        insert(baseId() + 22, "DistinctThree", 25, "same@nxn.test");
        insert(baseId() + 23, "DistinctFour", 25, "other@nxn.test");
        insert(baseId() + 24, "DistinctFive", 30, "other@nxn.test");

        List<Map<String, Object>> ages = lambdaTemplate.query(UserInfo.class)//
                .applySelect(distinctSelect("age"))//
                .like(UserInfo::getName, "Distinct%")//
                .orderBy("age")//
                .queryForMapList();

        assertEquals(3, ages.size());
        assertEquals(20, ((Number) getVal(ages.get(0), "age")).intValue());
        assertEquals(25, ((Number) getVal(ages.get(1), "age")).intValue());
        assertEquals(30, ((Number) getVal(ages.get(2), "age")).intValue());

        List<Map<String, Object>> ageEmail = lambdaTemplate.query(UserInfo.class)//
                .applySelect(distinctSelect("age, email"))//
                .like(UserInfo::getName, "Distinct%")//
                .queryForMapList();
        assertEquals(4, ageEmail.size());
    }

    // 能力归属：构造器 API / 查询操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SELECT_DISTINCT_COUNT, column = "builder/queries/query")
    public void lambdaSelect_shouldCountDistinctValues() throws Exception {
        insert(baseId() + 30, "DistinctCountOne", 20, "dc1@nxn.test");
        insert(baseId() + 31, "DistinctCountTwo", 20, "dc2@nxn.test");
        insert(baseId() + 32, "DistinctCountThree", 25, "dc3@nxn.test");
        insert(baseId() + 33, "DistinctCountFour", 30, "dc4@nxn.test");

        List<Map<String, Object>> result = lambdaTemplate.query(UserInfo.class)//
                .applySelect(distinctCountSelect())//
                .like(UserInfo::getName, "DistinctCount%")//
                .queryForMapList();

        assertEquals(1, result.size());
        assertEquals(3, ((Number) getVal(result.get(0), "distinct_count")).intValue());
    }

    // 能力归属：构造器 API / 查询操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EMPTY_DISTINCT, column = "builder/queries/query")
    public void lambdaDistinct_shouldReturnEmptyListWhenNoRowsMatch() throws SQLException {
        List<Integer> result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 51)//
                .applySelect(distinctSelect())//
                .queryForList(Integer.class);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    protected String distinctSelect() {
        return "distinct id";
    }
}
