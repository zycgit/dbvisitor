/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.keygen;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuid32User;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuid36User;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuidStringUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyWrongTypeUuidUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NxnContract
public abstract class UuidKeyCase extends KeyGenerationSupport {
    @Test
    @Capability(CapabilityId.KEYGEN_UUID32)
    public void keygenUuid32_shouldPopulateHexStringKeyField() throws SQLException {
        KeyUuid32User user = new KeyUuid32User();
        user.setId(770032);
        user.setAge(35);
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(KeyUuid32User.class).applyEntity(user).executeSumResult();

        assertEquals(1, rows);
        assertNotNull(user.getName());
        assertEquals(32, user.getName().length());
        assertFalse(user.getName().contains("-"));
        assertTrue(user.getName().matches("[0-9a-f]{32}"));
    }

    @Test
    @Capability(CapabilityId.KEYGEN_UUID32_UNIQUE)
    public void keygenUuid32_shouldGenerateUniqueValues() throws SQLException {
        KeyUuid32User first = uuid32User(770033, 36);
        KeyUuid32User second = uuid32User(770034, 37);

        lambdaTemplate.insert(KeyUuid32User.class).applyEntity(first).executeSumResult();
        lambdaTemplate.insert(KeyUuid32User.class).applyEntity(second).executeSumResult();

        assertNotNull(first.getName());
        assertNotNull(second.getName());
        assertFalse(first.getName().equals(second.getName()));
    }

    @Test
    @Capability(CapabilityId.KEYGEN_UUID36)
    public void keygenUuid36_shouldPopulateStandardUuidStringKeyField() throws SQLException {
        KeyUuid36User user = new KeyUuid36User();
        user.setId(770036);
        user.setAge(36);
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(KeyUuid36User.class).applyEntity(user).executeSumResult();

        assertEquals(1, rows);
        assertNotNull(user.getName());
        assertEquals(36, user.getName().length());
        assertTrue(user.getName().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    @Capability(CapabilityId.KEYGEN_UUID36_STRING_ID)
    public void keygenUuid36_shouldPopulateStringPrimaryKey() throws SQLException {
        ensureUuidStringTable();

        KeyUuidStringUser user = new KeyUuidStringUser();
        user.setName("String ID User");
        user.setAge(70);
        user.setCreateTime(new Date());

        assertEquals(1, lambdaTemplate.insert(KeyUuidStringUser.class).applyEntity(user).executeSumResult());
        assertNotNull(user.getId());
        assertEquals(36, user.getId().length());
    }

    @Test
    @Capability(CapabilityId.KEYGEN_UUID_WRONG_TYPE)
    public void keygenUuid_shouldRejectNonStringTargetField() throws SQLException {
        requiresNxnFeature(FeatureId.KEYGEN_UUID_WRONG_TYPE_REJECTED);
        KeyWrongTypeUuidUser user = new KeyWrongTypeUuidUser();
        user.setName("Wrong Type User");
        user.setAge(35);
        user.setCreateTime(new Date());

        try {
            lambdaTemplate.insert(KeyWrongTypeUuidUser.class).applyEntity(user).executeSumResult();
            fail("Expected UUID generation on Integer field to fail");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }
}
