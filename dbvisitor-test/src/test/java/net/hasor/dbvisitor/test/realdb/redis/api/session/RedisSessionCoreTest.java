/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.session;

import java.util.*;
import net.hasor.dbvisitor.session.*;
import net.hasor.dbvisitor.test.nxn.capability.*;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisSessionCoreTest extends RedisNativeMapperSupport {


    @Before
    public void loadStatements() throws Exception {
        loadXml();
    }

    @Test
    @Capability(CapabilityId.SESSION_LIFECYCLE)
    public void lifecycle() throws Exception {
        Configuration configuration = new Configuration();
        Session local = configuration.newSession(OneApiDataSourceManager.getConnection("redis"));
        assertSame(configuration, local.getConfiguration());
        try {
            String k = key("life");
            NativeMapper nativeMapper = local.createMapper(NativeMapper.class);
            nativeMapper.put(k, "v");
            assertEquals("v", nativeMapper.get(k));
        } finally {
            local.close();
            local.close();
        }
    }

    @Test
    @Capability(CapabilityId.SESSION_COMPONENT_JDBC)
    public void jdbc() throws Exception {
        String k = key("jdbc");
        assertNotNull(session.jdbc());
        assertEquals(1, session.jdbc().executeUpdate("SET ? ?", new Object[] { k, "v" }));
        assertEquals("v", session.jdbc().queryForString("GET ?", k));
    }

    @Test
    @Capability(CapabilityId.SESSION_BASEMAPPER_NAMESPACE)
    public void namespace() throws Exception {
        assertNotNull(session.createBaseMapper(Entry.class, "redis.custom"));
        assertNotNull(session.getConfiguration().findBySpace("redis.custom", Entry.class));
    }

    @Test
    @Capability(CapabilityId.SESSION_CONFIGURATION_ACCESSORS)
    public void configuration() throws Exception {
        Configuration c = session.getConfiguration();
        assertNotNull(c.options());
        assertNotNull(c.getTypeRegistry());
        assertNotNull(c.getMacroRegistry());
        assertNotNull(c.getRuleRegistry());
        assertNotNull(c.getMapperRegistry());
        assertNotNull(c.getMappingRegistry());
        assertNotNull(c.getClassLoader());
        c.loadEntityToSpace(Entry.class);
        assertNotNull(c.findByEntity(Entry.class));
        assertEquals(Entry.class, c.loadClass(Entry.class.getName()));
        assertThrows(ClassNotFoundException.class, () -> c.loadClass("redis.Missing"));
    }

    @Test
    @Capability(CapabilityId.SESSION_MULTI_SESSION)
    public void multipleSessions() throws Exception {
        Configuration c = session.getConfiguration();
        try (Session second = c.newSession(OneApiDataSourceManager.getConnection("redis"))) {
            assertNotSame(session, second);
            assertSame(c, second.getConfiguration());
            String k = key("shared");
            mapper().put(k, "shared");
            assertEquals("shared", second.createMapper(NativeMapper.class).get(k));
        }
    }
}
