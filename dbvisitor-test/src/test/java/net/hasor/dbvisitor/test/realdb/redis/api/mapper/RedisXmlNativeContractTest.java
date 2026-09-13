/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.util.*;
import org.junit.Before;
import org.junit.Test;
import net.hasor.dbvisitor.test.nxn.capability.*;
import static org.junit.Assert.*;

public class RedisXmlNativeContractTest extends RedisNativeMapperSupport {

    private static final String NS = "redis.Native.";

    @Before
    public void xml() throws Exception {
        loadXml();
    }

    private Map<String, Object> seedList() throws Exception {
        Map<String, Object> p = params(key("list"), null);
        session.jdbc().executeUpdate("RPUSH ? first second", p.get("key"));
        return p;
    }

    private Map<String, Object> seedHash() throws Exception {
        Map<String, Object> p = params(key("hash"), null);
        session.jdbc().executeUpdate("HSET ? name mali age 18", p.get("key"));
        return p;
    }

    private Generated generated(String suffix) {
        Generated value = new Generated();
        value.setKey(key(suffix));
        value.setCounter(key(suffix + "-counter"));
        value.setValue("data");
        return value;
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

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_MAP)
    public void map() throws Exception {
        List<Map<String, Object>> rows = session.queryStatement(NS + "maps", seedHash());
        assertEquals(2, rows.size());
        assertEquals(new HashSet<>(Arrays.asList("FIELD", "VALUE")), rows.get(0).keySet());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_SCALAR)
    public void scalar() throws Exception {
        Map<String, Object> p = params(key("scalar"), "scalar");
        session.executeStatement(NS + "put", p);
        assertEquals(Arrays.asList("scalar"), session.queryStatement(NS + "get", p));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_FULL)
    public void resultMap() throws Exception {
        List<Entry> rows = session.queryStatement(NS + "entries", seedHash());
        assertEquals(2, rows.size());
        Map<String, String> values = new HashMap<>();
        for (Entry e : rows) {
            values.put(e.getField(), e.getValue());
        }
        assertEquals("18", values.get("age"));
        assertEquals("mali", values.get("name"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_JAVA_TYPE)
    public void javaType() throws Exception {
        List<Entry> rows = session.queryStatement(NS + "entries", seedHash());
        assertEquals(2, rows.size());
        for (Entry e : rows) {
            assertTrue(e.getValue() instanceof String);
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_HANDLER_ROW_MAPPER)
    public void rowMapper() throws Exception {
        assertEquals(Arrays.asList("0:first", "1:second"), session.queryStatement(NS + "mapped", seedList()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_HANDLER_EXTRACTOR)
    public void extractor() throws Exception {
        assertEquals(Arrays.asList("first", "second"), session.queryStatement(NS + "extracted", seedList()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_HANDLER_RESULT_MAP)
    public void handlerMap() throws Exception {
        List<Entry> rows = session.queryStatement(NS + "entries", seedHash());
        assertEquals(2, rows.size());
        assertNotNull(rows.get(0).getField());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_HANDLER_RESULT_TYPE)
    public void handlerType() throws Exception {
        assertEquals(Arrays.asList("first", "second"), session.queryStatement(NS + "list", seedList()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_TYPE)
    public void statementType() throws Exception {
        Map<String, Object> p = params(key("statement"), "v");
        session.executeStatement(NS + "put", p);
        assertEquals(Arrays.asList("v"), session.queryStatement(NS + "get", p));
        assertEquals(Arrays.asList("PONG"), session.queryStatement(NS + "statement", p));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_TIMEOUT)
    public void timeout() throws Exception {
        assertEquals(Arrays.asList("first", "second"), session.queryStatement(NS + "options", seedList()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_FETCH_SIZE)
    public void fetchSize() throws Exception {
        assertEquals(Arrays.asList("first", "second"), session.queryStatement(NS + "options", seedList()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_FORWARD_ONLY)
    public void forwardOnly() throws Exception {
        assertEquals(Arrays.asList("first", "second"), session.queryStatement(NS + "options", seedList()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_COMBINED)
    public void combined() throws Exception {
        assertEquals(Arrays.asList("first", "second"), session.queryStatement(NS + "options", seedList()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_SELECT_KEY_BEFORE)
    public void before() throws Exception {
        Generated value = generated("before");
        session.executeStatement(NS + "before", value);
        assertEquals(Long.valueOf(1), value.getId());
        assertEquals("data", session.jdbc().queryForString("HGET ? ?", new Object[] { value.getKey(), value.getId() }));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_SELECT_KEY_AFTER)
    public void after() throws Exception {
        Generated value = generated("after");
        session.executeStatement(NS + "after", value);
        assertEquals(Long.valueOf(1), value.getId());
        assertEquals("data", session.jdbc().queryForString("LINDEX ? ?", new Object[] { value.getKey(), value.getId() - 1 }));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_RESULT_SET_SOURCE)
    public void resultKey() throws Exception {
        Generated value = generated("resultKey");
        session.executeStatement(NS + "resultKey", value);
        assertEquals(Long.valueOf(1), value.getId());
        assertEquals("1", mapper().get(value.getCounter()));
    }
}
