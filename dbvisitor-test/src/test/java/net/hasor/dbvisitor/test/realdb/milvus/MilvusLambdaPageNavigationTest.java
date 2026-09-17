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
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaPageNavigationCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import net.hasor.dbvisitor.test.nxn.junit.NxnConcurrent;
import org.junit.After;
import org.junit.Before;

@NxnConcurrent
public class MilvusLambdaPageNavigationTest extends LambdaPageNavigationCase {
    private final MilvusLambdaPageFixture fixture = new MilvusLambdaPageFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open(), new MappingRegistry(), null);
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @Override
    protected void insert(int id, String name, Integer age) throws SQLException {
        this.fixture.insert(id, name, age);
    }

    @Override
    protected EntityQuery<? extends UserInfo> queryRows() throws SQLException {
        return this.fixture.query(this.lambdaTemplate);
    }

    @Override
    protected EntityQuery<? extends UserInfo> queryRows(String prefix) throws SQLException {
        return this.fixture.query(this.lambdaTemplate, prefix);
    }

    @Override
    protected EntityQuery<? extends UserInfo> orderRows(EntityQuery<? extends UserInfo> query) {
        return this.fixture.order(query);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
