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

public class RedisAnnotationMapperSelectKeyTest extends RedisNativeMapperSupport {
    private Generated generated(String suffix) {
        Generated value = new Generated();
        value.setKey(key(suffix));
        value.setCounter(key(suffix + "-counter"));
        value.setValue("data");
        return value;
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SELECT_KEY)
    public void selectKey() throws Exception {
        Generated before = generated("before");
        assertEquals(1, mapper().before(before));
        assertEquals(Long.valueOf(1), before.getId());
        assertEquals("data", session.jdbc().queryForString("HGET ? ?", new Object[] { before.getKey(), before.getId() }));
        Generated after = generated("after");
        assertEquals(1, mapper().after(after));
        assertEquals(Long.valueOf(1), after.getId());
        assertEquals("data", session.jdbc().queryForString("LINDEX ? ?", new Object[] { after.getKey(), after.getId() - 1 }));
    }
}
