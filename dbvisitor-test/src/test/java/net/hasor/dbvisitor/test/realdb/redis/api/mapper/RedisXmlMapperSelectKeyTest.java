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

public class RedisXmlMapperSelectKeyTest extends RedisNativeMapperSupport {
    private Generated generated(String suffix) {
        Generated value = new Generated();
        value.setKey(key(suffix));
        value.setCounter(key(suffix + "-counter"));
        value.setValue("data");
        return value;
    }

    private static final String NS = "redis.Native.";

    @Before
    public void loadStatements() throws Exception {
        loadXml();
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_SELECT_KEY_BEFORE)
    public void before() throws Exception {
        Generated value = generated("before");
        session.executeStatement(NS + "before", value);
        assertEquals(Long.valueOf(1), value.getId());
        assertEquals("data", session.jdbc().queryForString("HGET ? ?", new Object[] { value.getKey(), value.getId() }));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_SELECT_KEY_AFTER)
    public void after() throws Exception {
        Generated value = generated("after");
        session.executeStatement(NS + "after", value);
        assertEquals(Long.valueOf(1), value.getId());
        assertEquals("data", session.jdbc().queryForString("LINDEX ? ?", new Object[] { value.getKey(), value.getId() - 1 }));
    }
}
