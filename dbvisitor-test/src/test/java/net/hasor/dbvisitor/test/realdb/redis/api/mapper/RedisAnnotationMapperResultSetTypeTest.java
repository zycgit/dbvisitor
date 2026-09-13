/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;

import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisAnnotationMapperExecutionTest.NativeAttributesMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperResultSetTypeCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;

public class RedisAnnotationMapperResultSetTypeTest extends AnnotationMapperResultSetTypeCase {
    private final RedisMapperFixture fixture = new RedisMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.fixture.open();
        this.jdbcTemplate = this.fixture.session().jdbc();
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        this.fixture.open();
        Configuration configuration = newConfiguration();
        configuration.addMacro("redisAttributeUsers", "'" + this.fixture.key("attribute-users") + "'");
        configuration.addMacro("redisAttributeFetch", "'" + this.fixture.key("attribute-fetch") + "'");
        Session session = configuration.newSession(this.fixture.session().jdbc().getConnection());
        this.mapper = session.createMapper(NativeAttributesMapper.class);
        initData();
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            UserInfo user = user(baseId() + i, "AttrNxn" + i, 20 + i, "attr-nxn" + i + "@nxn.test");
            this.mapper.insertUserBasic(user);
            this.jdbcTemplate.queryForLong("ZADD #{arg0} #{arg1} #{arg2,typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}",
                    new Object[] { this.fixture.key("attribute-fetch"), user.getId(), user });
        }
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }

}
