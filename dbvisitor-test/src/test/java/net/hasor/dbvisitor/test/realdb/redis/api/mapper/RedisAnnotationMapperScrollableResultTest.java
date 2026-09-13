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

public class RedisAnnotationMapperScrollableResultTest extends RedisNativeMapperSupport {
    private String list() throws Exception {
        String k = key("list");
        session.jdbc().executeUpdate("RPUSH ? first second", k);
        return k;
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR_OPTIONS)
    public void extractorOptions() throws Exception {
        assertEquals(Arrays.asList("first", "second"), mapper().extractedOptions(list()));
    }
}
