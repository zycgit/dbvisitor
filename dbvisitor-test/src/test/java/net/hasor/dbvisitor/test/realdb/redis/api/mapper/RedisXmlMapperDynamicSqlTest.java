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
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisXmlMapperDynamicSqlTest extends RedisNativeMapperSupport {
    private static final String NS = "redis.Native.";

    private Map<String, Object> seedList() throws Exception {
        Map<String, Object> p = params(key("list"), null);
        session.jdbc().executeUpdate("RPUSH ? first second", p.get("key"));
        return p;
    }

    @Before
    public void loadStatements() throws Exception {
        loadXml();
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_IF)
    public void conditional() throws Exception {
        Map<String, Object> p = params(key("if"), "v");
        p.put("existing", true);
        assertEquals(0, ((Number) session.executeStatement(NS + "conditional", p)).intValue());
        p.put("existing", false);
        assertEquals(1, ((Number) session.executeStatement(NS + "conditional", p)).intValue());
        assertEquals(Arrays.asList("v"), session.queryStatement(NS + "get", p));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_CHOOSE)
    public void choose() throws Exception {
        Map<String, Object> p = seedList();
        p.put("single", true);
        assertEquals(Arrays.asList("first"), session.queryStatement(NS + "choose", p));
        p.put("single", false);
        assertEquals(Arrays.asList("first", "second"), session.queryStatement(NS + "choose", p));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_FOREACH)
    public void foreach() throws Exception {
        Map<String, Object> a = params(key("a"), "A"), b = params(key("b"), "B");
        session.executeStatement(NS + "put", a);
        session.executeStatement(NS + "put", b);
        List<Map<String, Object>> rows = session.queryStatement(NS + "many", Collections.singletonMap("keyList", Arrays.asList(a.get("key"), b.get("key"))));
        assertEquals(2, rows.size());
        assertEquals("A", rows.get(0).get("VALUE"));
        assertEquals("B", rows.get(1).get("VALUE"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_FOREACH_WRITE)
    public void foreachWrite() throws Exception {
        Map<String, Object> a = params(key("a"), "A"), b = params(key("b"), "B");
        session.executeStatement(NS + "manyPut", Collections.singletonMap("items", Arrays.asList(a, b)));
        assertEquals(Arrays.asList("A"), session.queryStatement(NS + "get", a));
        assertEquals(Arrays.asList("B"), session.queryStatement(NS + "get", b));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_BIND)
    public void bind() throws Exception {
        String k = key("bound");
        mapper().put(k, "boundValue");
        Map<String, Object> p = new HashMap<>();
        p.put("prefix", k.substring(0, k.length() - 5));
        p.put("suffix", "bound");
        assertEquals(Arrays.asList("boundValue"), session.queryStatement(NS + "bound", p));
    }
}
