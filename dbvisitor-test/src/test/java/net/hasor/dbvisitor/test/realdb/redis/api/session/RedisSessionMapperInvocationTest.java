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
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisSessionMapperInvocationTest extends RedisNativeMapperSupport {


    @Before
    public void loadStatements() throws Exception {
        loadXml();
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_SIMPLE)
    public void simpleMapper() throws Exception {
        SimpleNativeMapper mapper = session.createMapper(SimpleNativeMapper.class);
        String k = key("simple");
        assertEquals(1, mapper.put(k, "a"));
        assertEquals("a", mapper.get(k));
        assertEquals(1, mapper.replace(k, "b"));
        assertEquals("b", mapper.get(k));
        assertEquals(1, mapper.remove(k));
        assertNull(mapper.get(k));
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_REF)
    public void refMapper() throws Exception {
        RefNativeMapper mapper = session.createMapper(RefNativeMapper.class);
        String k = key("ref");
        assertEquals(1, mapper.put(k, "a"));
        assertEquals("a", mapper.get(k));
        assertEquals(1, mapper.replace(k, "b"));
        assertEquals("b", mapper.get(k));
        assertEquals(1, mapper.remove(k));
        assertNull(mapper.get(k));
        String list = key("ref-list");
        session.jdbc().executeUpdate("RPUSH ? a b", list);
        assertEquals(Arrays.asList("a", "b"), mapper.list(list));
    }
}
