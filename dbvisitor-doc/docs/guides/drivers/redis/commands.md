---
id: commands
sidebar_position: 3
hide_table_of_contents: true
title: 支持的命令
description: jdbc-redis 支持 140+ 常用命令，涵盖 DB、Server、Keys、List、Set、StoreSet、String、Hash 命令集。
---

- 值：使用 executeUpdate / getUpdateCount 获取影响行数。
- 结果集：使用 executeQuery / getResultSet 获取结果集。

## Hash 命令集 {#hash}

| 命令                                                                  | 返回值 | 行数       | 结果                                                                                |
|---------------------------------------------------------------------|-----|----------|-----------------------------------------------------------------------------------|
| [HDEL](https://redis.io/docs/latest/commands/hdel/)                 | 结果集 | 1        | RESULT 字段，LONG 类型                                                                 |
| [HEXISTS](https://redis.io/docs/latest/commands/hexists/)           | 结果集 | 1        | RESULT 字段，BOOLEAN 类型                                                              |
| [HEXPIRE](https://redis.io/docs/latest/commands/hexpire/)           | 结果集 | multiple | RESULT 字段，LONG 类型                                                                 |
| [HEXPIREAT](https://redis.io/docs/latest/commands/hexpireat/)       | 结果集 | multiple | RESULT 字段，LONG 类型                                                                 |
| [HEXPIRETIME](https://redis.io/docs/latest/commands/hexpiretime/)   | 结果集 | multiple | RESULT 字段，LONG 类型                                                                 |
| [HPEXPIRE](https://redis.io/docs/latest/commands/hpexpire/)         | 结果集 | multiple | RESULT 字段，LONG 类型                                                                 |
| [HPEXPIREAT](https://redis.io/docs/latest/commands/hpexpireat/)     | 结果集 | multiple | RESULT 字段，LONG 类型                                                                 |
| [HPEXPIRETIME](https://redis.io/docs/latest/commands/hpexpiretime/) | 结果集 | multiple | RESULT 字段，LONG 类型                                                                 |
| [HGET](https://redis.io/docs/latest/commands/hget/)                 | 结果集 | 1        | VALUE 字段，STRING 类型                                                                |
| [HGETALL](https://redis.io/docs/latest/commands/hgetall/)           | 结果集 | multiple | FIELD 字段，STRING 类型<br/>VALUE 字段，STRING 类型                                         |
| [HINCRBY](https://redis.io/docs/latest/commands/hincrby/)           | 结果集 | 1        | VALUE 字段，LONG 类型                                                                  |
| [HKEYS](https://redis.io/docs/latest/commands/hkeys/)               | 结果集 | multiple | KEY 字段，STRING 类型                                                                  |
| [HLEN](https://redis.io/docs/latest/commands/hlen/)                 | 结果集 | 1        | RESULT 字段，LONG 类型                                                                 |
| [HMGET](https://redis.io/docs/latest/commands/hmget/)               | 结果集 | multiple | VALUE 字段，STRING 类型                                                                |
| [HSET](https://redis.io/docs/latest/commands/hset/)                 | 结果集 | 1        | RESULT 字段，LONG 类型                                                                 |
| [HMSET](https://redis.io/docs/latest/commands/hmset/)               | 结果集 | 1        | RESULT 字段，STRING 类型                                                               |
| [HSETNX](https://redis.io/docs/latest/commands/hsetnx/)             | 结果集 | 1        | RESULT 字段，LONG 类型                                                                 |
| [HPERSIST](https://redis.io/docs/latest/commands/hpersist/)         | 结果集 | multiple | RESULT 字段，LONG 类型                                                                 |
| [HTTL](https://redis.io/docs/latest/commands/httl/)                 | 结果集 | multiple | RESULT 字段，LONG 类型                                                                 |
| [HPTTL](https://redis.io/docs/latest/commands/hpttl/)               | 结果集 | multiple | RESULT 字段，LONG 类型                                                                 |
| [HRANDFIELD](https://redis.io/docs/latest/commands/hrandfield/)     | 结果集 | multiple | FIELD 字段，STRING 类型<br/>VALUE 字段，STRING 类型（当使用 WITHVALUES 时）                       |
| [HSCAN](https://redis.io/docs/latest/commands/hscan/)               | 结果集 | multiple | CURSOR 字段，STRING 类型<br/>FIELD 字段，STRING 类型<br/>VALUE 字段，STRING 类型（不使用 NOVALUES 时） |
| [HSTRLEN](https://redis.io/docs/latest/commands/hstrlen/)           | 结果集 | 1        | RESULT 字段，LONG 类型                                                                 |
| [HVALS](https://redis.io/docs/latest/commands/hvals/)               | 结果集 | multiple | VALUE 字段，STRING 类型                                                                |

## Keys 命令集 {#keys}

| 命令                                                                        | 返回值 | 行数       | 结果                                              |
|---------------------------------------------------------------------------|-----|----------|-------------------------------------------------|
| [COPY](https://redis.io/docs/latest/commands/COPY/)                       | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [DEL](https://redis.io/docs/latest/commands/del/)                         | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [UNLINK](https://redis.io/docs/latest/commands/unlink/)                   | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [DUMP](https://redis.io/docs/latest/commands/dump/)                       | 结果集 | 1        | VALUE 字段，BYTES 类型                               |
| [EXISTS](https://redis.io/docs/latest/commands/exists/)                   | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [EXPIRE](https://redis.io/docs/latest/commands/expire/)                   | 值   | --       | 如果设置了超时时间，则为 1；否则为 0。                           |
| [EXPIREAT](https://redis.io/docs/latest/commands/expireat/)               | 值   | --       | 如果设置了超时时间，则为 1；否则为 0。                           |
| [EXPIRETIME](https://redis.io/docs/latest/commands/expiretime/)           | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [PEXPIRE](https://redis.io/docs/latest/commands/pexpire/)                 | 值   | --       | 如果设置了超时时间，则为 1；否则为 0。                           |
| [PEXPIREAT](https://redis.io/docs/latest/commands/pexpireat/)             | 值   | --       | 如果设置了超时时间，则为 1；否则为 0。                           |
| [PEXPIRETIME](https://redis.io/docs/latest/commands/pexpiretime/)         | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [KEYS](https://redis.io/docs/latest/commands/keys/)                       | 结果集 | multiple | KEY 字段，STRING 类型（提示：驱动会使用 scan 命令替代 keys 命令的调用） |
| [OBJECT ENCODING](https://redis.io/docs/latest/commands/object-encoding/) | 结果集 | 1        | RESULT 字段，STRING 类型                             |
| [OBJECT FREQ](https://redis.io/docs/latest/commands/object-freq/)         | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [OBJECT IDLETIME](https://redis.io/docs/latest/commands/object-idletime/) | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [OBJECT REFCOUNT](https://redis.io/docs/latest/commands/object-refcount/) | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [PERSIST](https://redis.io/docs/latest/commands/persist/)                 | 值   | --       | 如果该键当前已生效则为 1，否则为 0（仅在该键未设置的情况下出现）              |
| [TTL](https://redis.io/docs/latest/commands/ttl/)                         | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [PTTL](https://redis.io/docs/latest/commands/pttl/)                       | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [RANDOMKEY](https://redis.io/docs/latest/commands/randomkey/)             | 结果集 | 1        | KEY 字段，STRING 类型                                |
| [RENAME](https://redis.io/docs/latest/commands/rename/)                   | 值   | --       | 1 表示键已重命名，0 表示目标键已存在.（使用 OK 状态码判断）              |
| [RENAMENX](https://redis.io/docs/latest/commands/renamenx/)               | 值   | --       | 1 表示键已重命名，0 表示目标键已存在.                           |
| [SCAN](https://redis.io/docs/latest/commands/scan/)                       | 结果集 | multiple | CURSOR 字段，STRING 类型<br/>KEY 字段，STRING 类型        |
| [TOUCH](https://redis.io/docs/latest/commands/touch/)                     | 值   | --       | 被 TOUCH 的键的数量。                                  |
| [TYPE](https://redis.io/docs/latest/commands/type/)                       | 结果集 | 1        | RESULT 字段，STRING 类型                             |

## List 命令集 {#list}

| 命令                                                              | 返回值 | 行数       | 结果                                                                  |
|-----------------------------------------------------------------|-----|----------|---------------------------------------------------------------------|
| [LMOVE](https://redis.io/docs/latest/commands/lmove/)           | 结果集 | 1        | ELEMENT 字段，STRING 类型                                                |
| [BLMOVE](https://redis.io/docs/latest/commands/blmove/)         | 结果集 | 1        | ELEMENT 字段，STRING 类型                                                |
| [LMPOP](https://redis.io/docs/latest/commands/lmpop/)           | 结果集 | multiple | KEY 字段，STRING 类型，返回 Key,ValueList 结构中的 Key<br/>ELEMENT 字段，STRING 类型 |
| [BLMPOP](https://redis.io/docs/latest/commands/blmpop/)         | 结果集 | multiple | KEY 字段，STRING 类型，返回 Key,ValueList 结构中的 Key<br/>ELEMENT 字段，STRING 类型 |
| [LPOP](https://redis.io/docs/latest/commands/lpop/)             | 结果集 | multiple | ELEMENT 字段，STRING 类型                                                |
| [RPOP](https://redis.io/docs/latest/commands/rpop/)             | 结果集 | multiple | ELEMENT 字段，STRING 类型                                                |
| [BLPOP](https://redis.io/docs/latest/commands/blpop/)           | 结果集 | multiple | ELEMENT 字段，STRING 类型                                                |
| [BRPOP](https://redis.io/docs/latest/commands/brpop/)           | 结果集 | multiple | ELEMENT 字段，STRING 类型                                                |
| [RPOPLPUSH](https://redis.io/docs/latest/commands/rpoplpush/)   | 结果集 | 1        | ELEMENT 字段，STRING 类型                                                |
| [BRPOPLPUSH](https://redis.io/docs/latest/commands/brpoplpush/) | 结果集 | 1        | ELEMENT 字段，STRING 类型                                                |
| [LINDEX](https://redis.io/docs/latest/commands/lindex/)         | 结果集 | 1        | ELEMENT 字段，STRING 类型                                                |
| [LINSERT](https://redis.io/docs/latest/commands/linsert/)       | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [LLEN](https://redis.io/docs/latest/commands/llen/)             | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [LPOS](https://redis.io/docs/latest/commands/lpos/)             | 结果集 | multiple | RESULT 字段，LONG 类型                                                   |
| [LPUSH](https://redis.io/docs/latest/commands/lpush/)           | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [LPUSHX](https://redis.io/docs/latest/commands/lpushx/)         | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [RPUSH](https://redis.io/docs/latest/commands/rpush/)           | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [RPUSHX](https://redis.io/docs/latest/commands/rpushx/)         | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [LRANGE](https://redis.io/docs/latest/commands/lrange/)         | 结果集 | multiple | ELEMENT 字段，STRING 类型                                                |
| [LREM](https://redis.io/docs/latest/commands/lrem/)             | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [LSET](https://redis.io/docs/latest/commands/lset/)             | 结果集 | 1        | RESULT 字段，STRING 类型                                                 |
| [LTRIM](https://redis.io/docs/latest/commands/ltrim/)           | 结果集 | 1        | RESULT 字段，STRING 类型                                                 |

## Server 命令集 {#server}

| 命令                                                        | 返回值 | 行数  | 结果                                       |
|-----------------------------------------------------------|-----|-----|------------------------------------------|
| [MOVE](https://redis.io/docs/latest/commands/move/)       | 结果集 | 1   | RESULT 字段，LONG 类型                        |
| [WAIT](https://redis.io/docs/latest/commands/wait/)       | 结果集 | 1   | REPLICAS 字段，LONG 类型                      |
| [WAITAOF](https://redis.io/docs/latest/commands/waitaof/) | 结果集 | 1   | LOCAL 字段，LONG 类型<br/>REPLICAS 字段，LONG 类型 |

## Set 命令集 {#set}

| 命令                                                                | 返回值 | 行数       | 结果                                           |
|-------------------------------------------------------------------|-----|----------|----------------------------------------------|
| [SADD](https://redis.io/docs/latest/commands/sadd/)               | 结果集 | 1        | RESULT 字段，LONG 类型                            |
| [SCARD](https://redis.io/docs/latest/commands/scard/)             | 结果集 | 1        | RESULT 字段，LONG 类型                            |
| [SDIFF](https://redis.io/docs/latest/commands/sdiff/)             | 结果集 | multiple | ELEMENT 字段，STRING 类型                         |
| [SDIFFSTORE](https://redis.io/docs/latest/commands/sdiffstore/)   | 结果集 | 1        | RESULT 字段，LONG 类型                            |
| [SINTER](https://redis.io/docs/latest/commands/sinter/)           | 结果集 | multiple | ELEMENT 字段，STRING 类型                         |
| [SINTERCARD](https://redis.io/docs/latest/commands/sintercard/)   | 结果集 | 1        | RESULT 字段，LONG 类型                            |
| [SINTERSTORE](https://redis.io/docs/latest/commands/sinterstore/) | 结果集 | 1        | RESULT 字段，LONG 类型                            |
| [SISMEMBER](https://redis.io/docs/latest/commands/sismember/)     | 结果集 | 1        | RESULT 字段，LONG 类型                            |
| [SMISMEMBER](https://redis.io/docs/latest/commands/smismember/)   | 结果集 | multiple | RESULT 字段，BOOLEAN 类型                         |
| [SMEMBERS](https://redis.io/docs/latest/commands/smembers/)       | 结果集 | multiple | ELEMENT 字段，STRING 类型                         |
| [SMOVE](https://redis.io/docs/latest/commands/smove/)             | 结果集 | 1        | RESULT 字段，LONG 类型                            |
| [SPOP](https://redis.io/docs/latest/commands/spop/)               | 结果集 | multiple | ELEMENT 字段，STRING 类型                         |
| [SRANDMEMBER](https://redis.io/docs/latest/commands/srandmember/) | 结果集 | multiple | ELEMENT 字段，STRING 类型                         |
| [SREM](https://redis.io/docs/latest/commands/srem/)               | 结果集 | 1        | RESULT 字段，LONG 类型                            |
| [SSCAN](https://redis.io/docs/latest/commands/sscan/)             | 结果集 | multiple | CURSOR 字段，STRING 类型<br/>ELEMENT 字段，STRING 类型 |
| [SUNION](https://redis.io/docs/latest/commands/sunion/)           | 结果集 | multiple | ELEMENT 字段，STRING 类型                         |
| [SUNIONSTORE](https://redis.io/docs/latest/commands/sunionstore/) | 结果集 | 1        | RESULT 字段，LONG 类型                            |

## StoreSet 命令集 {#storeset}

| 命令                                                                          | 返回值 | 行数       | 结果                                                                  |
|-----------------------------------------------------------------------------|-----|----------|---------------------------------------------------------------------|
| [ZMPOP](https://redis.io/docs/latest/commands/zmpop/)                       | 结果集 | multiple | KEY 字段，STRING 类型                                                    |
| [BZMPOP](https://redis.io/docs/latest/commands/bzmpop/)                     | 结果集 | multiple | KEY 字段，STRING 类型                                                    |
| [ZPOPMAX](https://redis.io/docs/latest/commands/zpopmax/)                   | 结果集 | multiple | SCORE 字段，DOUBLE 类型<br/>ELEMENT 字段，STRING 类型                         |
| [BZPOPMAX](https://redis.io/docs/latest/commands/bzpopmax/)                 | 结果集 | 1        | KEY 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型<br/>ELEMENT 字段，STRING 类型    |
| [ZPOPMIN](https://redis.io/docs/latest/commands/zpopmin/)                   | 结果集 | multiple | SCORE 字段，DOUBLE 类型<br/>ELEMENT 字段，STRING 类型                         |
| [BZPOPMIN](https://redis.io/docs/latest/commands/bzpopmin/)                 | 结果集 | 1        | KEY 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型<br/>ELEMENT 字段，STRING 类型    |
| [ZADD](https://redis.io/docs/latest/commands/zadd/)                         | 结果集 | 1        | RESULT 字段，DOUBLE 类型（当使用 INCR 时）<br/>RESULT 字段，LONG 类型（当不使用 INCR 时）  |
| [ZCARD](https://redis.io/docs/latest/commands/zcard/)                       | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZCOUNT](https://redis.io/docs/latest/commands/zcount/)                     | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZDIFF](https://redis.io/docs/latest/commands/zdiff/)                       | 结果集 | multiple | SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）<br/>ELEMENT 字段，STRING 类型       |
| [ZDIFFSTORE](https://redis.io/docs/latest/commands/zdiffstore/)             | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZINCRBY](https://redis.io/docs/latest/commands/zincrby/)                   | 结果集 | 1        | SCORE 字段，DOUBLE 类型                                                  |
| [ZINTER](https://redis.io/docs/latest/commands/zinter/)                     | 结果集 | multiple | SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）<br/>ELEMENT 字段，STRING 类型       |
| [ZINTERCARD](https://redis.io/docs/latest/commands/zintercard/)             | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZINTERSTORE](https://redis.io/docs/latest/commands/zinterstore/)           | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZLEXCOUNT](https://redis.io/docs/latest/commands/zlexcount/)               | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZSCORE](https://redis.io/docs/latest/commands/zscore/)                     | 结果集 | 1        | SCORE 字段，DOUBLE 类型                                                  |
| [ZMSCORE](https://redis.io/docs/latest/commands/zmscore/)                   | 结果集 | multiple | SCORE 字段，DOUBLE 类型                                                  |
| [ZRANDMEMBER](https://redis.io/docs/latest/commands/zrandmember/)           | 结果集 | multiple | SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）<br/>ELEMENT 字段，STRING 类型       |
| [ZRANGE](https://redis.io/docs/latest/commands/zrange/)                     | 结果集 | multiple | SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）<br/>ELEMENT 字段，STRING 类型       |
| [ZRANGEBYLEX](https://redis.io/docs/latest/commands/zrangebylex/)           | 结果集 | multiple | ELEMENT 字段，STRING 类型                                                |
| [ZRANGEBYSCORE](https://redis.io/docs/latest/commands/zrangebyscore/)       | 结果集 | multiple | SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）<br/>ELEMENT 字段，STRING 类型       |
| [ZRANGESTORE](https://redis.io/docs/latest/commands/zrangestore/)           | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZRANK](https://redis.io/docs/latest/commands/zrank/)                       | 结果集 | 1        | SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）<br/> RANK 字段，LONG 类型           |
| [ZREVRANK](https://redis.io/docs/latest/commands/zrevrank/)                 | 结果集 | 1        | SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）<br/> RANK 字段，LONG 类型           |
| [ZREM](https://redis.io/docs/latest/commands/zrem/)                         | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZREMRANGEBYLEX](https://redis.io/docs/latest/commands/zremrangebylex/)     | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZREMRANGEBYRANK](https://redis.io/docs/latest/commands/zremrangebyrank/)   | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZREMRANGEBYSCORE](https://redis.io/docs/latest/commands/zremrangebyscore/) | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [ZREVRANGE](https://redis.io/docs/latest/commands/zrevrange/)               | 结果集 | multiple | SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）<br/>ELEMENT 字段，STRING 类型       |
| [ZREVRANGEBYLEX](https://redis.io/docs/latest/commands/zrevrangebylex/)     | 结果集 | multiple | ELEMENT 字段，STRING 类型                                                |
| [ZREVRANGEBYSCORE](https://redis.io/docs/latest/commands/zrevrangebyscore/) | 结果集 | multiple | SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）<br/>ELEMENT 字段，STRING 类型       |
| [ZSCAN](https://redis.io/docs/latest/commands/zscan/)                       | 结果集 | multiple | CURSOR 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型<br/>ELEMENT 字段，STRING 类型 |
| [ZUNION](https://redis.io/docs/latest/commands/zunion/)                     | 结果集 | multiple | SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时）<br/>ELEMENT 字段，STRING 类型       |
| [ZUNIONSTORE](https://redis.io/docs/latest/commands/zunionstore/)           | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |

## String 命令集 {#string}

| 命令                                                          | 返回值 | 行数       | 结果                                                                |
|-------------------------------------------------------------|-----|----------|-------------------------------------------------------------------|
| [SET](https://redis.io/docs/latest/commands/set/)           | 结果集 | 1        | RESULT 字段，STRING 类型（当不使用 GET 时）<br/>VALUE 字段，STRING 类型（当使用 GET 时） |
| [GET](https://redis.io/docs/latest/commands/get/)           | 结果集 | 1        | VALUE 字段，STRING 类型                                                |
| [INCR](https://redis.io/docs/latest/commands/incr/)         | 结果集 | 1        | VALUE 字段，LONG 类型                                                  |
| [INCRBY](https://redis.io/docs/latest/commands/incrby/)     | 结果集 | 1        | VALUE 字段，LONG 类型                                                  |
| [DECR](https://redis.io/docs/latest/commands/decr/)         | 结果集 | 1        | VALUE 字段，LONG 类型                                                  |
| [DECRBY](https://redis.io/docs/latest/commands/decrby/)     | 结果集 | 1        | VALUE 字段，LONG 类型                                                  |
| [APPEND](https://redis.io/docs/latest/commands/append/)     | 结果集 | 1        | RESULT 字段，LONG 类型                                                 |
| [GETDEL](https://redis.io/docs/latest/commands/getdel/)     | 结果集 | 1        | VALUE 字段，STRING 类型                                                |
| [GETEX](https://redis.io/docs/latest/commands/getex/)       | 结果集 | 1        | VALUE 字段，STRING 类型                                                |
| [GETRANGE](https://redis.io/docs/latest/commands/getrange/) | 结果集 | 1        | VALUE 字段，STRING 类型                                                |
| [GETSET](https://redis.io/docs/latest/commands/getset/)     | 结果集 | 1        | VALUE 字段，STRING 类型                                                |
| [MGET](https://redis.io/docs/latest/commands/mget/)         | 结果集 | multiple | VALUE 字段，STRING 类型                                                |
| [MSET](https://redis.io/docs/latest/commands/mset/)         | 结果集 | 1        | RESULT 字段，STRING 类型                                               |
| [MSETNX](https://redis.io/docs/latest/commands/msetnx/)     | 结果集 | 1        | RESULT 字段，LONG 类型                                                 |
| [PSETEX](https://redis.io/docs/latest/commands/psetex/)     | 结果集 | 1        | RESULT 字段，STRING 类型                                               |
| [SETEX](https://redis.io/docs/latest/commands/setex/)       | 结果集 | 1        | RESULT 字段，STRING 类型                                               |
| [SETNX](https://redis.io/docs/latest/commands/setnx/)       | 结果集 | 1        | RESULT 字段，LONG 类型                                                 |
| [SETRANGE](https://redis.io/docs/latest/commands/setrange/) | 结果集 | 1        | RESULT 字段，LONG 类型                                                 |
| [STRLEN](https://redis.io/docs/latest/commands/strlen/)     | 结果集 | 1        | RESULT 字段，LONG 类型                                                 |
| [SUBSTR](https://redis.io/docs/latest/commands/substr/)     | 结果集 | 1        | VALUE 字段，STRING 类型                                                |
