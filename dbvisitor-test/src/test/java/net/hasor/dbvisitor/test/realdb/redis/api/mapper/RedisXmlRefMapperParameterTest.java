/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlRefMapperParameterCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisXmlRefMapperParameterTest extends XmlRefMapperParameterCase {
    private final RedisXmlRefFixture fixture = new RedisXmlRefFixture();

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
    public void createRefMapper() throws Exception {
        this.dao = this.fixture.createMapper(newConfiguration(), baseId());
    }

    @Override
    protected UserInfo beanParameters() {
        UserInfo sample = new UserInfo();
        sample.setName(this.fixture.index("ages"));
        sample.setAge(28);
        return sample;
    }

    @Override
    protected List<String> expectedBeanNames() {
        return List.of("RefMapB", "RefMapD", "RefMapC");
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
