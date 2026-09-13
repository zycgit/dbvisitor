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
import net.hasor.dbvisitor.test.contract.api.session.SessionMapperSharingCase;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import org.junit.After;
import org.junit.Before;

public class Elastic7SessionMapperSharingTest extends SessionMapperSharingCase {
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
    protected Session createSession() throws Exception {
        return fixture.session();
    }

    @Override
    protected SessionUserMapper simpleMapper(Session session) throws Exception {
        return session.createMapper(Elastic7SessionMapperFixture.NativeSimple.class);
    }

    @Override
    protected SessionRefUserMapper refMapper(Session session) throws Exception {
        return session.createMapper(Elastic7SessionMapperFixture.NativeRef.class);
    }

    @Override
    protected String countUsersCommand() {
        return fixture.countCommand();
    }

    @After
    public void closeSessionFixture() throws Exception {
        fixture.close();
    }
}
