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

public class RedisXmlMapperStatementAttributeTest extends RedisNativeMapperSupport {
    private static final String NS = "redis.Native.";

    private Map<String, Object> seedList() throws Exception {
        Map<String, Object> p = params(key("list"), null);
        session.jdbc().executeUpdate("RPUSH ? first second", p.get("key"));
        return p;
    }

    @Before
    public void loadStatements() throws Exception {
        loadXml();
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
}
