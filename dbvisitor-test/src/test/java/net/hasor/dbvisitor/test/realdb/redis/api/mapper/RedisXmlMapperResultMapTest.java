/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperResultMapCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisXmlMapperResultMapTest extends XmlMapperResultMapCase {
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
    public void createXmlMapperSession() throws Exception {
        this.fixture.open();
        for (int offset = 1; offset <= 3; offset++) {
            this.fixture.session().jdbc().executeUpdate("HSET ? ? ?", new Object[] { this.fixture.key("entry-" + (baseId() + offset)), baseId() + offset, "RmCfg" + offset });
            this.fixture.session().jdbc().executeUpdate("HSET ? ? ?", new Object[] { this.fixture.key("all"), baseId() + offset, "RmCfg" + offset });
        }
        Configuration configuration = newConfiguration();
        configuration.addMacro("redisEntryKey", "#{'" + this.fixture.key("entry-") + "' + id}");
        configuration.addMacro("redisAllEntries", "'" + this.fixture.key("all") + "'");
        configuration.loadMapper("/mapper/redis/ResultMapMapper.xml");
        this.session = configuration.newSession(this.fixture.session().jdbc().getConnection());
    }

    @Override
    protected Map<String, Object> expectedFull(int offset) {
        return Map.of("field", baseId() + offset, "value", "RmCfg" + offset);
    }

    @Override
    protected Map<String, Object> expectedPartial(int offset) {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("field", baseId() + offset);
        expected.put("value", null);
        return expected;
    }

    @Override
    protected Map<String, Class<?>> expectedPropertyTypes() {
        return Map.of("field", Integer.class, "value", String.class);
    }

    @Override
    protected List<String> nonNullFullProperties() {
        return List.of("field", "value");
    }

    @Override
    protected List<String> nonNullPartialProperties() {
        return List.of("field");
    }

    @Override
    protected List<String> nullPartialProperties() {
        return List.of("value");
    }

    @Override
    protected Map<String, Object> expectedColumnLabels() {
        return Map.of("FIELD", String.valueOf(baseId() + 3), "VALUE", "RmCfg3");
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
