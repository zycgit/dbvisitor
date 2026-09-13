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

public class RedisXmlMapperResultMapTest extends RedisNativeMapperSupport {
    private Map<String, Object> seedHash() throws Exception {
        Map<String, Object> p = params(key("hash"), null);
        session.jdbc().executeUpdate("HSET ? name mali age 18", p.get("key"));
        return p;
    }

    private static final String NS = "redis.Native.";

    private RedisExtendedMapper extended() throws Exception {
        return session.createMapper(RedisExtendedMapper.class);
    }

    private String hash() throws Exception {
        String k = key("hash");
        session.jdbc().executeUpdate("HSET ? name mali age 18", k);
        return k;
    }

    @Before
    public void loadStatements() throws Exception {
        loadXml();
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
}
