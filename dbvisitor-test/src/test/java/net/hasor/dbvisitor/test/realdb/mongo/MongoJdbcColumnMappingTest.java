/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcColumnMappingCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;

public class MongoJdbcColumnMappingTest extends JdbcColumnMappingCase {
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

    @Override
    protected void seedColumnValue() throws SQLException {
        this.jdbcTemplate.executeUpdate(this.fixture.command("insert({id: ?, name: ?, age: ?})"),
                new Object[] { 932001, "NXN-Column", 21 });
    }

    @Override
    protected String columnQuery() {
        return this.fixture.command("aggregate([{$match: {id: 932001}}, {$project: {_id: 0, NXN_VALUE: '$name'}}])");
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }
}
