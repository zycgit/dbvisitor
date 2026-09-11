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
public abstract class LambdaCalculatedResultContractTest extends LambdaResultHandlingSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_CALCULATED_COLUMN)
    public void lambdaResult_shouldReadCalculatedColumnThroughRowMapper() throws SQLException {
        insertByJdbc(baseId() + 182, "LRCalc", 30, "lr-calc@test.com");
        Integer doubled = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 182)//
                .applySelect("age * 2 as doubled_age")//
                .queryForObject((rs, rowNum) -> rs.getInt("doubled_age"));
        assertEquals(Integer.valueOf(60), doubled);
    }
}
