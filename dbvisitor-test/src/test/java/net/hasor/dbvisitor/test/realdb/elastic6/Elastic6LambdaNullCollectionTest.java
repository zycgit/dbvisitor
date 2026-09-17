/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaNullCollectionCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import net.hasor.dbvisitor.test.nxn.junit.NxnConcurrent;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

@NxnConcurrent
public class Elastic6LambdaNullCollectionTest extends LambdaNullCollectionCase {
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
    protected void insert(int id, String name, Integer age, String email) throws SQLException {
        fixture.insert(id, name, age, email);
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
