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
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class LambdaPageOffsetContractTest extends LambdaQuerySupport {
    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_PAGE)
    public void lambdaQueryPage_shouldLimitAndOffsetResults() throws SQLException {
        for (int i = 1; i <= 12; i++) {
            insertByJdbc(baseId() + 60 + i, "PageQ" + i, 20 + i, "page" + i + "@test.com");
        }

        List<UserInfo> users = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 61, baseId() + 72)//
                .orderBy("id")//
                .initPage(5, 1)//
                .queryForList();

        assertNotNull(users);
        assertEquals(5, users.size());
        assertEquals(Integer.valueOf(baseId() + 66), users.get(0).getId());
        assertEquals(Integer.valueOf(baseId() + 70), users.get(4).getId());
    }
}
