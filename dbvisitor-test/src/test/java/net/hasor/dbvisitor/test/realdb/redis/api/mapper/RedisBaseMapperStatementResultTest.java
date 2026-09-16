/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperStatementResultCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisBaseMapperStatementResultTest extends BaseMapperStatementResultCase {
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
        Session session = this.fixture.session(newConfiguration(), "/mapper/redis/StatementProjectionMapper.xml");
        this.mapper = session.createBaseMapper(UserInfo.class);
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected void prepareProjectionData() {
        for (int i = 1; i <= 3; i++) {
            execute("insertUserWithId", mapOf("id", baseId() + 10 + i, "name", "BaseStmtQuery" + i));
        }
    }

    @Override
    protected List<String> requiredProjectionProperties() {
        return Collections.emptyList();
    }

    @Override
    protected List<?> queryProjection(String statementId, Object parameters) {
        if ("queryUsersByName".equals(statementId)) {
            this.mapper.executeStatement(NS + ".prepareNameResults", parameters);
        }
        return super.queryProjection(statementId, parameters);
    }

    public static class Projection {
        @Column("SCORE")
        private Integer id;
        @Column("ELEMENT")
        private String  name;

        public Integer getId() {
            return this.id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
