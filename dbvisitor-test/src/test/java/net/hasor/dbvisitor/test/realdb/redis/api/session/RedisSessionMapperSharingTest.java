/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.session;

import java.util.*;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.*;
import net.hasor.dbvisitor.test.nxn.capability.*;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisSessionMapperSharingTest extends RedisNativeMapperSupport {
    private static final String NS = "redis.Native.";

    @Before
    public void loadStatements() throws Exception {
        loadXml();
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_DECLARATIVE)
    public void declarative() throws Exception {
        String k = key("declarative");
        NativeMapper m = mapper();
        assertEquals(1, m.put(k, "v"));
        assertEquals("v", m.get(k));
        assertEquals(Arrays.asList("v"), session.createBaseMapper(Entry.class).queryStatement(NS + "get", params(k, null)));
        assertEquals(1, m.remove(k));
        assertNull(m.get(k));
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_MIXED)
    public void mixed() throws Exception {
        String k = key("mixed");
        BaseMapper<Entry> base = session.createBaseMapper(Entry.class);
        base.executeStatement(NS + "put", params(k, "a"));
        assertEquals(Arrays.asList("a"), session.queryStatement(NS + "get", params(k, null)));
        session.executeStatement(NS + "replace", params(k, "b"));
        assertEquals("b", mapper().get(k));
        assertEquals("b", session.createMapper(RefNativeMapper.class).get(k));
    }
}
