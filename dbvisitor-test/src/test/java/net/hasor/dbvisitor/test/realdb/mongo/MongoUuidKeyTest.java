/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.feature.keygen.UuidKeyCase;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuid32User;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuid36User;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuidStringUser;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;
import org.junit.After;
import org.junit.Before;

public class MongoUuidKeyTest extends UuidKeyCase {
    private final MongoEntityFixture fixture = new MongoEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(KeyUuid32User.class, "{id: 1}");
        fixture.mapEntity(KeyUuid36User.class);
        fixture.mapEntity(KeyUuidStringUser.class);
        this.lambdaTemplate = fixture.lambda();
    }

    @Override
    protected void ensureUuidStringTable() throws SQLException {
        jdbcTemplate.executeUpdate(fixture.command("remove({})"));
    }

    @After
    public void cleanupFixture() throws SQLException {
        fixture.close();
    }
}
