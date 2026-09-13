/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.util.*;
import net.hasor.dbvisitor.mapper.*;

@RefMapper("/mapper/redis/ExtendedMapper.xml")
public interface RedisExtendedMapper {

    int put(@Param("key") String key, @Param("value") String value);

    int replace(@Param("key") String key, @Param("value") String value);

    int remove(@Param("key") String key);

    String get(@Param("key") String key);

    String bean(RedisNativeMapperSupport.Generated bean);

    String map(Map<String, Object> args);

    List<Map<String, Object>> many(@Param("keyList") List<String> keys);

    List<String> choose(@Param("key") String key, @Param("single") boolean single);

    List<String> direction(@Param("key") String key, @Param("direction") String direction);

    List<RedisNativeMapperSupport.Entry> partial(@Param("key") String key);

    List<RedisNativeMapperSupport.Entry> automatic(@Param("key") String key);

    List<Map<String, Object>> renamed(@Param("key") String key);

    List<String> included(@Param("key") String key);
}
