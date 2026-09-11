/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeDocumentQueryFixture;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaCountContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import org.junit.After;
import org.junit.Before;

public class Elastic6LambdaCountContractTest extends LambdaCountContractTest {
    private final NativeDocumentQueryFixture fixture = new NativeDocumentQueryFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(profile().env());
        this.lambdaTemplate = this.fixture.lambdaTemplate();
    }

    @Override
    protected void insertByJdbc(int id, String name, Integer age, String email) throws SQLException {
        this.fixture.insert(id, name, age, email, new Date());
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
