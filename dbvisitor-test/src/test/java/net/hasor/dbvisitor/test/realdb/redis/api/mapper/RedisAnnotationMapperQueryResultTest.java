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

public class RedisAnnotationMapperQueryResultTest extends RedisNativeMapperSupport {
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
    @Capability(CapabilityId.MAPPER_ANNOTATION_NATIVE_QUERY)
    public void nativeQuery_shouldReturnObjectListAndScalar() throws Exception {
        RedisCoverageMapper mapper = coverage();
        String key = key("users");
        assertEquals(1, mapper.appendBean(key, user(1, "first")));
        assertEquals(2, mapper.appendBean(key, user(2, "second")));
        List<RedisParameterUser> rows = mapper.beans(key);
        assertEquals(2, rows.size());
        assertEquals(Integer.valueOf(1), rows.get(0).getId());
        assertEquals("first", rows.get(0).getName());
        assertEquals(Integer.valueOf(2), rows.get(1).getId());
        assertEquals("second", rows.get(1).getName());
        assertEquals(2, mapper.count(key));
    }
}
