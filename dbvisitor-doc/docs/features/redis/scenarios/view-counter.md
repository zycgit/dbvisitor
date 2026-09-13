---
id: view-counter
sidebar_position: 4
title: 浏览计数
---

每次访问执行 INCR，由 Redis 对一个计数值加一。

## 数据结构

键 `demo:views:article:a1001`，String 值保存整数，例如 `2`。

## 数据映射

只需要 Long，不定义实体类。GET 在键不存在时返回 null；INCR 在键不存在时从 0 开始加一。

## Mapper

```java title="ViewCounterMapper.java"
package com.example.redis;

import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;

@SimpleMapper
public interface ViewCounterMapper {
    @Query("INCR #{key}")
    long increment(@Param("key") String key);

    @Query("GET #{key}")
    Long count(@Param("key") String key);
}
```

## 调用示例

在[已创建的 Session](about.md#创建-session) 中执行：

```java
ViewCounterMapper mapper = session.createMapper(ViewCounterMapper.class);
String key = "demo:views:article:a1001";

Long before = mapper.count(key);
long first = mapper.increment(key);
long second = mapper.increment(key);
Long total = mapper.count(key);
```

从不存在的键开始，`before = null`、`first = 1`、`second = 2`、`total = 2`。

## 注意事项

- INCR 返回新数值，Mapper 必须使用 @Query，而不是把它当成更新行数。
- 不要用 GET 后再 SET 模拟加一，否则并发访问可能丢失计数。键中只能保存 64 位整数。
