/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperScrollableResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.Elastic7SessionMapperFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic6XmlMapperScrollableResultTest extends XmlMapperScrollableResultCase {
    private final Elastic7SessionMapperFixture fixture = new Elastic7SessionMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
        for (int i = 1; i <= 5; i++) {
            fixture.insert(baseId() + i, "StmtAttr" + i, 20 + i, "attr" + i + "@nxn.test");
        }
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        this.session = fixture.session();
        this.session.getConfiguration().loadMapper("/mapper/elastic/StatementAttributeMatrix.xml");
    }

    @After
    public void closeXmlFixture() throws Exception {
        fixture.close();
    }
}
