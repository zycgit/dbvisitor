/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperStatementAccessCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusXmlMapperStatementAccessTest extends XmlMapperStatementAccessCase {
    private final MilvusXmlCapabilityFixture fixture = new MilvusXmlCapabilityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(baseId());
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        this.jdbcTemplate = this.fixture.open(baseId());
        this.session = this.fixture.session(newConfiguration());
    }

    @Override
    protected Session createMultiNamespaceSession() throws Exception {
        return this.fixture.session(newConfiguration());
    }

    @After
    public void cleanupFixture() throws Exception {
        try {
            this.fixture.close();
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
}
