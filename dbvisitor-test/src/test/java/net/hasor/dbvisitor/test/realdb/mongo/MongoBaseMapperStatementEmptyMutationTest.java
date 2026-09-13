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
import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperStatementEmptyMutationCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoMapperFixture;
import org.junit.After;
import org.junit.Before;

public class MongoBaseMapperStatementEmptyMutationTest extends BaseMapperStatementEmptyMutationCase {
    private final MongoMapperFixture fixture = new MongoMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @Override
    @Before
    public void createBaseMapperWithStatements() throws Exception {
        Configuration configuration = newConfiguration();
        configuration.getMappingRegistry().loadEntityAsTable(UserInfo.class, this.fixture.table());
        configuration.loadMapper("/realdb/mongo/BaseMapperStatements.xml");
        this.mapper = this.fixture.session(configuration).createBaseMapper(UserInfo.class);
    }

    @After
    public void closeFixture() throws Exception {
        this.fixture.close();
    }
}

