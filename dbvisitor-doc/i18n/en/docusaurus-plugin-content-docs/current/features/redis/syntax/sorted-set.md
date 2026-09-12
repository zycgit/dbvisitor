---
id: sorted-set
sidebar_position: 6
title: SortedSet Command Set
---


| Command | Return | Rows | Result |
|---|---|---|---|
| [ZMPOP](https://redis.io/docs/latest/commands/zmpop/) | ResultSet | multiple | KEY field, STRING type<br/>ELEMENT field, STRING type<br/>SCORE field, DOUBLE type |
| [BZMPOP](https://redis.io/docs/latest/commands/bzmpop/) | ResultSet | multiple | KEY field, STRING type<br/>ELEMENT field, STRING type<br/>SCORE field, DOUBLE type |
| [ZPOPMAX](https://redis.io/docs/latest/commands/zpopmax/) | ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type |
| [BZPOPMAX](https://redis.io/docs/latest/commands/bzpopmax/) | ResultSet | 1 | KEY field, STRING type<br/>ELEMENT field, STRING type<br/>SCORE field, DOUBLE type |
| [ZPOPMIN](https://redis.io/docs/latest/commands/zpopmin/) | ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type |
| [BZPOPMIN](https://redis.io/docs/latest/commands/bzpopmin/) | ResultSet | 1 | KEY field, STRING type<br/>ELEMENT field, STRING type<br/>SCORE field, DOUBLE type |
| [ZADD](https://redis.io/docs/latest/commands/zadd/) | ResultSet | 1 | RESULT field, DOUBLE type (when using INCR)<br/>RESULT field, LONG type (when not using INCR) |
| [ZCARD](https://redis.io/docs/latest/commands/zcard/) | ResultSet | 1 | RESULT field, LONG type |
| [ZCOUNT](https://redis.io/docs/latest/commands/zcount/) | ResultSet | 1 | RESULT field, LONG type |
| [ZDIFF](https://redis.io/docs/latest/commands/zdiff/) | ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |
| [ZDIFFSTORE](https://redis.io/docs/latest/commands/zdiffstore/) | Value | -- | Number of elements in the resulting sorted set. |
| [ZINCRBY](https://redis.io/docs/latest/commands/zincrby/) | ResultSet | 1 | SCORE field, DOUBLE type |
| [ZINTER](https://redis.io/docs/latest/commands/zinter/) | ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |
| [ZINTERCARD](https://redis.io/docs/latest/commands/zintercard/) | ResultSet | 1 | RESULT field, LONG type |
| [ZINTERSTORE](https://redis.io/docs/latest/commands/zinterstore/) | Value | -- | Number of elements in the resulting sorted set. |
| [ZLEXCOUNT](https://redis.io/docs/latest/commands/zlexcount/) | ResultSet | 1 | RESULT field, LONG type |
| [ZSCORE](https://redis.io/docs/latest/commands/zscore/) | ResultSet | 1 | SCORE field, DOUBLE type |
| [ZMSCORE](https://redis.io/docs/latest/commands/zmscore/) | ResultSet | multiple | SCORE field, DOUBLE type |
| [ZRANDMEMBER](https://redis.io/docs/latest/commands/zrandmember/) | ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |
| [ZRANGE](https://redis.io/docs/latest/commands/zrange/) | ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |
| [ZRANGEBYLEX](https://redis.io/docs/latest/commands/zrangebylex/) | ResultSet | multiple | ELEMENT field, STRING type |
| [ZRANGEBYSCORE](https://redis.io/docs/latest/commands/zrangebyscore/) | ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |
| [ZRANGESTORE](https://redis.io/docs/latest/commands/zrangestore/) | Value | -- | Number of elements in the resulting sorted set. |
| [ZRANK](https://redis.io/docs/latest/commands/zrank/) | ResultSet | 1 | SCORE field, DOUBLE type (when using WITHSCORE)<br/>RANK field, LONG type |
| [ZREVRANK](https://redis.io/docs/latest/commands/zrevrank/) | ResultSet | 1 | SCORE field, DOUBLE type (when using WITHSCORE)<br/>RANK field, LONG type |
| [ZREM](https://redis.io/docs/latest/commands/zrem/) | Value | -- | Number of members removed from the sorted set, not including non-existing members. |
| [ZREMRANGEBYLEX](https://redis.io/docs/latest/commands/zremrangebylex/) | Value | -- | Number of members removed from the sorted set, not including non-existing members. |
| [ZREMRANGEBYRANK](https://redis.io/docs/latest/commands/zremrangebyrank/) | Value | -- | Number of members removed from the sorted set, not including non-existing members. |
| [ZREMRANGEBYSCORE](https://redis.io/docs/latest/commands/zremrangebyscore/) | Value | -- | Number of members removed from the sorted set, not including non-existing members. |
| [ZREVRANGE](https://redis.io/docs/latest/commands/zrevrange/) | ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |
| [ZREVRANGEBYLEX](https://redis.io/docs/latest/commands/zrevrangebylex/) | ResultSet | multiple | ELEMENT field, STRING type |
| [ZREVRANGEBYSCORE](https://redis.io/docs/latest/commands/zrevrangebyscore/) | ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |
| [ZSCAN](https://redis.io/docs/latest/commands/zscan/) | ResultSet | multiple | CURSOR field, STRING type<br/>ELEMENT field, STRING type<br/>SCORE field, DOUBLE type |
| [ZUNION](https://redis.io/docs/latest/commands/zunion/) | ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |
| [ZUNIONSTORE](https://redis.io/docs/latest/commands/zunionstore/) | Value | -- | Number of elements in the resulting sorted set. |
