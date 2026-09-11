/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.MapQuery;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeLambdaPaginationSupport;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import org.junit.After;
import org.junit.Before;

public abstract class Elastic6LambdaPaginationSupport extends NativeLambdaPaginationSupport {
    protected Connection connection;

    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Before
    public void createPageFixture() throws SQLException {
        this.connection = newAdapterConnection();
        this.collection = "nxn_page_" + UUID.randomUUID().toString().replace("-", "");
        this.lambda = new LambdaTemplate(this.connection);
        insertPageRows();
    }

    @Override
    protected MapQuery order(MapQuery query) {
        return query.asc("seq");
    }

    @After
    public void cleanupPageFixture() throws SQLException {
        if (this.connection != null) {
            try (Connection closing = this.connection) {
                new JdbcTemplate(closing).execute("DELETE /" + this.collection);
            }
        }
    }
}
