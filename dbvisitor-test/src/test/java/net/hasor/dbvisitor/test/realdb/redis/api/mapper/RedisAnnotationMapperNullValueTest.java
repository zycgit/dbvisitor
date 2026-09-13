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
import net.hasor.dbvisitor.test.realdb.redis.dto1.RedisParameterUser;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisAnnotationMapperNullValueTest extends RedisNativeMapperSupport {
    private RedisCoverageMapper coverage() throws Exception {
        return session.createMapper(RedisCoverageMapper.class);
    }

    private RedisParameterUser user(int id, String name) {
        RedisParameterUser user = new RedisParameterUser();
        user.setId(id);
        user.setName(name);
        return user;
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_NULL_VALUE)
    public void jsonBean_shouldPreserveNullProperties() throws Exception {
        RedisCoverageMapper mapper = coverage();
        String key = key("null-properties");
        RedisParameterUser value = user(1, "nullable");
        value.setAge(null);
        value.setEmail(null);
        assertEquals(1, mapper.putBean(key, value));
        RedisParameterUser actual = mapper.bean(key);
        assertNotNull(actual);
        assertEquals(Integer.valueOf(1), actual.getId());
        assertEquals("nullable", actual.getName());
        assertNull(actual.getAge());
        assertNull(actual.getEmail());
    }
}
