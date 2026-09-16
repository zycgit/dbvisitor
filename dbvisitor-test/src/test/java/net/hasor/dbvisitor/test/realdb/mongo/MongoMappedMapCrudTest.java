/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.test.contract.api.lambda.map_query.MappedMapCrudCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;
import org.junit.After;
import org.junit.Before;

public class MongoMappedMapCrudTest extends MappedMapCrudCase {
    private final MongoEntityFixture fixture = new MongoEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        this.lambdaTemplate = this.fixture.lambda();
    }

    @Override
    protected String insertCommand() {
        return fixture.command("insert({id: ?, name: ?, age: ?, email: ?, create_time: ?})");
    }

    @Override
    protected String selectColumnCommand(String column) {
        return fixture.command("find({id: ?}, {_id: 0, " + column + ": 1})");
    }

    @Override
    protected String countCommand() {
        return fixture.command("count({id: ?})");
    }

    @Override
    protected void insertByJdbc(int id, String name, int age, String email) throws SQLException {
        jdbcTemplate.executeUpdate(insertCommand(), new Object[] { id, name, age, email, new Date() });
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
