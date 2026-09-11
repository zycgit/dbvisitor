/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.RedisJdbcCrudFixture;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudScalarReadbackContractTest;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudCommand;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisJdbcCrudScalarReadbackContractTest extends JdbcCrudScalarReadbackContractTest {
    private final RedisJdbcCrudFixture fixture = new RedisJdbcCrudFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @Override
    protected String command(JdbcCrudCommand command) throws SQLException {
        return this.fixture.command(command);
    }

    @Override
    protected int insertUser(int id, String name, int age, String email) throws SQLException {
        return this.fixture.insert(id, name, age, email);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
