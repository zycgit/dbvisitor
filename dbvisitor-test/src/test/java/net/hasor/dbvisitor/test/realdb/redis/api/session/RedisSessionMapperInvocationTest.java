/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.session;

import java.sql.SQLException;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.session.SessionMapperInvocationCase;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefCrudMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisEntityFixture;
import org.junit.After;
import org.junit.Before;

public class RedisSessionMapperInvocationTest extends SessionMapperInvocationCase {
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
    protected Session createSession() throws Exception {
        return this.fixture.session(newConfiguration(), "/session/RedisUserSessionMapper.xml");
    }

    @Override
    protected SessionUserMapper simpleMapper(Session session) throws Exception {
        return session.createMapper(RedisSessionUserMapper.class);
    }

    @Override
    protected SessionRefCrudMapper refMapper(Session session) throws Exception {
        return session.createMapper(RedisSessionRefUserMapper.class);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
