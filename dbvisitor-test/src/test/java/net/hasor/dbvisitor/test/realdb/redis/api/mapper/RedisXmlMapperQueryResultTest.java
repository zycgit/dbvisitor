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
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperQueryResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisXmlMapperQueryResultTest extends XmlMapperQueryResultCase {
    private final RedisEntityFixture fixture = new RedisEntityFixture();

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
    public void createXmlMapperSession() throws Exception {
        this.session = this.fixture.session(newConfiguration(), "/mapper/redis/CrudAccessMapper.xml");
        for (int i = 1; i <= 5; i++) {
            this.session.executeStatement("xmltest.CrudMapper.seed", Map.of(
                    "id", baseId() + i, "name", "XmlCrud" + i, "age", 20 + i, "email", "crud" + i + "@test.com"));
        }
    }

    @Override
    protected Map<String, Object> expectedFirstMapRow() {
        return Map.of("ELEMENT", "XmlCrud1", "SCORE", (double) (baseId() + 1));
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
