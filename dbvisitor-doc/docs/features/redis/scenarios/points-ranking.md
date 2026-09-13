---
id: points-ranking
sidebar_position: 6
title: 积分排行榜
---

用 Sorted Set 保存用户编号与积分，按积分从高到低读取榜单。

## 数据结构

键 `demo:ranking:points`，成员是用户编号，score 是积分。

| 用户（ELEMENT） | 积分（SCORE） |
| --- | --- |
| u1002 | 20.25 |
| u1001 | 10.75 |

## 数据映射

ZRANGE ... WITHSCORES 返回 ELEMENT、SCORE 两列，一行映射为一个 RankEntry。

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

## 调用示例

在[已创建的 Session](about.md#创建-session) 中执行：

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

从空榜单开始，`updated = 10.75`；top 的两行依次为 u1002 / 20.25、u1001 / 10.75。`rank = 0`，展示给用户的名次为 rank + 1。

## 注意事项

- top 的参数是最后一个下标，包含该位置：取前 10 名传 9。rank 在成员不存在时返回 null。
- 使用 ZADD INCR 保留小数增量；当前 ZINCRBY 的增量按整数读取。积分使用 Double，不适合要求十进制精确计算的金额。
