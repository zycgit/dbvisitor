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
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisXmlMapperSqlFragmentTest extends RedisNativeMapperSupport {
    private RedisExtendedMapper extended() throws Exception {
        return session.createMapper(RedisExtendedMapper.class);
    }

    private String list() throws Exception {
        String k = key("list");
        session.jdbc().executeUpdate("RPUSH ? a b", k);
        extended();
        return k;
    }

    @Before
    public void loadStatements() throws Exception {
        session.getConfiguration().loadMapper("/mapper/redis/CoverageMapper.xml");
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_FRAGMENT_MULTIPLE)
    public void fragments() throws Exception {
        assertEquals(Arrays.asList("a", "b"), extended().included(list()));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_FRAGMENT_COLUMNS)
    public void fragment_shouldSelectRequestedFields() throws Exception {
        String key = key("fields");
        session.jdbc().executeUpdate("HSET ? name mali age 28 email private", key);
        List<Map<String, Object>> rows = session.queryStatement("redis.Coverage.selectedFields", params(key, null));
        assertEquals(2, rows.size());
        for (Map<String, Object> row : rows) {
            assertEquals(Collections.singleton("VALUE"), row.keySet());
        }
        // HMGET returns values in the requested field order, without field names.
        assertEquals("mali", rows.get(0).get("VALUE"));
        assertEquals("28", rows.get(1).get("VALUE"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_FRAGMENT_DYNAMIC_CONDITION)
    public void fragment_shouldIncludeConditionalScoreBounds() throws Exception {
        String key = key("scores");
        session.jdbc().queryForLong("ZADD ? 10 a 20 b 30 c", key);
        Map<String, Object> args = params(key, null);
        assertEquals(Arrays.asList("a", "b", "c"), session.queryStatement("redis.Coverage.filteredScores", args));
        args.put("minimum", 20);
        args.put("maximum", 30);
        assertEquals(Arrays.asList("b", "c"), session.queryStatement("redis.Coverage.filteredScores", args));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_FRAGMENT_ORDER)
    public void fragment_shouldApplyResultOrdering() throws Exception {
        String key = key("scores");
        session.jdbc().queryForLong("ZADD ? 10 a 20 b 30 c", key);
        assertEquals(Arrays.asList("c", "b", "a"), session.queryStatement("redis.Coverage.orderedScores", params(key, null)));
    }
}
