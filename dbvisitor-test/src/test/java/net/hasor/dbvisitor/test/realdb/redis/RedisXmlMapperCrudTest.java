/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperCrudCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisXmlMapperCrudTest extends XmlMapperCrudCase {
    private final RedisXmlCrudFixture fixture = new RedisXmlCrudFixture(baseId());

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        initData();
    }

    @Override
    protected void initData() throws SQLException {
        this.fixture.seedUsers();
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        this.fixture.open();
        this.session = this.fixture.createSession(newConfiguration());
    }

    @After
    public void cleanupFixture() throws Exception {
        this.fixture.close();
    }
}
