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

public class RedisXmlMapperGeneratedKeysTest extends RedisNativeMapperSupport {
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
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_RESULT_SET_SOURCE)
    public void resultKey() throws Exception {
        Generated value = generated("resultKey");
        session.executeStatement(NS + "resultKey", value);
        assertEquals(Long.valueOf(1), value.getId());
        assertEquals("1", mapper().get(value.getCounter()));
    }
}
