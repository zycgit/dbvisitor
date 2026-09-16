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
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuid32User;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuid36User;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuidStringUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class UuidKeyCase extends KeyGenerationSupport {
    // 能力归属：对象映射 / 主键策略 / UUID 主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_UUID32, column = "mapping-keys/key-generators/strategies")
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
        KeyUuid32User stored = lambdaTemplate.query(KeyUuid32User.class)//
                .eq(KeyUuid32User::getId, user.getId())//
                .queryForObject();
        assertNotNull(stored);
        assertEquals(user.getName(), stored.getName());
        assertEquals(user.getAge(), stored.getAge());
    }

    // 能力归属：对象映射 / 主键策略 / UUID 主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_UUID32_UNIQUE, column = "mapping-keys/key-generators/strategies")
    public void keygenUuid32_shouldGenerateUniqueValues() throws SQLException {
        KeyUuid32User first = uuid32User(770033, 36);
        KeyUuid32User second = uuid32User(770034, 37);

        lambdaTemplate.insert(KeyUuid32User.class).applyEntity(first).executeSumResult();
        lambdaTemplate.insert(KeyUuid32User.class).applyEntity(second).executeSumResult();

        assertNotNull(first.getName());
        assertNotNull(second.getName());
        assertNotEquals(first.getName(), second.getName());
        assertEquals(first.getName(), lambdaTemplate.query(KeyUuid32User.class)//
                .eq(KeyUuid32User::getId, first.getId())//
                .queryForObject().getName());
        assertEquals(second.getName(), lambdaTemplate.query(KeyUuid32User.class)//
                .eq(KeyUuid32User::getId, second.getId())//
                .queryForObject().getName());
    }

    // 能力归属：对象映射 / 主键策略 / UUID 主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_UUID36, column = "mapping-keys/key-generators/strategies")
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
        KeyUuid36User stored = lambdaTemplate.query(KeyUuid36User.class)//
                .eq(KeyUuid36User::getId, user.getId())//
                .queryForObject();
        assertNotNull(stored);
        assertEquals(user.getName(), stored.getName());
        assertEquals(user.getAge(), stored.getAge());
    }

    // 能力归属：对象映射 / 主键策略 / UUID 主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_UUID36_STRING_ID, column = "mapping-keys/key-generators/strategies")
    public void keygenUuid36_shouldPopulateStringPrimaryKey() throws SQLException {
        ensureUuidStringTable();

        KeyUuidStringUser user = new KeyUuidStringUser();
        user.setName("String ID User");
        user.setAge(70);
        user.setCreateTime(new Date());

        assertEquals(1, lambdaTemplate.insert(KeyUuidStringUser.class).applyEntity(user).executeSumResult());
        assertNotNull(user.getId());
        assertEquals(36, user.getId().length());
        assertTrue(user.getId().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        KeyUuidStringUser stored = lambdaTemplate.query(KeyUuidStringUser.class)//
                .eq(KeyUuidStringUser::getId, user.getId())//
                .queryForObject();
        assertNotNull(stored);
        assertEquals(user.getId(), stored.getId());
        assertEquals("String ID User", stored.getName());
        assertEquals(Integer.valueOf(70), stored.getAge());
    }

}
