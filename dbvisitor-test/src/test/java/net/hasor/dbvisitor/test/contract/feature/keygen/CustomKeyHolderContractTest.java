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

import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderAfterUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderBothUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderConnectionUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderContextUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderFailingUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderSqlExceptionUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NxnContract
public abstract class CustomKeyHolderContractTest extends KeyGenerationSupport {
    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_BEFORE)
    public void keygenHolderBefore_shouldUseCustomGeneratedKeyHandler() throws SQLException {
        KeyHolderUser user = new KeyHolderUser();
        user.setAge(40);
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(KeyHolderUser.class).applyEntity(user).executeSumResult();

        assertEquals(1, rows);
        assertEquals(Integer.valueOf(999999), user.getId());
        assertNotNull(user.getName());
    }

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_AFTER)
    public void keygenHolderAfter_shouldReadDatabaseGeneratedKey() throws SQLException {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        ensureAfterTable();

        KeyHolderAfterUser user = new KeyHolderAfterUser();
        user.setName("After Key User");
        user.setAge(45);
        user.setCreateTime(new Date());

        assertEquals(1, lambdaTemplate.insert(KeyHolderAfterUser.class).applyEntity(user).executeSumResult());
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_BOTH)
    public void keygenHolderBoth_shouldRunBeforeAndAfterWhenBothHooksAreEnabled() throws SQLException {
        deleteUserInfoIds(888888, 777777);

        KeyHolderBothUser user = new KeyHolderBothUser();
        user.setName("Both Key User");
        user.setAge(50);
        user.setCreateTime(new Date());

        lambdaTemplate.insert(KeyHolderBothUser.class).applyEntity(user).executeSumResult();

        assertEquals(Integer.valueOf(777777), user.getId());
        Integer inserted = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { 888888 }, Integer.class);
        assertEquals(Integer.valueOf(1), inserted);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_CONTEXT)
    public void keygenHolderContext_shouldExposeMappingContext() throws SQLException {
        KeyHolderContextUser user = new KeyHolderContextUser();
        user.setName("Context Key User");
        user.setAge(55);
        user.setCreateTime(new Date());

        lambdaTemplate.insert(KeyHolderContextUser.class).applyEntity(user).executeSumResult();

        assertNotNull(user.getId());
        assertTrue(user.getId() >= 700000 && user.getId() < 800000);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_CONNECTION)
    public void keygenHolderConnection_shouldUseJdbcConnectionDuringBeforeGeneration() throws SQLException {
        KeyHolderConnectionUser user = new KeyHolderConnectionUser();
        user.setName("Connection Aware User");
        user.setAge(60);
        user.setCreateTime(new Date());

        lambdaTemplate.insert(KeyHolderConnectionUser.class).applyEntity(user).executeSumResult();

        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_EXCEPTION)
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

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_SQL_EXCEPTION)
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
}
