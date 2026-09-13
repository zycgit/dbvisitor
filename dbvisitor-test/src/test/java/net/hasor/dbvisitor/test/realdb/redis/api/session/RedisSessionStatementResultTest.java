/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.session;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.test.contract.api.session.SessionStatementResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisEntityFixture;
import org.junit.After;
import org.junit.Before;

public class RedisSessionStatementResultTest extends SessionStatementResultCase {
    private final RedisEntityFixture fixture = new RedisEntityFixture();

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
    @Before
    public void createStatementSession() throws Exception {
        this.session = this.fixture.session(newConfiguration(), "/session/RedisResultSessionMapper.xml");
    }

    @Override
    protected String resultIdProperty() {
        return "field";
    }

    @Override
    protected String resultNameProperty() {
        return "value";
    }

    @Override
    protected List<String> presentResultProperties() {
        return List.of("field", "value");
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
