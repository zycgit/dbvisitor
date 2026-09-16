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
import net.hasor.dbvisitor.lambda.Insert;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyAutoLongUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyAutoUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class DatabaseGeneratedKeyCase extends KeyGenerationSupport {
    // 能力归属：对象映射 / 主键策略 / 自增主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_AUTO_SINGLE, column = "mapping-keys/key-generators/strategies")
    public void keygenAuto_shouldPopulateDatabaseGeneratedKeyOnSingleInsert() throws SQLException {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        ensureAutoTable();
        verifySingleAutoKey(autoKeyModel());
    }

    // 能力归属：对象映射 / 主键策略 / 自增主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_AUTO_BATCH, column = "mapping-keys/key-generators/strategies")
    public void keygenAuto_shouldPopulateGeneratedKeysOnBatchInsert() throws SQLException {
        requiresNxnFeature(FeatureId.KEYGEN_AUTO_BATCH_EXPLICIT_NULL);
        ensureAutoTable();
        verifyBatchAutoKeys(autoKeyModel());
    }

    // 能力归属：对象映射 / 主键策略 / 自增主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_AUTO_MANUAL, column = "mapping-keys/key-generators/strategies")
    public void keygenAuto_shouldKeepManuallyAssignedPrimaryKey() throws SQLException {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        ensureAutoTable();
        allowExplicitAutoId();
        verifyExplicitAutoKey(autoKeyModel());
    }

    // 能力归属：对象映射 / 主键策略 / 自增主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_AUTO_LONG, column = "mapping-keys/key-generators/strategies")
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
        KeyAutoLongUser stored = lambdaTemplate.query(KeyAutoLongUser.class)//
                .eq(KeyAutoLongUser::getId, user.getId())//
                .queryForObject();
        assertNotNull(stored);
        assertEquals(user.getId(), stored.getId());
        assertEquals("Long ID User", stored.getName());
        assertEquals(Integer.valueOf(65), stored.getAge());
    }

    protected NumericKeyModel<?> autoKeyModel() {
        return new NumericKeyModel<>(KeyAutoUser.class, this::autoUser, KeyAutoUser::getId, (user, id) -> user.setId(Math.toIntExact(id)));
    }

    protected void allowExplicitAutoId() throws SQLException {
        // Most identity schemas already allow an explicit key.
    }

    protected long readAutoKey(long id) throws SQLException {
        return jdbcTemplate.queryForObject("SELECT id FROM user_keygen_auto WHERE id = ?", new Object[] { id }, Long.class);
    }

    protected String readAutoKeyName(long id) throws SQLException {
        return jdbcTemplate.queryForString("SELECT name FROM user_keygen_auto WHERE id = ?", new Object[] { id });
    }

    private <T> void verifySingleAutoKey(NumericKeyModel<T> model) throws SQLException {
        T user = model.factory().apply("Auto Key User", 30);
        assertEquals(1, lambdaTemplate.insert(model.type()).applyEntity(user).executeSumResult());
        assertNotNull(model.key().apply(user));
        assertTrue(model.key().apply(user).longValue() > 0);
        assertEquals(model.key().apply(user).longValue(), readAutoKey(model.key().apply(user).longValue()));
        assertEquals("Auto Key User", readAutoKeyName(model.key().apply(user).longValue()));
    }

    private <T> void verifyBatchAutoKeys(NumericKeyModel<T> model) throws SQLException {
        T first = model.factory().apply("Auto Batch 1", 31);
        T second = model.factory().apply("Auto Batch 2", 32);
        T third = model.factory().apply("Auto Batch 3", 33);
        Insert<T> insert = lambdaTemplate.insert(model.type());
        assertEquals(3, insert.applyEntity(first).applyEntity(second).applyEntity(third).executeSumResult());
        assertNotNull(model.key().apply(first));
        assertNotNull(model.key().apply(second));
        assertNotNull(model.key().apply(third));
        assertNotEquals(model.key().apply(first), model.key().apply(second));
        assertNotEquals(model.key().apply(second), model.key().apply(third));
        assertNotEquals(model.key().apply(first), model.key().apply(third));
        assertEquals("Auto Batch 1", readAutoKeyName(model.key().apply(first).longValue()));
        assertEquals("Auto Batch 2", readAutoKeyName(model.key().apply(second).longValue()));
        assertEquals("Auto Batch 3", readAutoKeyName(model.key().apply(third).longValue()));
    }

    private <T> void verifyExplicitAutoKey(NumericKeyModel<T> model) throws SQLException {
        T user = model.factory().apply("Manual Auto User", 67);
        model.assignKey().accept(user, 54321L);
        assertEquals(1, lambdaTemplate.insert(model.type()).applyEntity(user).executeSumResult());
        assertEquals(54321L, model.key().apply(user).longValue());
        assertEquals(54321L, readAutoKey(54321L));
    }
}
