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

public class RedisAnnotationMapperExecutionTest extends RedisNativeMapperSupport {


    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_MULTILINE)
    public void multiline() throws Exception {
        String k = key("multiline");
        mapper().multiline(k, "assembled");
        assertEquals("assembled", mapper().get(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_TIMEOUT)
    public void timeout() throws Exception {
        String k = key("timeout");
        mapper().put(k, "v");
        assertEquals("v", mapper().options(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_FETCH_SIZE)
    public void fetchSize() throws Exception {
        String k = key("fetch");
        mapper().put(k, "v");
        assertEquals("v", mapper().options(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_COMBINED)
    public void combined() throws Exception {
        String k = key("combined");
        mapper().put(k, "v");
        assertEquals("v", mapper().options(k));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_STATEMENT_TYPE)
    public void statementType() throws Exception {
        String k = key("stmt");
        mapper().put(k, "v");
        assertEquals("v", mapper().get(k));
        assertEquals("PONG", mapper().statement(k));
    }
}
