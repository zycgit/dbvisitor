/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlRefMapperCrudCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoMapperFixture;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoRefMapper;
import org.junit.After;
import org.junit.Before;

public class MongoXmlRefMapperCrudTest extends XmlRefMapperCrudCase {
    private final MongoMapperFixture fixture = new MongoMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        initData();
    }

    @Override
    protected void insertUser(Object[] values) throws SQLException {
        this.jdbcTemplate.executeUpdate(this.fixture.source() + ".insert({id: ?, name: ?, age: ?, email: ?, create_time: 1700000000000})", values);
    }

    @Override
    protected String orderExpression(String field) {
        return "{" + field + ": 1}";
    }

    @Override
    @Before
    public void createRefMapper() throws Exception {
        this.dao = this.fixture.session(newConfiguration()).createMapper(MongoRefMapper.class);
    }

    @After
    public void closeFixture() throws Exception {
        this.fixture.close();
    }
}
