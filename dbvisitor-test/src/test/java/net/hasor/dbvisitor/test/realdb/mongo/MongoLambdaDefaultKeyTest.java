/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaDefaultKeyCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoDefaultKeyEntity;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;
import org.junit.After;
import org.junit.Before;

public class MongoLambdaDefaultKeyTest extends LambdaDefaultKeyCase {
    private final MongoEntityFixture fixture = new MongoEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(MongoDefaultKeyEntity.class, "{name: 1}");
        this.lambdaTemplate = this.fixture.lambda();
    }

    @Override
    protected Class<?> defaultKeyEntityType() {
        return MongoDefaultKeyEntity.class;
    }

    @Override
    protected Object newDefaultKeyEntity() {
        MongoDefaultKeyEntity entity = new MongoDefaultKeyEntity();
        entity.setName("NXN-Lambda-Entity-Auto-Id");
        return entity;
    }

    @Override
    protected Object defaultKeyValue(Object entity) {
        return ((MongoDefaultKeyEntity) entity).getId();
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
