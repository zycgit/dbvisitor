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

public class RedisXmlRefMapperResultMapTest extends RedisNativeMapperSupport {
    private RedisExtendedMapper extended() throws Exception {
        return session.createMapper(RedisExtendedMapper.class);
    }

    private String hash() throws Exception {
        String k = key("hash");
        session.jdbc().executeUpdate("HSET ? name mali age 18", k);
        return k;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_RESULT_MAP)
    public void refResultMap() throws Exception {
        List<Entry> rows = extended().automatic(hash());
        assertEquals(2, rows.size());
        for (Entry row : rows) {
            assertNotNull(row.getField());
            assertNotNull(row.getValue());
        }
    }
}
