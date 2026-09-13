/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperStatementEmptyMutationCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;

public class RedisBaseMapperStatementEmptyMutationTest extends BaseMapperStatementEmptyMutationCase {
    private final RedisEntityFixture fixture = new RedisEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @Override
    @Before
    public void createBaseMapperWithStatements() throws Exception {
        Session session = this.fixture.session(newConfiguration(), "/mapper/redis/StatementMapper.xml");
        this.mapper = session.createBaseMapper(UserInfo.class);
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected List<UserInfo> query(String statementId, Object params) {
        if ("queryUsersByName".equals(statementId)) {
            this.mapper.executeStatement(NS + ".prepareNameResults", params);
        }
        return super.query(statementId, params);
    }
}
