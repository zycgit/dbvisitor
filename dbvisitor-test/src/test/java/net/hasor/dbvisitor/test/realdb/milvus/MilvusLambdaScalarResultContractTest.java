/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaScalarResultContractTest;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusLambdaScalarResultContractTest extends LambdaScalarResultContractTest {
    private final MilvusLambdaResultFixture fixture = new MilvusLambdaResultFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @Override
    protected void insertByJdbc(int id, String name, Integer age, String email) throws SQLException {
        this.fixture.insert(id, name, age, email);
    }

    @Override
    protected EntityQuery<? extends UserInfo> queryRows(String prefix) throws SQLException {
        return this.fixture.queryRows(this.lambdaTemplate, prefix);
    }

    @Override
    protected EntityQuery<? extends UserInfo> orderRows(EntityQuery<? extends UserInfo> query, String field) {
        return this.fixture.orderRows(query);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
