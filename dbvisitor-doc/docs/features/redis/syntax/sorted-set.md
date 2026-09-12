---
id: sorted-set
sidebar_position: 6
title: Sorted Set 命令集
---


| 命令                                                                          | 返回值 | 行数       | 结果                                                                  |
|-----------------------------------------------------------------------------|-----|----------|---------------------------------------------------------------------|
| [ZMPOP](https://redis.io/docs/latest/commands/zmpop/)                       | 结果集 | multiple | KEY 字段，STRING 类型<br/>ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型    |
| [BZMPOP](https://redis.io/docs/latest/commands/bzmpop/)                     | 结果集 | multiple | KEY 字段，STRING 类型<br/>ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型    |
| [ZPOPMAX](https://redis.io/docs/latest/commands/zpopmax/)                   | 结果集 | multiple | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型                         |
| [BZPOPMAX](https://redis.io/docs/latest/commands/bzpopmax/)                 | 结果集 | 1        | KEY 字段，STRING 类型<br/>ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型    |
| [ZPOPMIN](https://redis.io/docs/latest/commands/zpopmin/)                   | 结果集 | multiple | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型                         |
| [BZPOPMIN](https://redis.io/docs/latest/commands/bzpopmin/)                 | 结果集 | 1        | KEY 字段，STRING 类型<br/>ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型    |
| [ZADD](https://redis.io/docs/latest/commands/zadd/)                         | 结果集 | 1        | RESULT 字段，DOUBLE 类型（当使用 INCR 时）<br/>RESULT 字段，LONG 类型（当不使用 INCR 时）  |
| [ZCARD](https://redis.io/docs/latest/commands/zcard/)                       | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZCOUNT](https://redis.io/docs/latest/commands/zcount/)                     | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZDIFF](https://redis.io/docs/latest/commands/zdiff/)                       | 结果集 | multiple | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）       |
| [ZDIFFSTORE](https://redis.io/docs/latest/commands/zdiffstore/)             | 值   | --       | 结果有序集合中的元素数量。                                                       |
| [ZINCRBY](https://redis.io/docs/latest/commands/zincrby/)                   | 结果集 | 1        | SCORE 字段，DOUBLE 类型                                                  |
| [ZINTER](https://redis.io/docs/latest/commands/zinter/)                     | 结果集 | multiple | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）       |
| [ZINTERCARD](https://redis.io/docs/latest/commands/zintercard/)             | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZINTERSTORE](https://redis.io/docs/latest/commands/zinterstore/)           | 值   | --       | 结果有序集合中的元素数量。                                                       |
| [ZLEXCOUNT](https://redis.io/docs/latest/commands/zlexcount/)               | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZSCORE](https://redis.io/docs/latest/commands/zscore/)                     | 结果集 | 1        | SCORE 字段，DOUBLE 类型                                                  |
| [ZMSCORE](https://redis.io/docs/latest/commands/zmscore/)                   | 结果集 | multiple | SCORE 字段，DOUBLE 类型                                                  |
| [ZRANDMEMBER](https://redis.io/docs/latest/commands/zrandmember/)           | 结果集 | multiple | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）       |
| [ZRANGE](https://redis.io/docs/latest/commands/zrange/)                     | 结果集 | multiple | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）       |
| [ZRANGEBYLEX](https://redis.io/docs/latest/commands/zrangebylex/)           | 结果集 | multiple | ELEMENT 字段，STRING 类型                                                |
| [ZRANGEBYSCORE](https://redis.io/docs/latest/commands/zrangebyscore/)       | 结果集 | multiple | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）       |
| [ZRANGESTORE](https://redis.io/docs/latest/commands/zrangestore/)           | 值   | --       | 结果有序集合中的元素数量。                                                       |
| [ZRANK](https://redis.io/docs/latest/commands/zrank/)                       | 结果集 | 1        | SCORE 字段，DOUBLE 类型（当使用 WITHSCORE 时）<br/> RANK 字段，LONG 类型           |
| [ZREVRANK](https://redis.io/docs/latest/commands/zrevrank/)                 | 结果集 | 1        | SCORE 字段，DOUBLE 类型（当使用 WITHSCORE 时）<br/> RANK 字段，LONG 类型           |
| [ZREM](https://redis.io/docs/latest/commands/zrem/)                         | 值   | --       | 从有序集合中移除的成员数量，不包括不存在的成员。                                            |
| [ZREMRANGEBYLEX](https://redis.io/docs/latest/commands/zremrangebylex/)     | 值   | --       | 从有序集合中移除的成员数量，不包括不存在的成员。                                            |
| [ZREMRANGEBYRANK](https://redis.io/docs/latest/commands/zremrangebyrank/)   | 值   | --       | 从有序集合中移除的成员数量，不包括不存在的成员。                                            |
| [ZREMRANGEBYSCORE](https://redis.io/docs/latest/commands/zremrangebyscore/) | 值   | --       | 从有序集合中移除的成员数量，不包括不存在的成员。                                            |
| [ZREVRANGE](https://redis.io/docs/latest/commands/zrevrange/)               | 结果集 | multiple | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）       |
| [ZREVRANGEBYLEX](https://redis.io/docs/latest/commands/zrevrangebylex/)     | 结果集 | multiple | ELEMENT 字段，STRING 类型                                                |
| [ZREVRANGEBYSCORE](https://redis.io/docs/latest/commands/zrevrangebyscore/) | 结果集 | multiple | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）       |
| [ZSCAN](https://redis.io/docs/latest/commands/zscan/)                       | 结果集 | multiple | CURSOR 字段，STRING 类型<br/>ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型 |
| [ZUNION](https://redis.io/docs/latest/commands/zunion/)                     | 结果集 | multiple | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）       |
| [ZUNIONSTORE](https://redis.io/docs/latest/commands/zunionstore/)           | 值   | --       | 结果有序集合中的元素数量。                                                       |
