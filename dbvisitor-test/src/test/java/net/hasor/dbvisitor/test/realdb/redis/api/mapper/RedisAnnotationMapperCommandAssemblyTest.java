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

public class RedisAnnotationMapperCommandAssemblyTest extends RedisNativeMapperSupport {


    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_INSERT_NAMED_PARAMS)
    public void namedAssembly() throws Exception {
        String k = key("named");
        assertEquals(1, mapper().put(k, "a b\"中"));
        assertEquals("a b\"中", mapper().get(k));
    }
}
