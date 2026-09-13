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

public class RedisAnnotationMapperResultHandlerTest extends RedisNativeMapperSupport {
    private String hash() throws Exception {
        String k = key("hash");
        session.jdbc().executeUpdate("HSET ? name mali age 18", k);
        return k;
    }

    private String list() throws Exception {
        String k = key("list");
        session.jdbc().executeUpdate("RPUSH ? first second", k);
        return k;
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_DEFAULT)
    public void defaultRows() throws Exception {
        assertEquals(2, mapper().entries(hash()).size());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER)
    public void rowMapper() throws Exception {
        assertEquals(Arrays.asList("0:first", "1:second"), mapper().mapped(list()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER_OPTIONS)
    public void rowMapperOptions() throws Exception {
        assertEquals(Arrays.asList("0:first", "1:second"), mapper().mappedOptions(list()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER_SINGLE)
    public void rowMapperSingle() throws Exception {
        assertEquals("0:first", mapper().first(list()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR)
    public void extractor() throws Exception {
        assertEquals(Arrays.asList("first", "second"), mapper().extracted(list()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_CALLBACK)
    public void callback() throws Exception {
        ValuesCallback.VALUES.get().clear();
        try {
            mapper().callback(list());
            assertEquals(Arrays.asList("first", "second"), ValuesCallback.VALUES.get());
        } finally {
            ValuesCallback.VALUES.remove();
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EMPTY)
    public void emptyHandlers() throws Exception {
        String k = key("empty");
        assertTrue(mapper().mapped(k).isEmpty());
        assertTrue(mapper().extracted(k).isEmpty());
        assertNull(mapper().firstRow(k));
    }
}
