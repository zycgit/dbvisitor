/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaCountContractTest extends LambdaQuerySupport {
    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_COUNT)
    public void lambdaQueryCount_shouldReturnMatchingRowCount() throws SQLException {
        insertUsers("Cnt", new int[] { 31, 31, 32, 33, 31 }, baseId() + 80);

        long count = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 80, baseId() + 84)//
                .eq(UserInfo::getAge, 31)//
                .queryForCount();

        assertEquals(3, count);
    }
}
