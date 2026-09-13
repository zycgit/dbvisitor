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

public class RedisAnnotationMapperProjectionResultTest extends RedisNativeMapperSupport {
    private RedisCoverageMapper coverage() throws Exception {
        return session.createMapper(RedisCoverageMapper.class);
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_DISTINCT_LIST)
    public void setResult_shouldMapDistinctScalarValues() throws Exception {
        String key = key("distinct");
        session.jdbc().executeUpdate("SADD ? 23 23 28", key);
        List<Integer> values = coverage().distinct(key);
        assertEquals(2, values.size());
        assertEquals(new HashSet<>(Arrays.asList(23, 28)), new HashSet<>(values));
    }
}
