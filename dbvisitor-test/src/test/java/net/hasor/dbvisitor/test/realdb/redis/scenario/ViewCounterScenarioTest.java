/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.scenario;

import net.hasor.dbvisitor.test.realdb.redis.scenario.mapper.ViewCounterMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class ViewCounterScenarioTest extends RedisScenarioSupport {
    @Test
    public void incrementsAndReadsNumericResult() throws Exception {
        String key = key("views:article:a1001");
        ViewCounterMapper mapper = this.session.createMapper(ViewCounterMapper.class);
        assertNull(mapper.count(key));
        assertEquals(1L, mapper.increment(key));
        assertEquals(2L, mapper.increment(key));
        assertEquals(Long.valueOf(2), mapper.count(key));
    }
}
