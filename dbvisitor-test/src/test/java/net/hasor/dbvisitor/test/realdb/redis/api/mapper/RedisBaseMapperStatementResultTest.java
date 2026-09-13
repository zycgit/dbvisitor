/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.util.*;
import net.hasor.dbvisitor.session.*;
import net.hasor.dbvisitor.test.nxn.capability.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisBaseMapperStatementResultTest extends RedisNativeMapperSupport {
    private static final String NS = "redis.Native.";

    @Before
    public void loadStatements() throws Exception {
        loadXml();
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_QUERY_RESULT)
    public void baseResults() throws Exception {
        String k = key("list");
        session.jdbc().executeUpdate("RPUSH ? a b", k);
        assertEquals(Arrays.asList("a", "b"), session.createBaseMapper(Entry.class).queryStatement(NS + "list", params(k, null)));
    }
}
