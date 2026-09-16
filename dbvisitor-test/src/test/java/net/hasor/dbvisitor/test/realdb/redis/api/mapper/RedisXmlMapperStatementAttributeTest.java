/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.Map;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperStatementAttributeCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisXmlMapperStatementAttributeTest extends XmlMapperStatementAttributeCase {
    private final RedisEntityFixture fixture = new RedisEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    protected int expectedQueryTimeout() {
        return 5;
    }

    @Override
    protected int expectedCombinedFetchSize() {
        return 2;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        Session material = this.fixture.session(newConfiguration(), "/session/RedisUserSessionMapper.xml");
        for (int i = 1; i <= 5; i++) {
            material.executeStatement("session.UserSessionMapper.insertUser", Map.of("id", baseId() + i, "name", "StmtAttr" + i, "age", 20 + i, "email", "attr" + i + "@nxn.test"));
        }
        this.session = this.fixture.session(newConfiguration(), "/mapper/redis/StatementAttrMapper.xml");
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
