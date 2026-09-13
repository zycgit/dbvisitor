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
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperResultHandlerCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisXmlMapperResultHandlerTest extends XmlMapperResultHandlerCase {
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
        this.fixture.open();
        for (int i = 1; i <= 3; i++) {
            this.fixture.open().queryForLong("ZADD ? ? ?", new Object[] { this.fixture.key("ages"), 20 + i * 5, "ResHdl" + i });
        }
        this.session = this.fixture.session(newConfiguration(), "/mapper/redis/ResultHandlerMapper.xml");
    }

    @Override
    protected Map<String, Object> expectedEntity(int offset) {
        return Map.of("element", "ResHdl" + offset, "score", (double) (20 + offset * 5));
    }

    @Override
    protected Map<String, Object> expectedMap(int offset) {
        return Map.of("ELEMENT", "ResHdl" + offset, "SCORE", (double) (20 + offset * 5));
    }

    @Override
    protected Map<Object, Object> expectedPairs() {
        return Map.of("ResHdl1", 25.0, "ResHdl2", 30.0, "ResHdl3", 35.0);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
