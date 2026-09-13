---
id: article-likes
sidebar_position: 5
title: User Likes
---

Use one Set of user IDs per article so repeat likes from the same user do not increase the count.

## Data Structure

Key `demo:likes:article:a1001` contains Set members such as `u1001` and `u1002`.

## Mapping

Read members directly as `List<String>`, without an entity. Sets are unordered; SISMEMBER returns RESULT as 0 or 1.

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

## Calling the Mapper

Run inside the [Session created above](about.md#create-a-session):

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

Starting with an empty set, `first = 1`, `repeated = 0`, `count = 2`, `liked = 1` and `removed = 1`. users contains u1001 and u1002 in no guaranteed order.

## Notes

- A repeat like returns 0, not an error; removing an absent like also returns 0.
- Use count for the number of likes instead of fetching every member of a large set.
