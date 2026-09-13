---
id: points-ranking
sidebar_position: 6
title: Points Ranking
---

Store user IDs and points in a Sorted Set and read the ranking from highest to lowest score.

## Data Structure

Key `demo:ranking:points` uses user IDs as members and points as scores.

| User (ELEMENT) | Points (SCORE) |
| --- | --- |
| u1002 | 20.25 |
| u1001 | 10.75 |

## Mapping

ZRANGE ... WITHSCORES returns ELEMENT and SCORE columns, with one row mapped to one RankEntry.

```java title="RankEntry.java"
package com.example.redis;

import net.hasor.dbvisitor.mapping.Column;

public class RankEntry {
    @Column("ELEMENT")
    private String userId;
    @Column("SCORE")
    private Double points;

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getPoints() {
        return this.points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }
}
```

## Mapper

```java title="PointsRankingMapper.java"
package com.example.redis;

import java.util.List;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;

@SimpleMapper
public interface PointsRankingMapper {
    @Query("ZADD #{key} INCR #{points} #{userId}")
    double addPoints(@Param("key") String key, @Param("userId") String userId,
                     @Param("points") double points);

    @Query("ZRANGE #{key} 0 #{lastIndex} REV WITHSCORES")
    List<RankEntry> top(@Param("key") String key, @Param("lastIndex") int lastIndex);

    @Query("ZREVRANK #{key} #{userId}")
    Long rank(@Param("key") String key, @Param("userId") String userId);
}
```

## Calling the Mapper

Run inside the [Session created above](about.md#create-a-session):

```java
PointsRankingMapper mapper = session.createMapper(PointsRankingMapper.class);
String key = "demo:ranking:points";

mapper.addPoints(key, "u1001", 10.5);
double updated = mapper.addPoints(key, "u1001", 0.25);
mapper.addPoints(key, "u1002", 20.25);
mapper.addPoints(key, "u1003", 5.0);
List<RankEntry> top = mapper.top(key, 1);
Long rank = mapper.rank(key, "u1002");
```

Starting with an empty ranking, `updated = 10.75`. top contains u1002 / 20.25 followed by u1001 / 10.75. `rank = 0`; the displayed place is rank + 1.

## Notes

- top takes the inclusive last index: pass 9 for the top 10. rank returns null for a missing member.
- ZADD INCR preserves fractional increments; the current ZINCRBY path reads the increment as an integer. Scores use Double and are unsuitable for amounts requiring exact decimal arithmetic.
