/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperEmptyMutationCase;
import net.hasor.dbvisitor.test.realdb.redis.RedisAnnotationCrudFixture;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisAnnotationMapperEmptyMutationTest extends AnnotationMapperEmptyMutationCase {
    private final RedisAnnotationCrudFixture fixture = new RedisAnnotationCrudFixture(baseId());

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open();
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        fixture.open();
        this.mapper = fixture.createMapper(newConfiguration());
    }

    @After
    public void closeFixture() throws Exception {
        fixture.close();
    }
}
