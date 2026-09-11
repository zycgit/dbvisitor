/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlRefMapperTemplateContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusXmlRefMapperTemplateContractTest extends XmlRefMapperTemplateContractTest {
    private final MilvusXmlRefFixture fixture = new MilvusXmlRefFixture();

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
    public void createRefMapper() throws Exception {
        this.dao = this.fixture.mapper();
    }

    @Override
    protected void insertUser(Object[] values) throws SQLException {
        this.fixture.insert(this.jdbcTemplate, values);
    }

    @Override
    protected String orderExpression(String field) {
        // Only fixed fixture field names reach this hook; native vectors provide the requested order.
        if ("id".equals(field)) {
            return "v <-> [0, 0]";
        }
        if ("age".equals(field)) {
            return "v_age <-> [0, 0]";
        }
        throw new IllegalArgumentException("Unknown fixture ordering: " + field);
    }

    @After
    public void cleanupFixture() throws Exception {
        this.fixture.close();
    }
}
