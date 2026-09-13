/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.RedisJsonTypeFixture;
import net.hasor.dbvisitor.test.contract.feature.type.JsonSerializationJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisJsonSerializationJdbcTest extends JsonSerializationJdbcCase {
    private final RedisJsonTypeFixture fixture = new RedisJsonTypeFixture();

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
    protected String insertCommand(String columns, String... parameters) {
        return this.fixture.insertCommand(columns, parameters);
    }

    @Override
    protected String selectCommand(String columns) throws SQLException {
        return this.fixture.selectCommand(columns);
    }

    @Override
    protected Object fixtureKey(int id) {
        return this.fixture.key(id);
    }

    @Override
    protected String storedJson(Object id) throws SQLException {
        return this.fixture.storedJson(id);
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }
}
