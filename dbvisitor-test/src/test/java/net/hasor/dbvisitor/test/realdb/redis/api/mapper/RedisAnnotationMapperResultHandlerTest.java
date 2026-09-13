/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperResultHandlerCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;
import org.junit.After;
import org.junit.Before;

public class RedisAnnotationMapperResultHandlerTest extends AnnotationMapperResultHandlerCase {
    private final RedisMapperFixture fixture = new RedisMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        fixture.open();
    }

    @Override
    @Before
    public void createResultHandlerMapper() throws Exception {
        fixture.open();
        Configuration configuration = newConfiguration();
        configuration.getTypeRegistry().register(UserInfo.class, new JsonTypeHandler(UserInfo.class));
        configuration.addMacro("redisHandlerKey", "'" + fixture.key("pattern:AnnoHandler%") + "'");
        fixture.key("pattern:NoAnnoHandlerMatch%");
        configuration.addMacro("redisHandlerRows", "ZRANGE #{'" + fixture.key("pattern:") + "'+pattern} 0 -1");
        this.mapper = configuration.newSession(fixture.session().jdbc().getConnection()).createMapper(RedisResultHandlerMapper.class);
        prepareRows();
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
