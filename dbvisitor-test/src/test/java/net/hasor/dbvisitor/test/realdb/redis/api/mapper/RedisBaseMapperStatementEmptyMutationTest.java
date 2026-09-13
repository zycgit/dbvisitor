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

public class RedisBaseMapperStatementEmptyMutationTest extends RedisNativeMapperSupport {
    private int count(Object value) {
        return ((Number) value).intValue();
    }

    private static final String NS = "redis.Native.";

    @Before
    public void loadStatements() throws Exception {
        loadXml();
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
}
