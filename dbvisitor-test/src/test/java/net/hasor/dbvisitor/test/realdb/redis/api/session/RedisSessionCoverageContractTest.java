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
import org.junit.Before;
import org.junit.Test;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.*;
import net.hasor.dbvisitor.test.nxn.capability.*;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport;
import static org.junit.Assert.*;

public class RedisSessionCoverageContractTest extends RedisNativeMapperSupport {

    @Before
    public void loadStatements() throws Exception {
        loadXml();
        session.getConfiguration().loadMapper("/mapper/redis/CoverageMapper.xml");
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_NATIVE_ACCESSORS)
    public void accessors_shouldExposeEntitySessionAndWorkingJdbc() throws Exception {
        BaseMapper<Entry> base = session.createBaseMapper(Entry.class);
        assertEquals(Entry.class, base.entityType());
        assertSame(session, base.session());
        assertNotNull(base.jdbc());
        String key = key("accessor");
        assertEquals(1, base.jdbc().executeUpdate("SET ? ?", new Object[] { key, "value" }));
        assertEquals(Arrays.asList("value"), base.queryStatement("redis.Native.get", params(key, null)));
        assertEquals("value", base.session().jdbc().queryForString("GET ?", key));
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

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_CROSS_TABLE)
    public void namedStatements_shouldCoordinateOwnerAndOrderKeys() throws Exception {
        String ownerKey = key("user-1");
        String orderKey = key("user-1-orders");
        assertEquals(1, ((Number) session.executeStatement("redis.Coverage.owner", params(ownerKey, "mali"))).intValue());
        Map<String, Object> first = params(orderKey, "ORD-1");
        first.put("id", "1");
        Map<String, Object> second = params(orderKey, "ORD-2");
        second.put("id", "2");
        assertEquals(1, ((Number) session.executeStatement("redis.Coverage.order", first)).intValue());
        assertEquals(1, ((Number) session.executeStatement("redis.Coverage.order", second)).intValue());
        List<Map<String, Object>> orders = session.queryStatement("redis.Coverage.orders", params(orderKey, null));
        assertEquals(2, orders.size());
        Map<String, Object> actual = new HashMap<>();
        for (Map<String, Object> row : orders) {
            actual.put((String) row.get("FIELD"), row.get("VALUE"));
        }
        assertEquals("ORD-1", actual.get("1"));
        assertEquals("ORD-2", actual.get("2"));
        assertEquals(1, ((Number) session.executeStatement("redis.Coverage.deleteOrder", first)).intValue());
        List<Map<String, Object>> remaining = session.queryStatement("redis.Coverage.orders", params(orderKey, null));
        assertEquals(1, remaining.size());
        assertEquals("2", remaining.get(0).get("FIELD"));
        assertEquals("ORD-2", remaining.get(0).get("VALUE"));
        assertEquals(Arrays.asList("mali"), session.queryStatement("redis.Coverage.ownerRead", params(ownerKey, null)));
    }
}
