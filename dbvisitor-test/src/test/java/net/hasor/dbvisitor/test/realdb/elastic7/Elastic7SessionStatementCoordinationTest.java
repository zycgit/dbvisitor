/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.session.SessionStatementCoordinationCase;
import net.hasor.dbvisitor.test.nxn.env.*;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7SessionStatementCoordinationTest extends SessionStatementCoordinationCase {
    private final Elastic7SessionMapperFixture fixture = new Elastic7SessionMapperFixture();
    private final ElasticMatrixFixture orders = new ElasticMatrixFixture();
    @Override
    protected DataSourceProfile profile() { return Elastic7Profile.INSTANCE; }
    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
        orders.open(profile().env());
    }
    @Override
    @Before
    public void createStatementSession() throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        orders.open(profile().env());
        this.session = fixture.session();
        session.getConfiguration().addMacro("esOrderPath", "POST /" + orders.index());
        session.getConfiguration().loadMapper("/mapper/elastic/SessionCoordinationMatrix.xml");
    }
    @After
    public void closeCoordinationFixture() throws Exception {
        try {
            orders.close();
        } finally {
            fixture.close();
        }
    }
}
