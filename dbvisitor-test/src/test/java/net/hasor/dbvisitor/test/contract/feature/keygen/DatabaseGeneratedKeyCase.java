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

import net.hasor.dbvisitor.lambda.Insert;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyAutoLongUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyAutoUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class DatabaseGeneratedKeyCase extends KeyGenerationSupport {
    @Test
    @Capability(CapabilityId.KEYGEN_AUTO_SINGLE)
    public void keygenAuto_shouldPopulateDatabaseGeneratedKeyOnSingleInsert() throws SQLException {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        ensureAutoTable();

        KeyAutoUser user = new KeyAutoUser();
        user.setName("Auto Key User");
        user.setAge(30);
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(KeyAutoUser.class).applyEntity(user).executeSumResult();

        assertEquals(1, rows);
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_AUTO_BATCH)
    public void keygenAuto_shouldPopulateGeneratedKeysOnBatchInsert() throws SQLException {
        requiresNxnFeature(FeatureId.KEYGEN_AUTO_BATCH_EXPLICIT_NULL);
        ensureAutoTable();

        KeyAutoUser first = autoUser("Auto Batch 1", 31);
        KeyAutoUser second = autoUser("Auto Batch 2", 32);
        KeyAutoUser third = autoUser("Auto Batch 3", 33);

        Insert<KeyAutoUser> insert = lambdaTemplate.insert(KeyAutoUser.class);
        int rows = insert.applyEntity(first).applyEntity(second).applyEntity(third).executeSumResult();

        assertEquals(3, rows);
        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertNotNull(third.getId());
        assertFalse(first.getId().equals(second.getId()));
        assertFalse(second.getId().equals(third.getId()));
    }

    @Test
    @Capability(CapabilityId.KEYGEN_AUTO_MANUAL)
    public void keygenAuto_shouldKeepManuallyAssignedPrimaryKey() throws SQLException {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        ensureAutoTable();

        KeyAutoUser user = autoUser("Manual Auto User", 67);
        user.setId(54321);

        assertEquals(1, lambdaTemplate.insert(KeyAutoUser.class).applyEntity(user).executeSumResult());
        Integer dbId = jdbcTemplate.queryForObject("SELECT id FROM user_keygen_auto WHERE id = ?", new Object[] { 54321 }, Integer.class);

        assertEquals(Integer.valueOf(54321), user.getId());
        assertEquals(Integer.valueOf(54321), dbId);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_AUTO_LONG)
    public void keygenAuto_shouldPopulateLongPrimaryKey() throws SQLException {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        ensureAutoLongTable();

        KeyAutoLongUser user = new KeyAutoLongUser();
        user.setName("Long ID User");
        user.setAge(65);
        user.setCreateTime(new Date());

        assertEquals(1, lambdaTemplate.insert(KeyAutoLongUser.class).applyEntity(user).executeSumResult());
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0L);
    }
}
