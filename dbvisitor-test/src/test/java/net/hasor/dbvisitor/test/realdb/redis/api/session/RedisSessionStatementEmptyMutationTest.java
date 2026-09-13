/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.session;

import java.util.*;
import net.hasor.dbvisitor.session.*;
import net.hasor.dbvisitor.test.nxn.capability.*;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisSessionStatementEmptyMutationTest extends RedisNativeMapperSupport {
    private int count(Object value) {
        return ((Number) value).intValue();
    }

    private static final String NS = "redis.Native.";

    @Before
    public void loadStatements() throws Exception {
        loadXml();
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
}
