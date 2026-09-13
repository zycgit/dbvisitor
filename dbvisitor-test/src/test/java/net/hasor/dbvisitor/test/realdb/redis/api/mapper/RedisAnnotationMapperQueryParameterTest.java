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

public class RedisAnnotationMapperQueryParameterTest extends RedisNativeMapperSupport {


    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_RANGE)
    public void range() throws Exception {
        String k = key("range");
        session.jdbc().queryForObject("ZADD ? 10 a 20 b 30 c", new Object[] { k }, Long.class);
        assertEquals(Arrays.asList("b", "c"), mapper().range(k, 20, 30));
    }
}
