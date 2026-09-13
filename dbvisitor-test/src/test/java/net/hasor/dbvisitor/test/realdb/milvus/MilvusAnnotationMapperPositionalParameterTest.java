/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperPositionalParameterCase;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ParameterBindingMapper;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Shared parameter assertions against native storage, without database-function prerequisites. */
public class MilvusAnnotationMapperPositionalParameterTest extends AnnotationMapperPositionalParameterCase {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();
    private Session session;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        this.session = newConfiguration().newSession(this.fixture.open());
        this.mapper = this.session.createMapper(ParameterBindingMapper.class);
    }

    @After
    public void cleanupFixture() throws Exception {
        try {
            this.fixture.close();
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
}
