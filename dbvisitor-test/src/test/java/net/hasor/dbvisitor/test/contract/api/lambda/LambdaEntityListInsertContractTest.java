/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaEntityListInsertContractTest extends LambdaCrudSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_ENTITY_CRUD_BATCH_INSERT)
    public void lambdaEntityBatchInsert_shouldPersistEntityList() throws SQLException {
        List<UserInfo> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(user(baseId() + 20 + i, "NXN-Lambda-Entity-Batch-" + i, 20 + i, "nxn-lambda-entity-batch-" + i + "@test.com"));
        }

        int rows = lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(users)//
                .executeSumResult();
        long count = countInsertedUsers(users);

        assertEquals(10, rows);
        assertEquals(10, count);
    }

    protected long countInsertedUsers(List<UserInfo> users) throws SQLException {
        return lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Lambda-Entity-Batch-%")//
                .queryForCount();
    }
}
