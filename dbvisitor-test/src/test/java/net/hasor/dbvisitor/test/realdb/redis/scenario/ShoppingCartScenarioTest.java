/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.scenario;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.hasor.dbvisitor.test.realdb.redis.scenario.mapper.ShoppingCartMapper;
import net.hasor.dbvisitor.test.realdb.redis.scenario.model.CartItem;
import org.junit.Test;
import static org.junit.Assert.*;

public class ShoppingCartScenarioTest extends RedisScenarioSupport {
    @Test
    public void mapsEachHashEntryToOneCartItem() throws Exception {
        String key = key("cart:u1001");
        ShoppingCartMapper mapper = this.session.createMapper(ShoppingCartMapper.class);
        assertTrue(mapper.items(key).isEmpty());
        assertEquals(2L, mapper.add(key, "p1001", 2));
        assertEquals(3L, mapper.add(key, "p1001", 1));
        assertEquals(1L, mapper.add(key, "p1002", 1));
        List<CartItem> items = mapper.items(key);
        Map<String, Integer> quantities = items.stream()
                .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));
        assertEquals(2, items.size());
        assertEquals(Integer.valueOf(3), quantities.get("p1001"));
        assertEquals(Integer.valueOf(1), quantities.get("p1002"));
        assertEquals(1, mapper.remove(key, "p1002"));
        assertEquals(0, mapper.remove(key, "p1002"));
        assertEquals(1, mapper.items(key).size());
    }
}
