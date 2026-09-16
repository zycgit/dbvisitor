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
import net.hasor.dbvisitor.test.contract.material.model.keygen.*;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class CustomKeyHolderCase extends KeyGenerationSupport {
    // 能力归属：对象映射 / 主键策略 / 自定义主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_HOLDER_BEFORE, column = "mapping-keys/key-generators/strategies")
    public void keygenHolderBefore_shouldUseCustomGeneratedKeyHandler() throws SQLException {
        KeyHolderUser user = new KeyHolderUser();
        user.setAge(40);
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(KeyHolderUser.class).applyEntity(user).executeSumResult();

        assertEquals(1, rows);
        assertEquals(Integer.valueOf(999999), user.getId());
        assertNotNull(user.getName());
        KeyHolderUser stored = lambdaTemplate.query(KeyHolderUser.class)//
                .eq(KeyHolderUser::getId, user.getId())//
                .queryForObject();
        assertNotNull(stored);
        assertEquals(user.getName(), stored.getName());
        assertEquals(Integer.valueOf(40), stored.getAge());
    }

    // 能力归属：对象映射 / 主键策略 / 自定义主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_HOLDER_AFTER, column = "mapping-keys/key-generators/strategies")
    public void keygenHolderAfter_shouldReadDatabaseGeneratedKey() throws SQLException {
        if (numericAfterKey()) {
            requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        }
        ensureAfterTable();

        Object user = newAfterKeyEntity();
        assertEquals(1, insertAfterKeyEntity(afterKeyEntityType(), user));
        Object key = afterKeyValue(user);
        assertNotNull(key);
        assertAfterKey(key);
        assertEquals("After Key User", readAfterKeyName(key));
    }

    protected boolean numericAfterKey() {
        return true;
    }

    protected Class<?> afterKeyEntityType() {
        return KeyHolderAfterUser.class;
    }

    protected Object newAfterKeyEntity() {
        KeyHolderAfterUser user = new KeyHolderAfterUser();
        user.setName("After Key User");
        user.setAge(45);
        user.setCreateTime(new Date());
        return user;
    }

    protected Object afterKeyValue(Object entity) {
        return ((KeyHolderAfterUser) entity).getId();
    }

    protected void assertAfterKey(Object key) {
        assertTrue(((Number) key).longValue() > 0);
    }

    protected String readAfterKeyName(Object key) throws SQLException {
        return jdbcTemplate.queryForString("SELECT name FROM user_keygen_after WHERE id = ?", new Object[] { key });
    }

    private <T> int insertAfterKeyEntity(Class<T> type, Object entity) throws SQLException {
        return lambdaTemplate.insert(type).applyEntity(type.cast(entity)).executeSumResult();
    }

    // 能力归属：对象映射 / 主键策略 / 自定义主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_HOLDER_BOTH, column = "mapping-keys/key-generators/strategies")
    public void keygenHolderBoth_shouldRunBeforeAndAfterWhenBothHooksAreEnabled() throws SQLException {
        deleteUserInfoIds(888888, 777777);

        KeyHolderBothUser user = new KeyHolderBothUser();
        user.setName("Both Key User");
        user.setAge(50);
        user.setCreateTime(new Date());

        lambdaTemplate.insert(KeyHolderBothUser.class).applyEntity(user).executeSumResult();

        assertEquals(Integer.valueOf(777777), user.getId());
        Integer inserted = countStoredKey(888888);
        assertEquals(Integer.valueOf(1), inserted);
        assertEquals(Integer.valueOf(0), countStoredKey(777777));
    }

    // 能力归属：对象映射 / 主键策略 / 自定义主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_HOLDER_CONTEXT, column = "mapping-keys/key-generators/strategies")
    public void keygenHolderContext_shouldExposeMappingContext() throws SQLException {
        KeyHolderContextUser user = new KeyHolderContextUser();
        user.setName("Context Key User");
        user.setAge(55);
        user.setCreateTime(new Date());

        lambdaTemplate.insert(KeyHolderContextUser.class).applyEntity(user).executeSumResult();

        assertNotNull(user.getId());
        assertTrue(user.getId() >= 700000 && user.getId() < 800000);
        assertEquals(Integer.valueOf(1), countStoredKey(user.getId()));
    }

    // 能力归属：对象映射 / 主键策略 / 自定义主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_HOLDER_CONNECTION, column = "mapping-keys/key-generators/strategies")
    public void keygenHolderConnection_shouldUseJdbcConnectionDuringBeforeGeneration() throws SQLException {
        verifyConnectionKey(connectionKeyModel());
    }

    protected NumericKeyModel<?> connectionKeyModel() {
        return new NumericKeyModel<>(KeyHolderConnectionUser.class, (name, age) -> {
            KeyHolderConnectionUser user = new KeyHolderConnectionUser();
            user.setName(name);
            user.setAge(age);
            user.setCreateTime(new Date());
            return user;
        }, KeyHolderConnectionUser::getId, (user, id) -> user.setId(Math.toIntExact(id)));
    }

    private <T> void verifyConnectionKey(NumericKeyModel<T> model) throws SQLException {
        T user = model.factory().apply("Connection Aware User", 60);
        lambdaTemplate.insert(model.type()).applyEntity(user).executeSumResult();
        assertNotNull(model.key().apply(user));
        assertTrue(model.key().apply(user).longValue() > 0);
        assertEquals(1, lambdaTemplate.query(model.type()).asMap()//
                .eq("id", model.key().apply(user))//
                .queryForCount());
    }

    // 能力归属：对象映射 / 主键策略 / 自定义主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_HOLDER_EXCEPTION, column = "mapping-keys/key-generators/strategies")
    public void keygenHolderException_shouldPropagateRuntimeException() throws SQLException {
        KeyHolderFailingUser user = new KeyHolderFailingUser();
        user.setName("Failing Key User");
        user.setAge(60);
        user.setCreateTime(new Date());

        try {
            lambdaTemplate.insert(KeyHolderFailingUser.class).applyEntity(user).executeSumResult();
            fail("Expected custom key holder runtime exception");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Intentional failure") || String.valueOf(e.getCause()).contains("Intentional failure"));
        }
    }

    // 能力归属：对象映射 / 主键策略 / 自定义主键。
    @Test
    @Capability(value = CapabilityId.KEYGEN_HOLDER_SQL_EXCEPTION, column = "mapping-keys/key-generators/strategies")
    public void keygenHolderSqlException_shouldExposeSqlExceptionCause() throws SQLException {
        KeyHolderSqlExceptionUser user = new KeyHolderSqlExceptionUser();
        user.setName("SQL Ex User");
        user.setAge(40);
        user.setCreateTime(new Date());

        try {
            lambdaTemplate.insert(KeyHolderSqlExceptionUser.class).applyEntity(user).executeSumResult();
            fail("Expected custom key holder SQLException");
        } catch (Exception e) {
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof SQLException && cause.getMessage().contains("Intentional SQLException")) {
                    return;
                }
                cause = cause.getCause();
            }
            fail("Expected SQLException cause, got " + e);
        }
    }

    protected Integer countStoredKey(int id) throws SQLException {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { id }, Integer.class);
    }
}
