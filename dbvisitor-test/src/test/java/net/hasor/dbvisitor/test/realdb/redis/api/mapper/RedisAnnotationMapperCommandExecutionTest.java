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

public class RedisAnnotationMapperCommandExecutionTest extends RedisNativeMapperSupport {


    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_EXECUTE)
    public void execute() throws Exception {
        String k = key("exec");
        assertEquals(1, mapper().execute(k, "value"));
        assertEquals("value", mapper().get(k));
    }
}
