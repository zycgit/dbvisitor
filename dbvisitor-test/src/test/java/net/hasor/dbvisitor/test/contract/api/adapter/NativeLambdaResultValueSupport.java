/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.adapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaResultValueContractTest;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.After;
import org.junit.Before;

/** Document stores use isolated collections and the same public result-conversion assertions. */
public abstract class NativeLambdaResultValueSupport extends LambdaResultValueContractTest {
    protected final String collection = "nxn_result_" + UUID.randomUUID().toString().replace("-", "");
    private final NativeLambdaResultFixture fixture = new NativeLambdaResultFixture();
    private Connection connection;

    @Override
    @Before
    public void setup() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
        this.connection = OneApiDataSourceManager.getConnection(profile().env());
        this.jdbcTemplate = new JdbcTemplate(this.connection);
        prepareConnection();
        this.lambdaTemplate = this.fixture.initialize(this.connection, this.collection);
    }

    protected void prepareConnection() throws SQLException {
    }

    protected abstract void dropCollection() throws SQLException;

    @Override
    protected EntityQuery<? extends UserInfo> orderRows(EntityQuery<? extends UserInfo> query, String field) {
        return query.asc(field);
    }

    @Override
    protected void insertByJdbc(int id, String name, Integer age, String email) throws SQLException {
        this.fixture.insert(id, name, age, email);
    }

    @Override
    protected EntityQuery<? extends UserInfo> queryRows(String prefix) {
        return this.fixture.queryRows(prefix);
    }

    @After
    public void cleanupFixture() throws SQLException {
        if (this.connection != null) {
            try (Connection closing = this.connection) {
                if (this.fixture.hasRows()) {
                    dropCollection();
                }
            }
        }
    }
}
