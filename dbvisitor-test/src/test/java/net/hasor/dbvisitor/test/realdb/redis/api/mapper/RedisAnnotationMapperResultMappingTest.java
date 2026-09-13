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
import net.hasor.dbvisitor.test.realdb.redis.dto1.RedisParameterUser;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisAnnotationMapperResultMappingTest extends RedisNativeMapperSupport {
    private String hash() throws Exception {
        String k = key("hash");
        session.jdbc().executeUpdate("HSET ? name mali age 18", k);
        return k;
    }

    private String list() throws Exception {
        String k = key("list");
        session.jdbc().executeUpdate("RPUSH ? first second", k);
        return k;
    }

    private RedisCoverageMapper coverage() throws Exception {
        return session.createMapper(RedisCoverageMapper.class);
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_ENTITY)
    public void entity() throws Exception {
        String k = hash();
        List<Entry> rows = mapper().entries(k);
        assertEquals(2, rows.size());
        Map<String, String> values = new HashMap<>();
        for (Entry row : rows) {
            values.put(row.getField(), row.getValue());
        }
        assertEquals("18", values.get("age"));
        assertEquals("mali", values.get("name"));
        List<Entry> partial = mapper().partialEntries(k);
        assertEquals(2, partial.size());
        Set<String> projected = new HashSet<>();
        for (Entry row : partial) {
            projected.add(row.getField());
            assertNull(row.getValue());
        }
        assertEquals(values.keySet(), projected);
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_MAP)
    public void maps() throws Exception {
        List<Map<String, Object>> rows = mapper().maps(hash());
        assertEquals(2, rows.size());
        Set<Object> fields = new HashSet<>();
        for (Map<String, Object> row : rows) {
            assertEquals(2, row.size());
            fields.add(row.get("FIELD"));
        }
        assertEquals(new HashSet<>(Arrays.asList("name", "age")), fields);
        String singleKey = key("single-map");
        session.jdbc().executeUpdate("HSET ? name mali", singleKey);
        Map<String, Object> single = mapper().map(singleKey);
        assertEquals(2, single.size());
        assertEquals("name", single.get("FIELD"));
        assertEquals("mali", single.get("VALUE"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_LIST)
    public void listResult() throws Exception {
        assertEquals(Arrays.asList("first", "second"), mapper().list(list()));
        String numbers = key("numbers");
        session.jdbc().executeUpdate("RPUSH ? 1 2 3", numbers);
        assertEquals(Arrays.asList(1, 2, 3), mapper().integers(numbers));
        List<Entry> entities = mapper().entries(hash());
        assertEquals(2, entities.size());
        for (Entry entry : entities) {
            assertNotNull(entry.getField());
            assertNotNull(entry.getValue());
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_NULL)
    public void missingResults() throws Exception {
        String k = key("absent");
        assertNull(mapper().get(k));
        assertTrue(mapper().list(k).isEmpty());
        assertTrue(mapper().entries(k).isEmpty());
        RedisCoverageMapper coverage = session.createMapper(RedisCoverageMapper.class);
        assertNull(coverage.bean(k));
        RedisParameterUser user = new RedisParameterUser();
        user.setId(1);
        user.setName("nullable");
        coverage.putBean(k, user);
        RedisParameterUser actual = coverage.bean(k);
        assertEquals("nullable", actual.getName());
        assertNull(actual.getAge());
        assertNull(actual.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_SCALAR)
    public void scalar_shouldMapIntegerTextCountAndDate() throws Exception {
        RedisCoverageMapper mapper = coverage();
        String age = key("age");
        String name = key("name");
        String date = key("date");
        String list = key("list");
        mapper().put(age, "23");
        mapper().put(name, "mali");
        java.sql.Date expected = java.sql.Date.valueOf("2024-03-15");
        session.jdbc().executeUpdate("SET ? ?", new Object[] { date, expected });
        session.jdbc().executeUpdate("RPUSH ? a b c", list);
        assertEquals(Integer.valueOf(23), mapper.integer(age));
        assertEquals("mali", mapper().get(name));
        assertEquals(3, mapper.count(list));
        assertEquals(expected, mapper.date(date));
    }
}
