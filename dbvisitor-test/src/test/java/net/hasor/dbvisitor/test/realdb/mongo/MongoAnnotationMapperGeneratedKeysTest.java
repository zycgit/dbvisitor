/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperGeneratedKeysCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoKeyFixture;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoKeyMapper;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoKeyRecord;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MongoAnnotationMapperGeneratedKeysTest extends AnnotationMapperGeneratedKeysCase {
    private final MongoKeyFixture fixture = new MongoKeyFixture();
    private MongoKeyMapper keyMapper;

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        this.keyMapper = this.fixture.session(newConfiguration()).createMapper(MongoKeyMapper.class);
    }

    @Override
    protected boolean numericGeneratedKeys() {
        return false;
    }

    @Override
    protected Object keyRecord(Object id, String name, int age, String email) {
        MongoKeyRecord record = new MongoKeyRecord();
        record.setId((String) id);
        record.setName(name);
        return record;
    }

    @Override
    protected Object keyValue(Object record) {
        return ((MongoKeyRecord) record).getId();
    }

    @Override
    protected Object explicitKey() {
        return "manual-key";
    }

    @Override
    protected int writeKeyRecord(KeyWrite operation, Object value) throws Exception {
        MongoKeyRecord record = (MongoKeyRecord) value;
        return switch (operation) {
            case GENERATED -> this.keyMapper.insertGenerated(record);
            case COLUMN -> this.keyMapper.insertGeneratedColumn(record);
            case EXPLICIT -> this.keyMapper.insertExplicit(record);
            case BEFORE -> {
                this.fixture.reserve(record.getName());
                yield this.keyMapper.selectKeyBefore(record);
            }
            case AFTER -> this.keyMapper.selectKeyAfter(record);
            case OPTIONS -> {
                this.fixture.reserve(record.getName());
                yield this.keyMapper.selectKeyWithOptions(record);
            }
            case RESULT_SET -> throw new UnsupportedOperationException("Mongo insert returns generated keys, not a current result set.");
        };
    }

    @Override
    protected String readKeyName(Object id) throws Exception {
        assertNotNull(id);
        if (!"manual-key".equals(id)) {
            assertGeneratedKey(id);
        }
        return "manual-key".equals(id) ? this.keyMapper.explicitName((String) id) : this.keyMapper.generatedName((String) id);
    }

    @Override
    protected void assertGeneratedKey(Object id) {
        assertNotNull(id);
        assertTrue(id instanceof String);
        assertTrue(ObjectId.isValid((String) id));
    }

    @After
    public void closeFixture() throws Exception {
        this.fixture.close();
    }
}
