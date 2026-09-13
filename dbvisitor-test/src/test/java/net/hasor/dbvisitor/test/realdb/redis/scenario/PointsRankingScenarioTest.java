/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.scenario;

import java.util.List;
import net.hasor.dbvisitor.test.realdb.redis.scenario.mapper.PointsRankingMapper;
import net.hasor.dbvisitor.test.realdb.redis.scenario.model.RankEntry;
import org.junit.Test;
import static org.junit.Assert.*;

public class PointsRankingScenarioTest extends RedisScenarioSupport {
    @Test
    public void mapsOrderedMembersAndFractionalScores() throws Exception {
        String key = key("ranking:points");
        PointsRankingMapper mapper = this.session.createMapper(PointsRankingMapper.class);
        assertTrue(mapper.top(key, 1).isEmpty());
        assertEquals(10.5, mapper.addPoints(key, "u1001", 10.5), 0.000001);
        assertEquals(10.75, mapper.addPoints(key, "u1001", 0.25), 0.000001);
        assertEquals(20.25, mapper.addPoints(key, "u1002", 20.25), 0.000001);
        assertEquals(5.0, mapper.addPoints(key, "u1003", 5.0), 0.000001);
        List<RankEntry> top = mapper.top(key, 1);
        assertEquals(2, top.size());
        assertEquals("u1002", top.get(0).getUserId());
        assertEquals(20.25, top.get(0).getPoints(), 0.000001);
        assertEquals("u1001", top.get(1).getUserId());
        assertEquals(10.75, top.get(1).getPoints(), 0.000001);
        assertEquals(Long.valueOf(0), mapper.rank(key, "u1002"));
        assertEquals(Long.valueOf(1), mapper.rank(key, "u1001"));
        assertNull(mapper.rank(key, "missing"));
    }
}
