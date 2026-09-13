---
id: product-cache
sidebar_position: 1
title: Product Cache
---

Cache a product summary as JSON. A cache hit returns a product object; a miss falls back to the application database.

## Data Structure

Key `demo:cache:product:p1001` stores JSON in a String with a 300-second lifetime.

```json
{"productId":"p1001","name":"USB-C Hub","priceCents":12900}
```

## Mapping

`@BindTypeHandler` reads and writes ProductCache as one JSON value; its properties are not separate Redis fields.

```java title="ProductCache.java"
package com.example.redis;

import net.hasor.dbvisitor.types.BindTypeHandler;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@BindTypeHandler(JsonTypeHandler.class)
public class ProductCache {
    private String productId;
    private String name;
    private Long priceCents;

    public String getProductId() {
        return this.productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPriceCents() {
        return this.priceCents;
    }

    public void setPriceCents(Long priceCents) {
        this.priceCents = priceCents;
    }
}
```

## Mapper

```java title="ProductCacheMapper.java"
package com.example.redis;

import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;

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
```

## Calling the Mapper

Run inside the [Session created above](about.md#create-a-session):

```java
ProductCacheMapper mapper = session.createMapper(ProductCacheMapper.class);
String key = "demo:cache:product:p1001";

ProductCache product = new ProductCache();
product.setProductId("p1001");
product.setName("USB-C Hub");
product.setPriceCents(12900L);

int saved = mapper.save(key, product, 300);
ProductCache cached = mapper.load(key);
int removed = mapper.evict(key);
ProductCache missing = mapper.load(key);
```

`saved` and `removed` are `1`. All three properties of `cached` match the written object; `missing` is `null`.

## Notes

- On a miss, query the application database before calling save; the cache is not the sole source of product data.
- Invalidate the cache when the database product changes. save replaces the entire JSON value and resets its lifetime.
