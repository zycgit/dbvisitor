/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperInsertCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;

public class MilvusBaseMapperInsertTest extends BaseMapperInsertCase {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();
    private Session session;

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
    protected Session newSession() throws SQLException {
        // Both inherited mapper initialization and setup use the same isolated connection.
        this.session = newConfiguration().newSession(this.fixture.open());
        return this.session;
    }

    @After
    public void cleanupFixture() throws SQLException, IOException {
        try {
            this.fixture.close();
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
}
