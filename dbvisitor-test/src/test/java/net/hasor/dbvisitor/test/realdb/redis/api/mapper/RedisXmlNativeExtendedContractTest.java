/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.util.*;
import org.junit.Test;
import net.hasor.dbvisitor.test.nxn.capability.*;
import static org.junit.Assert.*;

public class RedisXmlNativeExtendedContractTest extends RedisNativeMapperSupport {

    private static final String NS = "net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisExtendedMapper.";

    private RedisExtendedMapper extended() throws Exception {
        return session.createMapper(RedisExtendedMapper.class);
    }

    private String list() throws Exception {
        String k = key("list");
        session.jdbc().executeUpdate("RPUSH ? a b", k);
        extended();
        return k;
    }

    private String hash() throws Exception {
        String k = key("hash");
        session.jdbc().executeUpdate("HSET ? name mali age 18", k);
        return k;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_LOAD)
    public void load() throws Exception {
        assertEquals(Arrays.asList("a", "b"), session.queryStatement(NS + "included", params(list(), null)));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_NAMESPACE_MULTIPLE)
    public void namespaces() throws Exception {
        loadXml();
        String k = key("ns");
        extended().put(k, "v");
        assertEquals(Arrays.asList("v"), session.queryStatement("redis.Native.get", params(k, null)));
        assertEquals(Arrays.asList("v"), session.queryStatement(NS + "get", params(k, null)));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CRUD_EXECUTE)
    public void execute() throws Exception {
        String k = key("execute");
        extended().put(k, "v");
        assertEquals(1, ((Number) session.executeStatement(NS + "remove", params(k, null))).intValue());
        assertNull(extended().get(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_CRUD)
    public void crud() throws Exception {
        RedisExtendedMapper m = extended();
        String k = key("crud");
        assertEquals(1, m.put(k, "a"));
        assertEquals("a", m.get(k));
        assertEquals(1, m.replace(k, "b"));
        assertEquals("b", m.get(k));
        assertEquals(1, m.remove(k));
        assertNull(m.get(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_PARAMETER)
    public void parameters() throws Exception {
        RedisExtendedMapper m = extended();
        String k = key("param");
        m.put(k, "v");
        Generated bean = new Generated();
        bean.setKey(k);
        assertEquals("v", m.bean(bean));
        assertEquals("v", m.map(params(k, null)));
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

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_PARTIAL)
    public void partial() throws Exception {
        List<Entry> rows = extended().partial(hash());
        assertEquals(2, rows.size());
        Set<String> fields = new HashSet<>();
        for (Entry row : rows) {
            fields.add(row.getField());
            assertNull(row.getValue());
        }
        assertEquals(new HashSet<>(Arrays.asList("name", "age")), fields);
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_PARTIAL_LIST)
    public void partialList() throws Exception {
        List<Entry> rows = extended().partial(hash());
        assertEquals(2, rows.size());
        for (Entry row : rows) {
            assertNotNull(row.getField());
            assertNull(row.getValue());
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_CASE_INSENSITIVE)
    public void caseInsensitive() throws Exception {
        List<Entry> rows = extended().partial(hash());
        assertEquals(2, rows.size());
        for (Entry row : rows) {
            assertNotNull(row.getField());
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_AUTO_MAPPING)
    public void autoMapping() throws Exception {
        List<Entry> rows = extended().automatic(hash());
        assertEquals(2, rows.size());
        Map<String, String> actual = new HashMap<>();
        for (Entry row : rows) {
            actual.put(row.getField(), row.getValue());
        }
        assertEquals("mali", actual.get("name"));
        assertEquals("18", actual.get("age"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_RENAMED_MAP)
    public void rename() throws Exception {
        List<Map<String, Object>> rows = extended().renamed(hash());
        assertEquals(2, rows.size());
        Map<String, Object> actual = new HashMap<>();
        for (Map<String, Object> row : rows) {
            assertEquals(new HashSet<>(Arrays.asList("FIELD", "VALUE")), row.keySet());
            actual.put((String) row.get("FIELD"), row.get("VALUE"));
        }
        assertEquals("mali", actual.get("name"));
        assertEquals("18", actual.get("age"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_RESULT_MAP)
    public void refResultMap() throws Exception {
        List<Entry> rows = extended().automatic(hash());
        assertEquals(2, rows.size());
        for (Entry row : rows) {
            assertNotNull(row.getField());
            assertNotNull(row.getValue());
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_FRAGMENT_MULTIPLE)
    public void fragments() throws Exception {
        assertEquals(Arrays.asList("a", "b"), extended().included(list()));
    }
}
