/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeDocumentParameterFixture;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterCommand;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcTextParameterContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import org.junit.After;
import org.junit.Before;

public class MongoJdbcTextParameterContractTest extends JdbcTextParameterContractTest {
    private final NativeDocumentParameterFixture fixture = new NativeDocumentParameterFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(profile().env());
    }

    @Override
    protected String command(JdbcParameterCommand command) throws SQLException {
        if (!command.name().startsWith("INSERT_")) {
            this.fixture.refresh();
        }
        return this.fixture.command(command);
    }

    @Override
    protected String fixtureTable() {
        return this.fixture.table();
    }

    @Override
    protected String fixtureColumn(String name) {
        return this.fixture.column(name);
    }

    @Override
    protected void assertNumericField(int expected, Object actual) {
        org.junit.Assert.assertEquals(Integer.toString(expected), actual);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
