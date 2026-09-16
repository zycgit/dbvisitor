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
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaEntityListInsertCase extends LambdaCrudSupport {
    // 能力归属：构造器 API / 写入操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_ENTITY_CRUD_BATCH_INSERT, column = "builder/inserts-updates-and-deletes/writes")
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
