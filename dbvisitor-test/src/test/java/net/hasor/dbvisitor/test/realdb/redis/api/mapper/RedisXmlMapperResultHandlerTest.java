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

public class RedisXmlMapperResultHandlerTest extends RedisNativeMapperSupport {
    private Map<String, Object> seedList() throws Exception {
        Map<String, Object> p = params(key("list"), null);
        session.jdbc().executeUpdate("RPUSH ? first second", p.get("key"));
        return p;
    }

    private static final String NS = "redis.Native.";

    private Map<String, Object> seedHash() throws Exception {
        Map<String, Object> p = params(key("hash"), null);
        session.jdbc().executeUpdate("HSET ? name mali age 18", p.get("key"));
        return p;
    }

    @Before
    public void loadStatements() throws Exception {
        loadXml();
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
}
