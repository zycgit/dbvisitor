/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.util.*;
import net.hasor.dbvisitor.test.nxn.capability.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisXmlRefMapperTemplateTest extends RedisNativeMapperSupport {
    private RedisExtendedMapper extended() throws Exception {
        return session.createMapper(RedisExtendedMapper.class);
    }

    private String list() throws Exception {
        String k = key("list");
        session.jdbc().executeUpdate("RPUSH ? a b", k);
        extended();
        return k;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_FOREACH)
    public void foreach() throws Exception {
        RedisExtendedMapper m = extended();
        String a = key("a"), b = key("b");
        m.put(a, "A");
        m.put(b, "B");
        List<Map<String, Object>> rows = m.many(Arrays.asList(a, b));
        assertEquals(2, rows.size());
        assertEquals("A", rows.get(0).get("VALUE"));
        assertEquals("B", rows.get(1).get("VALUE"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_DYNAMIC)
    public void dynamic() throws Exception {
        RedisExtendedMapper m = extended();
        String k = list();
        assertEquals(Arrays.asList("a"), m.choose(k, true));
        assertEquals(Arrays.asList("a", "b"), m.choose(k, false));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_TEXT_AND_MAP)
    public void text() throws Exception {
        RedisExtendedMapper m = extended();
        String k = key("rank");
        session.jdbc().queryForLong("ZADD ? 1 a 2 b", k);
        assertEquals(Arrays.asList("a", "b"), m.direction(k, "ZRANGE"));
        assertEquals(Arrays.asList("b", "a"), m.direction(k, "ZREVRANGE"));
    }
}
