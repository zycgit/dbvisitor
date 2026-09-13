/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.util.*;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.*;
import net.hasor.dbvisitor.test.nxn.capability.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisBaseMapperStatementMutationTest extends RedisNativeMapperSupport {
    private int count(Object value) {
        return ((Number) value).intValue();
    }

    private static final String NS = "redis.Native.";

    @Before
    public void loadStatements() throws Exception {
        loadXml();
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
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_BATCH_DELETE)
    public void baseBulkDelete() throws Exception {
        String a = key("a"), b = key("b");
        mapper().put(a, "a");
        mapper().put(b, "b");
        assertEquals(2, count(session.createBaseMapper(Entry.class).executeStatement(NS + "manyRemove", Collections.singletonMap("keyList", Arrays.asList(a, b)))));
        assertNull(mapper().get(a));
        assertNull(mapper().get(b));
    }
}
