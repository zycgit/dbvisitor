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

public class RedisSessionStatementLookupTest extends RedisNativeMapperSupport {
    private static final String NS = "redis.Native.";

    @Before
    public void loadStatements() throws Exception {
        loadXml();
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_INVALID_ID)
    public void invalidStatement() throws Exception {
        assertThrows(RuntimeException.class, () -> session.executeStatement(NS + "missing", null));
        assertThrows(RuntimeException.class, () -> session.queryStatement(NS + "missing", null));
    }
}
