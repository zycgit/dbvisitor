/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.session;

import java.util.*;
import org.junit.Before;
import org.junit.Test;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.*;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.capability.*;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport;
import static org.junit.Assert.*;

public class RedisSessionNativeContractTest extends RedisNativeMapperSupport {

    private static final String NS = "redis.Native.";

    @Before
    public void xml() throws Exception {
        loadXml();
    }

    private int count(Object value) {
        return ((Number) value).intValue();
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

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_EXECUTE_DML)
    public void mutation() throws Exception {
        Map<String, Object> p = params(key("mutation"), "a");
        assertEquals(1, count(session.executeStatement(NS + "put", p)));
        p.put("value", "b");
        assertEquals(1, count(session.executeStatement(NS + "replace", p)));
        assertEquals(Arrays.asList("b"), session.queryStatement(NS + "get", p));
        assertEquals(1, count(session.executeStatement(NS + "remove", p)));
        assertEquals(Arrays.asList((String) null), session.queryStatement(NS + "get", p));
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_UPDATE_NO_MATCH)
    public void updateMissing() throws Exception {
        Map<String, Object> p = params(key("missing"), "v");
        assertEquals(0, count(session.executeStatement(NS + "replace", p)));
        assertEquals(Arrays.asList((String) null), session.queryStatement(NS + "get", p));
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_DELETE_NO_MATCH)
    public void deleteMissing() throws Exception {
        assertEquals(0, count(session.executeStatement(NS + "remove", params(key("missing"), null))));
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_QUERY_RESULT)
    public void results() throws Exception {
        String k = key("list");
        session.jdbc().executeUpdate("RPUSH ? a b", k);
        assertEquals(Arrays.asList("a", "b"), session.queryStatement(NS + "list", params(k, null)));
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_DYNAMIC_PARAMETER)
    public void parameters() throws Exception {
        String k = key("dynamic");
        Map<String, Object> p = params(k, "v");
        p.put("existing", false);
        session.executeStatement(NS + "conditional", p);
        assertEquals(Arrays.asList("v"), session.queryStatement(NS + "get", p));
        Generated bean = new Generated();
        bean.setKey(k);
        assertEquals(Arrays.asList("v"), session.queryStatement(NS + "get", bean));
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_INVALID_ID)
    public void invalidStatement() throws Exception {
        assertThrows(RuntimeException.class, () -> session.executeStatement(NS + "missing", null));
        assertThrows(RuntimeException.class, () -> session.queryStatement(NS + "missing", null));
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

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_EXECUTE_DML)
    public void baseMutation() throws Exception {
        BaseMapper<Entry> base = session.createBaseMapper(Entry.class);
        Map<String, Object> p = params(key("base"), "a");
        assertEquals(1, count(base.executeStatement(NS + "put", p)));
        p.put("value", "b");
        assertEquals(1, count(base.executeStatement(NS + "replace", p)));
        assertEquals(Arrays.asList("b"), base.queryStatement(NS + "get", p));
        assertEquals(1, count(base.executeStatement(NS + "remove", p)));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_UPDATE_NO_MATCH)
    public void baseUpdateMissing() throws Exception {
        assertEquals(0, count(session.createBaseMapper(Entry.class).executeStatement(NS + "replace", params(key("missing"), "v"))));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_DELETE_NO_MATCH)
    public void baseDeleteMissing() throws Exception {
        assertEquals(0, count(session.createBaseMapper(Entry.class).executeStatement(NS + "remove", params(key("missing"), null))));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_QUERY_RESULT)
    public void baseResults() throws Exception {
        String k = key("list");
        session.jdbc().executeUpdate("RPUSH ? a b", k);
        assertEquals(Arrays.asList("a", "b"), session.createBaseMapper(Entry.class).queryStatement(NS + "list", params(k, null)));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_BATCH_DELETE)
    public void baseBulkDelete() throws Exception {
        String a = key("a"), b = key("b");
        mapper().put(a, "a");
        mapper().put(b, "b");
        assertEquals(2, count(session.createBaseMapper(Entry.class).executeStatement(NS + "manyRemove", Collections.singletonMap("keyList", Arrays.asList(a, b)))));
        assertNull(mapper().get(a));
        assertNull(mapper().get(b));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_INVALID_ID)
    public void baseInvalidStatement() throws Exception {
        BaseMapper<Entry> base = session.createBaseMapper(Entry.class);
        assertThrows(RuntimeException.class, () -> base.executeStatement(NS + "missing", null));
        assertThrows(RuntimeException.class, () -> base.queryStatement(NS + "missing", null));
    }
}
