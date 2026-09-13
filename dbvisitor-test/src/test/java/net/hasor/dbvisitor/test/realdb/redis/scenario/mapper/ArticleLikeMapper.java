/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.scenario.mapper;

import java.util.List;
import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;

@SimpleMapper
public interface ArticleLikeMapper {
    @Insert("SADD #{key} #{userId}")
    int like(@Param("key") String key, @Param("userId") String userId);

    @Delete("SREM #{key} #{userId}")
    int unlike(@Param("key") String key, @Param("userId") String userId);

    @Query("SISMEMBER #{key} #{userId}")
    long contains(@Param("key") String key, @Param("userId") String userId);

    @Query("SCARD #{key}")
    long count(@Param("key") String key);

    @Query("SMEMBERS #{key}")
    List<String> users(@Param("key") String key);
}
