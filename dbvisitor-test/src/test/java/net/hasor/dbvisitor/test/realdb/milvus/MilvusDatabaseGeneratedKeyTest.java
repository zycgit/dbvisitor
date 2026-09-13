/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.feature.keygen.DatabaseGeneratedKeyCase;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyAutoLongUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** AutoID uses a 64-bit key; preserve the common write/backfill assertions with a Long fixture. */
public class MilvusDatabaseGeneratedKeyTest extends DatabaseGeneratedKeyCase {
    private final MilvusCapabilityFixture fixture = new MilvusCapabilityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @Override
    protected void ensureAutoLongTable() throws SQLException {
        this.fixture.userTable("user_keygen_auto_long", "id INT64 PRIMARY KEY AUTO_ID");
    }

    @Override
    @Test
    @Capability(CapabilityId.KEYGEN_AUTO_SINGLE)
    public void keygenAuto_shouldPopulateDatabaseGeneratedKeyOnSingleInsert() throws SQLException {
        ensureAutoLongTable();
        KeyAutoLongUser user = nativeUser("Auto Key User", 30);
        assertEquals(1, this.lambdaTemplate.insert(KeyAutoLongUser.class).applyEntity(user).executeSumResult());
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
    }

    @Override
    @Test
    @Capability(CapabilityId.KEYGEN_AUTO_BATCH)
    public void keygenAuto_shouldPopulateGeneratedKeysOnBatchInsert() throws SQLException {
        ensureAutoLongTable();
        KeyAutoLongUser first = nativeUser("Auto Batch 1", 31);
        KeyAutoLongUser second = nativeUser("Auto Batch 2", 32);
        KeyAutoLongUser third = nativeUser("Auto Batch 3", 33);
        int rows = this.lambdaTemplate.insert(KeyAutoLongUser.class)
                .applyEntity(first).applyEntity(second).applyEntity(third).executeSumResult();
        assertEquals(3, rows);
        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertNotNull(third.getId());
        assertFalse(first.getId().equals(second.getId()));
        assertFalse(second.getId().equals(third.getId()));
    }

    @Override
    @Test
    @Capability(CapabilityId.KEYGEN_AUTO_MANUAL)
    public void keygenAuto_shouldKeepManuallyAssignedPrimaryKey() throws SQLException {
        ensureAutoLongTable();
        this.jdbcTemplate.execute("ALTER TABLE user_keygen_auto_long SET PROPERTIES (allow_insert_auto_id=true)");
        KeyAutoLongUser user = nativeUser("Manual Auto User", 67);
        user.setId(54321L);
        assertEquals(1, this.lambdaTemplate.insert(KeyAutoLongUser.class).applyEntity(user).executeSumResult());
        Long stored = this.jdbcTemplate.queryForObject("SELECT id FROM user_keygen_auto_long WHERE id = ?", new Object[] { 54321L }, Long.class);
        assertEquals(Long.valueOf(54321), user.getId());
        assertEquals(Long.valueOf(54321), stored);
    }

    private KeyAutoLongUser nativeUser(String name, int age) {
        KeyAutoLongUser user = new KeyAutoLongUser();
        user.setName(name);
        user.setAge(age);
        user.setCreateTime(new Date());
        return user;
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
