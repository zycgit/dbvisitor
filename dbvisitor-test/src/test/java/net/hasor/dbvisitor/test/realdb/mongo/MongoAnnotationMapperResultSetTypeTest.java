/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperResultSetTypeCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoMapperFixture;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoAttributeMapper;
import org.junit.After;
import org.junit.Before;

public class MongoAnnotationMapperResultSetTypeTest extends AnnotationMapperResultSetTypeCase {
    private final MongoMapperFixture fixture = new MongoMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        this.fixture.seed(baseId(), "AttrNxn", "attr-nxn");
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        this.mapper = this.fixture.session(newConfiguration()).createMapper(MongoAttributeMapper.class);
    }

    @After
    public void closeFixture() throws Exception {
        this.fixture.close();
    }
}

