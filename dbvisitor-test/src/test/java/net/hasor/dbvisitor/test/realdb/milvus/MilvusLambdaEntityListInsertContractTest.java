/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaEntityListInsertContractTest;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusLambdaEntityListInsertContractTest extends LambdaEntityListInsertContractTest {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture(false);

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @Override
    protected long countInsertedUsers(List<UserInfo> users) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        for (UserInfo user : users) {
            ids.add(user.getId());
        }
        return this.lambdaTemplate.query(UserInfo.class)
                .in(UserInfo::getId, ids).queryForCount();
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
