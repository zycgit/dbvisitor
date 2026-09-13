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
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperConditionalDeleteCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusAnnotationMapperConditionalDeleteTest extends AnnotationMapperConditionalDeleteCase {
    private final MilvusCapabilityFixture fixture = new MilvusCapabilityFixture();
    private Session session;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        this.fixture.userTable("user_info", "id INT64 PRIMARY KEY");
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        this.session = newConfiguration().newSession(this.jdbcTemplate.getConnection());
        this.mapper = this.session.createMapper(MilvusCapabilityMappers.Results.class);
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
