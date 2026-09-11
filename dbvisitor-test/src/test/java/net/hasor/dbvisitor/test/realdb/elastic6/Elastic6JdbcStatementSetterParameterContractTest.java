/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.PreparedStatementSetter;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeDocumentParameterFixture;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterCommand;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcStatementSetterParameterContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import org.junit.After;
import org.junit.Before;

public class Elastic6JdbcStatementSetterParameterContractTest extends JdbcStatementSetterParameterContractTest {
    private final NativeDocumentParameterFixture fixture = new NativeDocumentParameterFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
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
    protected String readEmail(String command, PreparedStatementSetter args) throws SQLException {
        return (String) jdbcTemplate.queryForMap(command, args).get("email");
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
