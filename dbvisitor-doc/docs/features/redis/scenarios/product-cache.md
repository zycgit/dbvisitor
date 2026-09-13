---
id: product-cache
sidebar_position: 1
title: 商品缓存
---

将商品摘要缓存为 JSON；命中时直接还原为商品对象，未命中时再查业务数据库。

## 数据结构

键 `demo:cache:product:p1001`，String 值保存 JSON，存活时间 300 秒。

```json
{"productId":"p1001","name":"USB-C Hub","priceCents":12900}
```

## 数据映射

`@BindTypeHandler` 让 ProductCache 整体作为一个 JSON 值读写，不需要把每个属性映射为 Redis 字段。

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

## 调用示例

在[已创建的 Session](about.md#创建-session) 中执行：

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

`saved`、`removed` 均为 `1`；`cached` 的三个属性与写入值一致，`missing` 为 `null`。

## 注意事项

- 缓存未命中时由业务查询数据库，再调用 save；不要把缓存当作商品数据的唯一来源。
- 数据库中的商品发生修改后应使缓存失效。save 会覆盖整个 JSON，并重新设置有效期。
