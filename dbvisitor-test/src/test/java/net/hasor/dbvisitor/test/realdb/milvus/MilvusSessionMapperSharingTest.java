/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.session.SessionMapperSharingCase;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusSessionMapperSharingTest extends SessionMapperSharingCase {
    private final MilvusSessionMapperSupport fixture = new MilvusSessionMapperSupport();

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
    protected Session createSession() throws Exception {
        return this.fixture.session();
    }

    @Override
    protected SessionUserMapper simpleMapper(Session session) throws Exception {
        return session.createMapper(MilvusSessionMapperSupport.AnnotationMapper.class);
    }

    @Override
    protected SessionRefUserMapper refMapper(Session session) throws Exception {
        return session.createMapper(MilvusSessionMapperSupport.XmlMapper.class);
    }

    @After
    public void closeFixture() throws Exception {
        this.fixture.close();
    }
}
