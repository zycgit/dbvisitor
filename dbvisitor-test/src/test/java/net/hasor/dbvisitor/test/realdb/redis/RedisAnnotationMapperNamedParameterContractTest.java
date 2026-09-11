/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperNamedParameterContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisAnnotationMapperNamedParameterContractTest extends AnnotationMapperNamedParameterContractTest {
    private final RedisAnnotationParameterFixture fixture = new RedisAnnotationParameterFixture();

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
    public void createAnnotationMapper() throws Exception {
        this.fixture.open();
        this.mapper = this.fixture.createMapper(newConfiguration());
    }

    @After
    public void cleanupFixture() throws Exception {
        this.fixture.close();
    }
}
