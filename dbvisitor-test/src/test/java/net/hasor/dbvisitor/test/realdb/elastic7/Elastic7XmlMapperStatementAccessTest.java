/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperStatementAccessCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import org.junit.After;
import org.junit.Before;

public class Elastic7XmlMapperStatementAccessTest extends XmlMapperStatementAccessCase {
    private final Elastic7SessionMapperFixture fixture = new Elastic7SessionMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
        for (int i = 1; i <= 5; i++) {
            fixture.insert(baseId() + i, "XmlCrud" + i, 20 + i, "crud" + i + "@test.com");
        }
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        this.session = fixture.session();
        this.session.getConfiguration().loadMapper("/mapper/elastic/CrudMatrix.xml");
    }

    @Override
    protected Session createMultiNamespaceSession() throws Exception {
        this.session.getConfiguration().loadMapper("/mapper/elastic/ResultMapMatrix.xml");
        return this.session;
    }

    @After
    public void closeXmlFixture() throws Exception {
        fixture.close();
    }
}
