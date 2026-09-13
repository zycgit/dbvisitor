/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeAnnotationParameterFixture;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperStructuredParameterCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import org.junit.After;
import org.junit.Before;

public class Elastic6AnnotationMapperStructuredParameterTest extends AnnotationMapperStructuredParameterCase {
    private final NativeAnnotationParameterFixture fixture = new NativeAnnotationParameterFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(profile().env());
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        this.fixture.open(profile().env());
        this.mapper = this.fixture.createMapper(newConfiguration());
    }

    @After
    public void cleanupFixture() throws Exception {
        this.fixture.close();
    }
}
