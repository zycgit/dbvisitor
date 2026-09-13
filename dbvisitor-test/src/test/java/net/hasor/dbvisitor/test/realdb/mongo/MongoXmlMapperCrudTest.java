/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeMapperCrudFixture;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperCrudCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import org.junit.After;
import org.junit.Before;

public class MongoXmlMapperCrudTest extends XmlMapperCrudCase {
    private final NativeMapperCrudFixture fixture = new NativeMapperCrudFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(profile().env());
        initData();
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        this.fixture.open(profile().env());
        this.session = this.fixture.createXmlSession(newConfiguration());
    }

    @Override
    protected void initData() throws SQLException {
        this.fixture.seedXmlUsers(baseId());
    }

    @After
    public void cleanupFixture() throws Exception {
        this.fixture.close();
    }
}
