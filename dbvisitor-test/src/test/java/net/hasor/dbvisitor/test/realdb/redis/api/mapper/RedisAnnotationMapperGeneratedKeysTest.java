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

public class RedisAnnotationMapperGeneratedKeysTest extends RedisNativeMapperSupport {
    private Generated generated(String suffix) {
        Generated value = new Generated();
        value.setKey(key(suffix));
        value.setCounter(key(suffix + "-counter"));
        value.setValue("data");
        return value;
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_RESULT_SET_KEY_SOURCE)
    public void resultSetKey() throws Exception {
        Generated value = generated("result");
        mapper().resultKey(value);
        assertEquals(Long.valueOf(1), value.getId());
        mapper().resultKey(value);
        assertEquals(Long.valueOf(2), value.getId());
        assertEquals(Long.valueOf(2), session.jdbc().queryForLong("GET ?", value.getCounter()));
    }
}
