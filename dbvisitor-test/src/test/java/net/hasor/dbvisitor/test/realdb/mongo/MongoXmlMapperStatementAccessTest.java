/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperStatementAccessCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoMapperFixture;
import org.junit.After;
import org.junit.Before;

public class MongoXmlMapperStatementAccessTest extends XmlMapperStatementAccessCase {
    private final MongoMapperFixture fixture = new MongoMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        for (int i = 1; i <= 5; i++) {
            this.jdbcTemplate.executeUpdate(this.fixture.source() + ".insert({id: ?, name: ?, age: ?})", new Object[] { baseId() + i, "XmlCrud" + i, 20 + i });
        }
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration configuration = newConfiguration();
        configuration.loadMapper("/realdb/mongo/QueryMapper.xml");
        this.session = this.fixture.session(configuration);
    }

    @Override
    protected Session createMultiNamespaceSession() throws Exception {
        Configuration configuration = newConfiguration();
        configuration.loadMapper("/realdb/mongo/QueryMapper.xml");
        configuration.loadMapper("/realdb/mongo/ResultMapMapper.xml");
        return this.fixture.session(configuration);
    }

    @After
    public void closeFixture() throws Exception {
        this.fixture.close();
    }
}
