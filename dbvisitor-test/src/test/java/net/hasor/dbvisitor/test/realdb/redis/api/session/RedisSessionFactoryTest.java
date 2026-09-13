/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.session;

import java.sql.Connection;
import java.util.*;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.*;
import net.hasor.dbvisitor.test.nxn.capability.*;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisSessionFactoryTest extends RedisNativeMapperSupport {


    @Before
    public void loadStatements() throws Exception {
        loadXml();
        session.getConfiguration().loadMapper("/mapper/redis/CoverageMapper.xml");
    }

    @Test
    @Capability(CapabilityId.SESSION_NATIVE_CONFIGURATION_FACTORY)
    public void configuration_shouldCreateWorkingNativeJdbcAndSession() throws Exception {
        Configuration configuration = new Configuration();
        try (Connection connection = OneApiDataSourceManager.getConnection("redis");
            Session local = configuration.newSession(connection)) {
            JdbcTemplate jdbc = configuration.newJdbc(connection);
            assertSame(configuration, local.getConfiguration());
            String key = key("factory");
            assertEquals(1, jdbc.executeUpdate("SET ? ?", new Object[] { key, "first" }));
            NativeMapper mapper = local.createMapper(NativeMapper.class);
            assertEquals("first", mapper.get(key));
            assertEquals(1, mapper.replace(key, "second"));
            assertEquals("second", jdbc.queryForString("GET ?", key));
        }
    }
}
