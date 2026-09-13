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

public class RedisXmlMapperStatementAccessTest extends RedisNativeMapperSupport {
    private String list() throws Exception {
        String k = key("list");
        session.jdbc().executeUpdate("RPUSH ? a b", k);
        extended();
        return k;
    }

    private static final String NS = "net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisExtendedMapper.";

    private RedisExtendedMapper extended() throws Exception {
        return session.createMapper(RedisExtendedMapper.class);
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
}
