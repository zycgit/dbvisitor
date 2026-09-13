/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.realdb.redis.dto1.RedisParameterUser;

@SimpleMapper
public interface RedisCoverageMapper {

    @Insert("SET #{key} #{value}")
    int putBean(@Param("key") String key, @Param("value") RedisParameterUser value);

    @Query("GET #{key}")
    RedisParameterUser bean(@Param("key") String key);

    @Insert("RPUSH #{key} #{value}")
    int appendBean(@Param("key") String key, @Param("value") RedisParameterUser value);

    @Query("LRANGE #{key} 0 -1")
    List<RedisParameterUser> beans(@Param("key") String key);

    @Query("LLEN #{key}")
    int count(@Param("key") String key);

    @Query("GET #{key}")
    Integer integer(@Param("key") String key);

    @Query("GET #{key}")
    java.sql.Date date(@Param("key") String key);

    @Query("SMEMBERS #{key}")
    List<Integer> distinct(@Param("key") String key);

    @Query("THIS_IS_NOT_A_REDIS_COMMAND")
    String invalidSyntax() throws SQLException;

    @Query("HGETALL #{key}")
    List<Map<String, Object>> hash(@Param("key") String key) throws SQLException;

    @Query("INCR #{key}")
    Long increment(@Param("key") String key) throws SQLException;
}
