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

public class RedisBaseMapperStatementLookupTest extends RedisNativeMapperSupport {
    private static final String NS = "redis.Native.";

    @Before
    public void loadStatements() throws Exception {
        loadXml();
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_INVALID_ID)
    public void baseInvalidStatement() throws Exception {
        BaseMapper<Entry> base = session.createBaseMapper(Entry.class);
        assertThrows(RuntimeException.class, () -> base.executeStatement(NS + "missing", null));
        assertThrows(RuntimeException.class, () -> base.queryStatement(NS + "missing", null));
    }
}
