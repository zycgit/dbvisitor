/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.redis;
import java.util.Map;

public interface CustomJedis {
    /** return JedisCluster or Jedis */
    Object createJedisCmd(String jdbcUrl, Map<String, String> props);
}
