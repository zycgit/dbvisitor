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
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.session.SessionMapperInvocationCase;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoMapperFixture;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoSessionRefMapper;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoSessionUserMapper;
import org.junit.After;
import org.junit.Before;

public class MongoSessionMapperInvocationTest extends SessionMapperInvocationCase {
    private final MongoMapperFixture users = new MongoMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.users.open();
    }

    @Override
    protected Session createSession() throws Exception {
        Configuration configuration = newConfiguration();
        configuration.getMappingRegistry().loadEntityAsTable(UserInfo.class, this.users.table());
        configuration.addMacro("mongoCollection", this.users.table());
        configuration.addMacro("mongoOrders", this.users.source() + "_orders");
        return this.users.session(configuration);
    }

    @Override
    protected SessionUserMapper simpleMapper(Session session) throws Exception {
        return session.createMapper(MongoSessionUserMapper.class);
    }

    @Override
    protected SessionRefUserMapper refMapper(Session session) throws Exception {
        return session.createMapper(MongoSessionRefMapper.class);
    }

    @Override
    protected String countUsersCommand() {
        return this.users.source() + ".count({})";
    }

    @After
    public void closeFixtures() throws Exception {
        this.users.close();
    }
}
