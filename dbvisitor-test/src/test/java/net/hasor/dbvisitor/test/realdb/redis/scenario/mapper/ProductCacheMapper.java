/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.scenario.mapper;

import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.realdb.redis.scenario.model.ProductCache;

@SimpleMapper
public interface ProductCacheMapper {
    @Insert("SET #{key} #{product} EX #{ttlSeconds}")
    int save(@Param("key") String key, @Param("product") ProductCache product,
             @Param("ttlSeconds") int ttlSeconds);

    @Query("GET #{key}")
    ProductCache load(@Param("key") String key);

    @Delete("DEL #{key}")
    int evict(@Param("key") String key);
}
