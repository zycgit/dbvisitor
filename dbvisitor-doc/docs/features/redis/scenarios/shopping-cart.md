---
id: shopping-cart
sidebar_position: 3
title: 购物车
---

每个用户使用一个 Hash：字段名是商品编号，字段值是数量。

## 数据结构

键 `demo:cart:u1001`，Hash 示例：

| 商品编号（FIELD） | 数量（VALUE） |
| --- | --- |
| p1001 | 3 |
| p1002 | 1 |

## 数据映射

HGETALL 每行返回 FIELD、VALUE 两列。一个结果行映射为一个 CartItem；数量从字符串转换为 Integer。整个 Hash 不会自动变成一个购物车 Bean。

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

## 调用示例

在[已创建的 Session](about.md#创建-session) 中执行：

```java
ShoppingCartMapper mapper = session.createMapper(ShoppingCartMapper.class);
String key = "demo:cart:u1001";

long first = mapper.add(key, "p1001", 2);
long second = mapper.add(key, "p1001", 1);
long other = mapper.add(key, "p1002", 1);
List<CartItem> items = mapper.items(key);
int removed = mapper.remove(key, "p1002");
```

从空购物车开始，`first = 2`、`second = 3`、`other = 1`。items 包含 p1001 数量 3 和 p1002 数量 1，顺序不保证；`removed = 1`。

## 注意事项

- HINCRBY 修改数据，但返回新数量结果集，因此 add 使用 @Query。
- 此例的 add 传正数。减数量至零后自动删字段、库存校验等业务规则不由该命令完成。
