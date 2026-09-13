/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaSecurityValueCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic6LambdaSecurityValueTest extends LambdaSecurityValueCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());
        lambdaTemplate = fixture.lambdaTemplate();
    }

    @Override
    protected void insertUser(int id, String name, Integer age) throws SQLException {
        fixture.insert(id, name, age, null);
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }

    @Override
    protected String rawNamePredicate() {
        return "{\"term\": {\"name\": ?}}";
    }

    @Override
    protected String rawTruePredicate() {
        return "{\"match_all\": {}}";
    }

    @Override
    protected String tableName() {
        return fixture.index();
    }
}
