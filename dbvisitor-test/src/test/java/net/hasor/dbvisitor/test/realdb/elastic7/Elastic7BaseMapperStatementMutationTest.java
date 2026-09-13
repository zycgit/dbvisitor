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
import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperStatementMutationCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import org.junit.After;
import org.junit.Before;

public class Elastic7BaseMapperStatementMutationTest extends BaseMapperStatementMutationCase {
    private final Elastic7SessionMapperFixture fixture = new Elastic7SessionMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
    }

    @Override
    @Before
    public void createBaseMapperWithStatements() throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        Session session = fixture.session();
        session.getConfiguration().loadMapper("/mapper/elastic/BaseMapperStatementMatrix.xml");
        this.mapper = session.createBaseMapper(UserInfo.class);
    }

    @After
    public void closeStatementFixture() throws Exception {
        fixture.close();
    }
}
