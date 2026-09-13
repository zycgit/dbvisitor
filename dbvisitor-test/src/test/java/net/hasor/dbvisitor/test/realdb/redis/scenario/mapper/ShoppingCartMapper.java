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
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.realdb.redis.scenario.model.CartItem;

@SimpleMapper
public interface ShoppingCartMapper {
    @Query("HINCRBY #{key} #{productId} #{quantity}")
    long add(@Param("key") String key, @Param("productId") String productId,
             @Param("quantity") int quantity);

    @Query("HGETALL #{key}")
    List<CartItem> items(@Param("key") String key);

    @Delete("HDEL #{key} #{productId}")
    int remove(@Param("key") String key, @Param("productId") String productId);
}
