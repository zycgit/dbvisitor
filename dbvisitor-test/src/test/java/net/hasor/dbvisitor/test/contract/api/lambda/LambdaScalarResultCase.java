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

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaScalarResultCase extends LambdaResultHandlingSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_TYPE_CONVERSION)
    public void lambdaResult_shouldConvertScalarObjectsAndLists() throws SQLException {
        insertUsers("LRConvert", new int[] { 10, 20, 30 }, baseId() + 60);

        Integer ageInt = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 60)//
                .select(UserInfo::getAge)//
                .queryForObject(Integer.class);
        Long ageLong = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 60)//
                .select(UserInfo::getAge)//
                .queryForObject(Long.class);
        String ageString = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 60)//
                .select(UserInfo::getAge)//
                .queryForObject(String.class);
        List<Integer> ages = orderRows(queryRows("LRConvert")//
                .select(UserInfo::getAge), "age")//
                .queryForList(Integer.class);

        assertEquals(Integer.valueOf(10), ageInt);
        assertEquals(Long.valueOf(10), ageLong);
        assertEquals("10", ageString);
        assertEquals(3, ages.size());
        assertEquals(Integer.valueOf(30), ages.get(2));
    }
}
