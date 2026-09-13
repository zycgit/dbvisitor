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
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperGeneratedKeysCase;
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
public class MilvusAnnotationMapperGeneratedKeysTest extends AnnotationMapperGeneratedKeysCase {
    private final MilvusCapabilityFixture fixture = new MilvusCapabilityFixture();
    private Session session;
    private NativeGeneratedKeys keys;

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
        this.keys = this.session.createMapper(NativeGeneratedKeys.class);
    }

    @Override
    protected Object keyRecord(Object id, String name, int age, String email) {
        KeyAutoLongUser user = new KeyAutoLongUser();
        user.setId((Long) id);
        user.setName(name);
        user.setAge(age);
        user.setCreateTime(new Date());
        return user;
    }

    @Override
    protected Object keyValue(Object record) {
        return ((KeyAutoLongUser) record).getId();
    }

    @Override
    protected Object explicitKey() {
        return (long) explicitId(101);
    }

    @Override
    protected int writeKeyRecord(KeyWrite operation, Object record) throws Exception {
        KeyAutoLongUser user = (KeyAutoLongUser) record;
        return switch (operation) {
            case GENERATED -> this.keys.generated(user);
            case COLUMN -> this.keys.generatedWithColumn(user);
            case EXPLICIT -> this.keys.explicit(user);
            default -> throw new UnsupportedOperationException("Milvus does not provide this generated-key source: " + operation);
        };
    }

    @Override
    protected String readKeyName(Object id) throws Exception {
        Long key = (Long) id;
        return explicitKey().equals(key) ? this.keys.loadExplicit(key).getName() : this.keys.load(key).getName();
    }

    @SimpleMapper
    public interface NativeGeneratedKeys extends MilvusCapabilityMappers.GeneratedKeys {
        @Insert(value = "INSERT INTO user_info (name, age, create_time) VALUES (#{name}, #{age}, #{createTime})",
                useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
        int generatedWithColumn(KeyAutoLongUser user);
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
