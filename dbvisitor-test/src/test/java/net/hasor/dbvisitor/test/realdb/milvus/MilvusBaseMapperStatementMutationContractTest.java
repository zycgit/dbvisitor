/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperStatementMutationContractTest;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusBaseMapperStatementMutationContractTest extends BaseMapperStatementMutationContractTest {
    private final MilvusStatementFixture fixture = new MilvusStatementFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.fixture.open();
    }

    @Override
    @Before
    public void createBaseMapperWithStatements() throws Exception {
        this.mapper = this.fixture.session("/realdb/milvus/material/BaseMapperStatements.xml").createBaseMapper(UserInfo.class);
    }

    @After
    public void cleanupFixture() throws Exception {
        this.fixture.close();
    }
}
