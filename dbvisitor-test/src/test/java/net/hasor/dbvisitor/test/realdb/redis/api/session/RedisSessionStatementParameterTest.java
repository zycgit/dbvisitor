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

public class RedisSessionStatementParameterTest extends RedisNativeMapperSupport {
    private static final String NS = "redis.Native.";

    @Before
    public void loadStatements() throws Exception {
        loadXml();
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_DYNAMIC_PARAMETER)
    public void parameters() throws Exception {
        String k = key("dynamic");
        Map<String, Object> p = params(k, "v");
        p.put("existing", false);
        session.executeStatement(NS + "conditional", p);
        assertEquals(Arrays.asList("v"), session.queryStatement(NS + "get", p));
        Generated bean = new Generated();
        bean.setKey(k);
        assertEquals(Arrays.asList("v"), session.queryStatement(NS + "get", bean));
    }
}
