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
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyNoneStrictUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyNoneUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class AssignedKeyCase extends KeyGenerationSupport {
    // 能力归属：对象映射 / 主键策略 / 手工主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_NONE_MANUAL, column = "mapping-keys/key-generators/strategies")
    public void keygenNone_shouldUseManuallyAssignedPrimaryKey() throws SQLException {
        ensureStrictNoneTable();

        KeyNoneStrictUser user = new KeyNoneStrictUser();
        user.setId(88001);
        user.setName("None Key User");
        user.setAge(25);
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(KeyNoneStrictUser.class).applyEntity(user).executeSumResult();
        Integer count = countStrictKey(88001);

        assertEquals(1, rows);
        assertEquals(Integer.valueOf(88001), user.getId());
        assertEquals(Integer.valueOf(1), count);
    }

    // 能力归属：对象映射 / 主键策略 / 手工主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_NONE_MISSING, column = "mapping-keys/key-generators/strategies")
    public void keygenNone_shouldRejectMissingPrimaryKeyOnStrictTable() throws SQLException {
        requiresNxnFeature(FeatureId.NON_NULL_PRIMARY_KEY_REJECTED);

        ensureStrictNoneTable();

        KeyNoneStrictUser user = new KeyNoneStrictUser();
        user.setName("None Missing User");
        user.setAge(26);
        user.setCreateTime(new Date());

        try {
            lambdaTemplate.insert(KeyNoneStrictUser.class).applyEntity(user).executeSumResult();
            fail("Expected missing primary key to be rejected");
        } catch (Exception e) {
            assertTrue(e.getMessage(), isNotNullMessage(e));
        }
    }

    // 能力归属：对象映射 / 主键策略 / 手工主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_DUPLICATE_KEY, column = "mapping-keys/key-generators/strategies")
    public void keygenNone_shouldPropagateDuplicatePrimaryKeyError() throws SQLException {
        requiresNxnFeature(FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED);

        deleteDuplicateKey(66666);
        KeyNoneUser first = noneUser(66666, "Duplicate User 1", 30);
        KeyNoneUser second = noneUser(66666, "Duplicate User 2", 31);

        assertEquals(1, lambdaTemplate.insert(KeyNoneUser.class).applyEntity(first).executeSumResult());
        try {
            lambdaTemplate.insert(KeyNoneUser.class).applyEntity(second).executeSumResult();
            fail("Expected duplicate primary key error");
        } catch (Exception e) {
            assertTrue(e.getMessage(), isDuplicateKeyMessage(e));
        }
        KeyNoneUser stored = lambdaTemplate.query(KeyNoneUser.class)//
                .eq(KeyNoneUser::getId, 66666)//
                .queryForObject();
        assertNotNull(stored);
        assertEquals("Duplicate User 1", stored.getName());
        assertEquals(Integer.valueOf(30), stored.getAge());
    }

    protected Integer countStrictKey(int id) throws SQLException {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_strict_none WHERE id = ?", new Object[] { id }, Integer.class);
    }

    protected void deleteDuplicateKey(int id) throws SQLException {
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = ?", new Object[] { id });
    }
}
