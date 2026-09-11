/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class RedisCommandTest {
    @org.junit.BeforeClass
    public static void assumeDataSource() {
        OneApiDataSourceManager.assumeCurrentDataSource("redis");
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_COMMAND_QUERY)
    public void testGetSet() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection("redis")) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);

            jdbc.execute("set abc Hello");

            List<Map<String, Object>> list = jdbc.queryForList("get abc");
            assertTrue(list.size() == 1);
            assertTrue(list.get(0).get("VALUE").equals("Hello"));
        }
    }
}
