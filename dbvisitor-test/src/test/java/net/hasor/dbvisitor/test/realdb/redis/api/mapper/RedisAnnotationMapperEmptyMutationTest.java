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

public class RedisAnnotationMapperEmptyMutationTest extends RedisNativeMapperSupport {


    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_NO_MATCH_AFFECTED_ROWS)
    public void missingMutations() throws Exception {
        String k = key("missing");
        assertEquals(0, mapper().replace(k, "v"));
        assertEquals(0, mapper().remove(k));
        assertNull(mapper().get(k));
    }
}
