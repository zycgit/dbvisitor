/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.scenario.mapper;

import java.util.List;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.realdb.redis.scenario.model.RankEntry;

@SimpleMapper
public interface PointsRankingMapper {
    @Query("ZADD #{key} INCR #{points} #{userId}")
    double addPoints(@Param("key") String key, @Param("userId") String userId,
                     @Param("points") double points);

    @Query("ZRANGE #{key} 0 #{lastIndex} REV WITHSCORES")
    List<RankEntry> top(@Param("key") String key, @Param("lastIndex") int lastIndex);

    @Query("ZREVRANK #{key} #{userId}")
    Long rank(@Param("key") String key, @Param("userId") String userId);
}
