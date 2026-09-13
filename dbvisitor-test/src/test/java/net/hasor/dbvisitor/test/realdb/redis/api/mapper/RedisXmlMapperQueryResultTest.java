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

public class RedisXmlMapperQueryResultTest extends RedisNativeMapperSupport {
    private Map<String, Object> seedHash() throws Exception {
        Map<String, Object> p = params(key("hash"), null);
        session.jdbc().executeUpdate("HSET ? name mali age 18", p.get("key"));
        return p;
    }

    private static final String NS = "redis.Native.";

    @Before
    public void loadStatements() throws Exception {
        loadXml();
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
}
