/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.scenario;

import net.hasor.dbvisitor.test.realdb.redis.scenario.mapper.ProductCacheMapper;
import net.hasor.dbvisitor.test.realdb.redis.scenario.model.ProductCache;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProductCacheScenarioTest extends RedisScenarioSupport {
    @Test
    public void cachesJsonWithExpirationAndEviction() throws Exception {
        String key = key("cache:product:p1001");
        ProductCacheMapper mapper = this.session.createMapper(ProductCacheMapper.class);
        ProductCache product = new ProductCache();
        product.setProductId("p1001");
        product.setName("USB-C Hub");
        product.setPriceCents(12900L);

        assertNull(mapper.load(key));
        assertEquals(1, mapper.save(key, product, 300));
        ProductCache loaded = mapper.load(key);
        assertNotSame(product, loaded);
        assertEquals("p1001", loaded.getProductId());
        assertEquals("USB-C Hub", loaded.getName());
        assertEquals(Long.valueOf(12900), loaded.getPriceCents());
        assertTtl(key, 300);

        product.setName("USB \"Hub\"\n新版");
        assertEquals(1, mapper.save(key, product, 300));
        assertEquals(product.getName(), mapper.load(key).getName());
        assertTtl(key, 300);
        assertEquals(1, mapper.evict(key));
        assertNull(mapper.load(key));
        assertEquals(0, mapper.evict(key));
    }
}
