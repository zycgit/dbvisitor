/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcBatchErrorCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;
import org.junit.After;
import org.junit.Before;

public class MongoJdbcBatchErrorTest extends JdbcBatchErrorCase {
    private final MongoEntityFixture fixture = new MongoEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected String insertCommand() {
        return this.fixture.command("insert({id: ?, string_value: ?})");
    }

    @Override
    protected String valueCommand() {
        return this.fixture.command("find({id: ?}, {_id: 0, string_value: 1})");
    }

    @Override
    protected String countCommand() {
        return this.fixture.command("count({id: ?})");
    }

    @Override
    protected String literalInsertCommand(int id, String value) {
        return this.fixture.command("insert({id: " + id + ", string_value: '" + value + "'})");
    }

    @Override
    protected String invalidCommand() {
        return this.fixture.command("unsupportedOperation()");
    }
}
