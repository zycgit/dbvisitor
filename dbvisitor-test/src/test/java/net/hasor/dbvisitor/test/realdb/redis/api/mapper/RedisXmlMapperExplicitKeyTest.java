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
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisXmlMapperExplicitKeyTest extends RedisNativeMapperSupport {


    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_EXPLICIT_ID)
    public void explicitKey_shouldWriteCallerIdentifierWithoutGeneration() throws Exception {
        session.getConfiguration().loadMapper("/mapper/redis/CoverageMapper.xml");
        Generated value = new Generated();
        value.setKey(key("explicit"));
        value.setId(99L);
        value.setValue("explicit-value");
        assertEquals(1, ((Number) session.executeStatement("redis.Coverage.explicit", value)).intValue());
        assertEquals(Long.valueOf(99), value.getId());
        assertEquals(Arrays.asList("explicit-value"), session.queryStatement("redis.Coverage.explicitRead", value));
    }
}
