---
id: shopping-cart
sidebar_position: 3
title: Shopping Cart
---

Use one Hash per user: each field name is a product ID and its value is the quantity.

## Data Structure

Key `demo:cart:u1001`, with Hash entries:

| Product ID (FIELD) | Quantity (VALUE) |
| --- | --- |
| p1001 | 3 |
| p1002 | 1 |

## Mapping

HGETALL returns FIELD and VALUE columns per entry. Each result row maps to one CartItem, with quantity converted from String to Integer. The whole Hash is not automatically mapped to a single cart bean.

```java title="CartItem.java"
package com.example.redis;

import net.hasor.dbvisitor.mapping.Column;

public class CartItem {
    @Column("FIELD")
    private String productId;
    @Column("VALUE")
    private Integer quantity;

    public String getProductId() {
        return this.productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
```

## Mapper

```java title="ShoppingCartMapper.java"
package com.example.redis;

import java.util.List;
import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;

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
```

## Calling the Mapper

Run inside the [Session created above](about.md#create-a-session):

```java
ShoppingCartMapper mapper = session.createMapper(ShoppingCartMapper.class);
String key = "demo:cart:u1001";

long first = mapper.add(key, "p1001", 2);
long second = mapper.add(key, "p1001", 1);
long other = mapper.add(key, "p1002", 1);
List<CartItem> items = mapper.items(key);
int removed = mapper.remove(key, "p1002");
```

Starting from an empty cart, `first = 2`, `second = 3` and `other = 1`. items contains p1001 with quantity 3 and p1002 with quantity 1, in no guaranteed order; `removed = 1`.

## Notes

- HINCRBY mutates data but returns the new quantity in a ResultSet, so add uses @Query.
- This example passes positive quantities to add. Removing zero-quantity fields or validating stock requires separate business rules.
