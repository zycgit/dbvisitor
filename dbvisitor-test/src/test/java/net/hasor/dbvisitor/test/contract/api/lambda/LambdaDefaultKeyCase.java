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
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class LambdaDefaultKeyCase extends LambdaCrudSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_ENTITY_CRUD_DEFAULT_KEY)
    public void lambdaEntityInsert_shouldUseDefaultPrimaryKey() throws SQLException {
        UserInfo autoIdUser = user(null, "NXN-Lambda-Entity-Auto-Id", 40, "nxn-lambda-entity-auto-id@test.com");
        int autoRows = lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(autoIdUser)//
                .executeSumResult();
        UserInfo loaded = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "NXN-Lambda-Entity-Auto-Id")//
                .queryForObject();

        assertEquals(1, autoRows);
        assertNotNull(loaded);
        assertNotNull(loaded.getId());
    }
}
