/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.session.SessionStatementPaginationCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusSessionStatementPaginationTest extends SessionStatementPaginationCase {
    private final MilvusStatementFixture fixture = new MilvusStatementFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.fixture.open();
    }

    @Override
    @Before
    public void createStatementSession() throws Exception {
        this.session = this.fixture.session("/realdb/milvus/material/SessionStatements.xml");
    }

    @After
    public void cleanupFixture() throws Exception {
        this.fixture.close();
    }
}
