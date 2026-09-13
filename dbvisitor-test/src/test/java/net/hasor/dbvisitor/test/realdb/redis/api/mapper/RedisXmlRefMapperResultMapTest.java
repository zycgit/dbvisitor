/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.Map;
import java.util.List;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlRefMapperResultMapCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisXmlRefMapperResultMapTest extends XmlRefMapperResultMapCase {
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
    protected Map<String, Object> expectedRow(int index) {
        return Map.of("ELEMENT", "RefMap" + (char) ('A' + index), "SCORE", (double) (baseId() + index + 1));
    }

    @Override
    protected List<String> presentColumns() {
        return List.of("ELEMENT", "SCORE");
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
