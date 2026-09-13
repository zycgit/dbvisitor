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
    protected Class<?> defaultKeyEntityType() {
        return UserInfo.class;
    }

    protected Object newDefaultKeyEntity() {
        return user(null, "NXN-Lambda-Entity-Auto-Id", 40, "nxn-lambda-entity-auto-id@test.com");
    }

    protected Object defaultKeyValue(Object entity) {
        return ((UserInfo) entity).getId();
    }

    private <T> int insertDefaultKeyEntity(Class<T> type, Object entity) throws SQLException {
        return lambdaTemplate.insert(type).applyEntity(type.cast(entity)).executeSumResult();
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ENTITY_CRUD_DEFAULT_KEY)
    public void lambdaEntityInsert_shouldUseDefaultPrimaryKey() throws SQLException {
        Object autoIdUser = newDefaultKeyEntity();
        int autoRows = insertDefaultKeyEntity(defaultKeyEntityType(), autoIdUser);
        Object loaded = lambdaTemplate.query(defaultKeyEntityType())//
                .eq("name", "NXN-Lambda-Entity-Auto-Id")//
                .queryForObject();

        assertEquals(1, autoRows);
        assertNotNull(loaded);
        assertNotNull(defaultKeyValue(loaded));
    }
}
