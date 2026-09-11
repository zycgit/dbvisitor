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
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperGeneratedKeysContractTest;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyAutoLongUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Same key backfill and explicit-ID assertions, using the native Int64 entity field. */
public class MilvusAnnotationMapperGeneratedKeysContractTest extends AnnotationMapperGeneratedKeysContractTest {
    private final MilvusCapabilityFixture fixture = new MilvusCapabilityFixture();
    private Session session;
    private MilvusCapabilityMappers.GeneratedKeys keys;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        if (this.jdbcTemplate == null) {
            this.jdbcTemplate = this.fixture.open();
            this.fixture.userTable("user_info", "id INT64 PRIMARY KEY AUTO_ID");
            // The contract checks key backfill on/off, not mixed key modes in one collection.
            this.fixture.userTable("user_manual", "id INT64 PRIMARY KEY");
        }
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        setup();
        this.session = newConfiguration().newSession(this.jdbcTemplate.getConnection());
        this.keys = this.session.createMapper(MilvusCapabilityMappers.GeneratedKeys.class);
    }

    @Override
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_GENERATED_KEYS)
    public void annotationAttributes_shouldPopulateGeneratedKeysAndSupportExplicitIds() throws Exception {
        KeyAutoLongUser generated = nativeUser("AttrGeneratedKey", 31);
        assertNull(generated.getId());
        assertEquals(1, this.keys.generated(generated));
        assertNotNull(generated.getId());
        assertTrue(generated.getId() > 0);
        assertEquals("AttrGeneratedKey", this.keys.load(generated.getId()).getName());

        long explicitId = explicitId(101);
        KeyAutoLongUser explicit = nativeUser("AttrManualKey", 32);
        explicit.setId(explicitId);
        assertEquals(1, this.keys.explicit(explicit));
        assertEquals(Long.valueOf(explicitId), explicit.getId());
        assertEquals("AttrManualKey", this.keys.loadExplicit(explicitId).getName());
    }

    private KeyAutoLongUser nativeUser(String name, int age) {
        KeyAutoLongUser user = new KeyAutoLongUser();
        user.setName(name);
        user.setAge(age);
        user.setCreateTime(new Date());
        return user;
    }

    @After
    public void cleanupFixture() throws Exception {
        try {
            this.fixture.close();
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
}
