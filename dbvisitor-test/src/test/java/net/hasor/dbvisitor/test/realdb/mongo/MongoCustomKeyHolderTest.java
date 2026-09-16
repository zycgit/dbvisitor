/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.feature.keygen.CustomKeyHolderCase;
import net.hasor.dbvisitor.test.contract.material.model.keygen.*;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoAfterKeyEntity;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertTrue;

public class MongoCustomKeyHolderTest extends CustomKeyHolderCase {
    private final MongoEntityFixture fixture      = new MongoEntityFixture();
    private final MongoEntityFixture afterFixture = new MongoEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(KeyHolderUser.class, "{id: 1}");
        for (Class<?> type : new Class<?>[] { KeyHolderBothUser.class, KeyHolderContextUser.class, KeyHolderConnectionUser.class, KeyHolderFailingUser.class, KeyHolderSqlExceptionUser.class }) {
            this.fixture.mapEntity(type);
        }
        this.lambdaTemplate = this.fixture.lambda();
    }

    @Override
    protected void deleteUserInfoIds(int first, int second) throws SQLException {
        jdbcTemplate.executeUpdate(this.fixture.command("remove({id: {$in: [?, ?]}})"), new Object[] { first, second });
    }

    @Override
    protected Integer countStoredKey(int id) throws SQLException {
        return jdbcTemplate.queryForInt(this.fixture.command("count({id: ?})"), new Object[] { id });
    }

    @Override
    protected void ensureAfterTable() throws SQLException {
        this.jdbcTemplate = this.afterFixture.open(MongoAfterKeyEntity.class, "{name: 1}");
        this.lambdaTemplate = this.afterFixture.lambda();
    }

    @Override
    protected boolean numericAfterKey() {
        return false;
    }

    @Override
    protected Class<?> afterKeyEntityType() {
        return MongoAfterKeyEntity.class;
    }

    @Override
    protected Object newAfterKeyEntity() {
        MongoAfterKeyEntity entity = new MongoAfterKeyEntity();
        entity.setName("After Key User");
        return entity;
    }

    @Override
    protected Object afterKeyValue(Object entity) {
        return ((MongoAfterKeyEntity) entity).getId();
    }

    @Override
    protected void assertAfterKey(Object key) {
        assertTrue(ObjectId.isValid((String) key));
    }

    @Override
    protected String readAfterKeyName(Object key) throws SQLException {
        return jdbcTemplate.queryForString(this.afterFixture.command("find({_id: ObjectId(?)}, {_id: 0, name: 1})"), new Object[] { key });
    }

    @After
    public void cleanupFixture() throws SQLException {
        try {
            this.afterFixture.close();
        } finally {
            this.fixture.close();
        }
    }
}
