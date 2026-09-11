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

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaResultValueContractTest extends LambdaResultHandlingSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_MAP_AND_SCALAR)
    public void lambdaResult_shouldReturnMapListsMapsCountsAndScalars() throws SQLException {
        insertUsers("LRMap", new int[] { 21, 22, 23, 24, 25 }, baseId() + 10);

        List<Map<String, Object>> maps = orderRows(queryRows("LRMap")//
                .between("age", 22, 24), "age")//
                .queryForMapList();
        assertEquals(3, maps.size());
        assertEquals(22, ((Number) getVal(maps.get(0), "age")).intValue());

        Map<String, Object> one = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 11)//
                .queryForMap();
        assertEquals(baseId() + 11, ((Number) getVal(one, "id")).intValue());
        assertEquals("LRMap2", getVal(one, "name"));

        long count = queryRows("LRMap")//
                .ge(UserInfo::getAge, 23)//
                .queryForCount();
        assertEquals(3, count);
    }
}
