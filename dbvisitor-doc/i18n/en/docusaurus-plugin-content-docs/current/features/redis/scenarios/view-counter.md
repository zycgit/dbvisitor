---
id: view-counter
sidebar_position: 4
title: View Counter
---

Execute INCR on each view to increment a Redis counter.

## Data Structure

Key `demo:views:article:a1001` stores an integer such as `2` in a String.

## Mapping

Use Long directly without an entity. GET returns null for a missing key; INCR starts from zero when the key is missing.

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

## Calling the Mapper

Run inside the [Session created above](about.md#create-a-session):

```java
ViewCounterMapper mapper = session.createMapper(ViewCounterMapper.class);
String key = "demo:views:article:a1001";

Long before = mapper.count(key);
long first = mapper.increment(key);
long second = mapper.increment(key);
Long total = mapper.count(key);
```

Starting with a missing key, `before = null`, `first = 1`, `second = 2` and `total = 2`.

## Notes

- INCR returns the new value, so the Mapper uses @Query rather than interpreting it as an update count.
- Do not emulate increment with GET followed by SET: concurrent views can lose increments. Store only signed 64-bit integers in the key.
