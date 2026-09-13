/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.util.*;
import net.hasor.dbvisitor.test.realdb.redis.dto1.RedisParameterUser;
import org.junit.Test;
import net.hasor.dbvisitor.test.nxn.capability.*;
import static org.junit.Assert.*;

public class RedisAnnotationNativeContractTest extends RedisNativeMapperSupport {

    private String list() throws Exception {
        String k = key("list");
        session.jdbc().executeUpdate("RPUSH ? first second", k);
        return k;
    }

    private String hash() throws Exception {
        String k = key("hash");
        session.jdbc().executeUpdate("HSET ? name mali age 18", k);
        return k;
    }

    private Generated generated(String suffix) {
        Generated value = new Generated();
        value.setKey(key(suffix));
        value.setCounter(key(suffix + "-counter"));
        value.setValue("data");
        return value;
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_INSERT_NAMED_PARAMS)
    public void namedAssembly() throws Exception {
        String k = key("named");
        assertEquals(1, mapper().put(k, "a b\"中"));
        assertEquals("a b\"中", mapper().get(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_POSITIONAL)
    public void positional() throws Exception {
        String k = key("pos");
        assertEquals(1, mapper().positional(k, "value"));
        assertEquals("value", mapper().get(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_EXECUTE)
    public void execute() throws Exception {
        String k = key("exec");
        assertEquals(1, mapper().execute(k, "value"));
        assertEquals("value", mapper().get(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_RANGE)
    public void range() throws Exception {
        String k = key("range");
        session.jdbc().queryForObject("ZADD ? 10 a 20 b 30 c", new Object[] { k }, Long.class);
        assertEquals(Arrays.asList("b", "c"), mapper().range(k, 20, 30));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_NO_MATCH_AFFECTED_ROWS)
    public void missingMutations() throws Exception {
        String k = key("missing");
        assertEquals(0, mapper().replace(k, "v"));
        assertEquals(0, mapper().remove(k));
        assertNull(mapper().get(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_MULTILINE)
    public void multiline() throws Exception {
        String k = key("multiline");
        mapper().multiline(k, "assembled");
        assertEquals("assembled", mapper().get(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_TIMEOUT)
    public void timeout() throws Exception {
        String k = key("timeout");
        mapper().put(k, "v");
        assertEquals("v", mapper().options(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_FETCH_SIZE)
    public void fetchSize() throws Exception {
        String k = key("fetch");
        mapper().put(k, "v");
        assertEquals("v", mapper().options(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_COMBINED)
    public void combined() throws Exception {
        String k = key("combined");
        mapper().put(k, "v");
        assertEquals("v", mapper().options(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_STATEMENT_TYPE)
    public void statementType() throws Exception {
        String k = key("stmt");
        mapper().put(k, "v");
        assertEquals("v", mapper().get(k));
        assertEquals("PONG", mapper().statement(k));
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
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_DEFAULT)
    public void defaultRows() throws Exception {
        assertEquals(2, mapper().entries(hash()).size());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER)
    public void rowMapper() throws Exception {
        assertEquals(Arrays.asList("0:first", "1:second"), mapper().mapped(list()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER_OPTIONS)
    public void rowMapperOptions() throws Exception {
        assertEquals(Arrays.asList("0:first", "1:second"), mapper().mappedOptions(list()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER_SINGLE)
    public void rowMapperSingle() throws Exception {
        assertEquals("0:first", mapper().first(list()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR)
    public void extractor() throws Exception {
        assertEquals(Arrays.asList("first", "second"), mapper().extracted(list()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR_OPTIONS)
    public void extractorOptions() throws Exception {
        assertEquals(Arrays.asList("first", "second"), mapper().extractedOptions(list()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_CALLBACK)
    public void callback() throws Exception {
        ValuesCallback.VALUES.get().clear();
        try {
            mapper().callback(list());
            assertEquals(Arrays.asList("first", "second"), ValuesCallback.VALUES.get());
        } finally {
            ValuesCallback.VALUES.remove();
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EMPTY)
    public void emptyHandlers() throws Exception {
        String k = key("empty");
        assertTrue(mapper().mapped(k).isEmpty());
        assertTrue(mapper().extracted(k).isEmpty());
        assertNull(mapper().firstRow(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SELECT_KEY)
    public void selectKey() throws Exception {
        Generated before = generated("before");
        assertEquals(1, mapper().before(before));
        assertEquals(Long.valueOf(1), before.getId());
        assertEquals("data", session.jdbc().queryForString("HGET ? ?", new Object[] { before.getKey(), before.getId() }));
        Generated after = generated("after");
        assertEquals(1, mapper().after(after));
        assertEquals(Long.valueOf(1), after.getId());
        assertEquals("data", session.jdbc().queryForString("LINDEX ? ?", new Object[] { after.getKey(), after.getId() - 1 }));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_RESULT_SET_KEY_SOURCE)
    public void resultSetKey() throws Exception {
        Generated value = generated("result");
        mapper().resultKey(value);
        assertEquals(Long.valueOf(1), value.getId());
        mapper().resultKey(value);
        assertEquals(Long.valueOf(2), value.getId());
        assertEquals(Long.valueOf(2), session.jdbc().queryForLong("GET ?", value.getCounter()));
    }
}
