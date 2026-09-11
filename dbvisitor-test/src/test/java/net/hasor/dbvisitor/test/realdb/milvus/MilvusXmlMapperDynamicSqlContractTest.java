/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperDynamicSqlContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusXmlMapperDynamicSqlContractTest extends XmlMapperDynamicSqlContractTest {
    private final MilvusXmlDynamicFixture fixture = new MilvusXmlDynamicFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        initData();
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        this.session = this.fixture.session("/realdb/milvus/material/XmlDynamicSqlMapper.xml");
    }

    @Override
    protected void insertUser(Object[] values) throws SQLException {
        this.fixture.insert(this.jdbcTemplate, values);
    }

    @After
    public void cleanupFixture() throws Exception {
        this.fixture.close();
    }
}
