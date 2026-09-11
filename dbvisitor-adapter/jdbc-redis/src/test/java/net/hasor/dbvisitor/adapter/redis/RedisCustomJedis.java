/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.redis;

import java.util.Map;
import org.powermock.api.mockito.PowerMockito;
import redis.clients.jedis.Jedis;

public class RedisCustomJedis implements CustomJedis {
    @Override
    public Object createJedisCmd(String jdbcUrl, Map<String, String> props) {
        return PowerMockito.mock(Jedis.class);
    }
}
