/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.map_query.FreedomMapIdentifierSecurityCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;
import java.util.Date;
import org.junit.After;
import org.junit.Before;

public class MongoFreedomMapIdentifierSecurityTest extends FreedomMapIdentifierSecurityCase {
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
    protected String tableName() {
        return fixture.table();
    }

    @Override
    protected void insertUser(int id, String name, Integer age) throws SQLException {
        jdbcTemplate.executeUpdate(fixture.command("insert({id: ?, name: ?, age: ?, create_time: ?})"),
                new Object[] { id, name, age, new Date() });
    }

    @Override
    protected long countById(int id) throws SQLException {
        return jdbcTemplate.queryForLong(fixture.command("count({id: ?})"), new Object[] { id });
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
