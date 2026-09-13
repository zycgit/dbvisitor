/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.feature.naming.ResultColumnCasingCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoNamingFixture;

public class MongoResultColumnCasingTest extends ResultColumnCasingCase {
    private final MongoNamingFixture fixture = new MongoNamingFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        this.lambdaTemplate = this.fixture.lambda(Options.of());
    }

    @Override
    protected String rawIdCondition() {
        return "{id: ?}";
    }

    @Override
    protected String resultProjection() {
        return "{_id: 0, id: 1, name: 1, age: 1, email: 1, create_time: 1}";
    }

    @Override
    protected String insertCommand(String table, String columns) {
        return this.fixture.insert(table, columns);
    }

    @Override
    protected LambdaTemplate optionsLambda(Options options) throws SQLException {
        return this.fixture.lambda(options);
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }
}
