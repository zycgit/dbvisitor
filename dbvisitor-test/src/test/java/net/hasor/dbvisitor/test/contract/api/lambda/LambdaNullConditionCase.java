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
public abstract class LambdaNullConditionCase extends LambdaQuerySupport {
    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_NULL)
    public void lambdaQueryNull_shouldMatchNullAndNotNullColumns() throws SQLException {
        insertByJdbc(baseId() + 41, "NullAge", null, "null-age@test.com");
        insertByJdbc(baseId() + 42, "NullEmail", 42, null);
        insertByJdbc(baseId() + 43, "NullNone", 43, "null-none@test.com");

        long nullAge = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 41, baseId() + 43)//
                .isNull(UserInfo::getAge)//
                .queryForCount();
        long nonNullEmail = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 41, baseId() + 43)//
                .isNotNull(UserInfo::getEmail)//
                .queryForCount();

        assertEquals(1, nullAge);
        assertEquals(2, nonNullEmail);
    }
}
