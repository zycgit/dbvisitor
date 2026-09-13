/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.scenario;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import net.hasor.dbvisitor.test.realdb.redis.scenario.mapper.ArticleLikeMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArticleLikeScenarioTest extends RedisScenarioSupport {
    @Test
    public void keepsLikesUniqueWithoutAssumingMemberOrder() throws Exception {
        String key = key("likes:article:a1001");
        ArticleLikeMapper mapper = this.session.createMapper(ArticleLikeMapper.class);
        assertEquals(0L, mapper.count(key));
        assertEquals(0L, mapper.contains(key, "u1001"));
        assertEquals(1, mapper.like(key, "u1001"));
        assertEquals(0, mapper.like(key, "u1001"));
        assertEquals(1, mapper.like(key, "u1002"));
        assertEquals(2L, mapper.count(key));
        assertEquals(1L, mapper.contains(key, "u1001"));
        List<String> users = mapper.users(key);
        assertEquals(2, users.size());
        assertEquals(new HashSet<>(Arrays.asList("u1001", "u1002")), new HashSet<>(users));
        assertEquals(1, mapper.unlike(key, "u1001"));
        assertEquals(0, mapper.unlike(key, "u1001"));
        assertEquals(0L, mapper.contains(key, "u1001"));
        assertEquals(1L, mapper.count(key));
    }
}
