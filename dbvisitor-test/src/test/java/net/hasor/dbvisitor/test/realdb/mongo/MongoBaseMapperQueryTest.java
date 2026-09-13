/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperQueryCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;
import net.hasor.dbvisitor.session.Session;
import org.junit.After;
import org.junit.Before;

public class MongoBaseMapperQueryTest extends BaseMapperQueryCase {
    private final MongoEntityFixture fixture = new MongoEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        if (this.jdbcTemplate != null) {
            return;
        }
        this.jdbcTemplate = this.fixture.open();
        this.lambdaTemplate = this.fixture.lambda();
    }

    @Override
    @Before
    public void createBaseMapper() throws SQLException {
        setup();
        super.createBaseMapper();
    }

    @Override
    protected Session newSession() throws SQLException {
        return this.fixture.session();
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
