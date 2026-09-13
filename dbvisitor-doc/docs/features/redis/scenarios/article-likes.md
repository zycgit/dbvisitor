---
id: article-likes
sidebar_position: 5
title: 用户点赞
---

每篇文章使用一个 Set 保存点赞用户编号，让同一用户重复点赞不增加数量。

## 数据结构

键 `demo:likes:article:a1001`，Set 成员示例：`u1001`、`u1002`。

## 数据映射

成员直接读取为 `List<String>`，不需要实体。Set 不保证顺序；SISMEMBER 返回的 RESULT 为 0 或 1。

## Mapper

```java title="ArticleLikeMapper.java"
package com.example.redis;

import java.util.List;
import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;

@SimpleMapper
public interface ArticleLikeMapper {
    @Insert("SADD #{key} #{userId}")
    int like(@Param("key") String key, @Param("userId") String userId);

    @Delete("SREM #{key} #{userId}")
    int unlike(@Param("key") String key, @Param("userId") String userId);

    @Query("SISMEMBER #{key} #{userId}")
    long contains(@Param("key") String key, @Param("userId") String userId);

    @Query("SCARD #{key}")
    long count(@Param("key") String key);

    @Query("SMEMBERS #{key}")
    List<String> users(@Param("key") String key);
}
```

## 调用示例

在[已创建的 Session](about.md#创建-session) 中执行：

```java
ArticleLikeMapper mapper = session.createMapper(ArticleLikeMapper.class);
String key = "demo:likes:article:a1001";

int first = mapper.like(key, "u1001");
int repeated = mapper.like(key, "u1001");
mapper.like(key, "u1002");
long count = mapper.count(key);
long liked = mapper.contains(key, "u1001");
List<String> users = mapper.users(key);
int removed = mapper.unlike(key, "u1001");
```

从空集合开始，`first = 1`、`repeated = 0`、`count = 2`、`liked = 1`、`removed = 1`。users 包含 u1001、u1002，不保证先后顺序。

## 注意事项

- 重复点赞返回 0，不是失败；取消不存在的点赞也返回 0。
- 用户很多时不要为展示数量读取全部成员，使用 count。
