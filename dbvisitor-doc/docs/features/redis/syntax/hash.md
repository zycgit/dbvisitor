---
id: hash
sidebar_position: 1
title: Hash 命令集
---


| 命令                                                                  | 返回值 | 行数       | 结果                                                                                |
|---------------------------------------------------------------------|-----|----------|-----------------------------------------------------------------------------------|
| [HDEL](https://redis.io/docs/latest/commands/hdel/)                 | 值   | --       | 从散列中删除的字段数，不包括指定但不存在的字段。如果key不存在，则将其作为空散列处理，此命令返回 0                               |
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
| [HKEYS](https://redis.io/docs/latest/commands/hkeys/)               | 结果集 | multiple | FIELD 字段，STRING 类型                                                                |
| [HLEN](https://redis.io/docs/latest/commands/hlen/)                 | 结果集 | 1        | RESULT 字段，LONG 类型                                                                 |
| [HMGET](https://redis.io/docs/latest/commands/hmget/)               | 结果集 | multiple | VALUE 字段，STRING 类型                                                                |
| [HSET](https://redis.io/docs/latest/commands/hset/)                 | 值   | --       | 添加的字段数量。                                                                          |
| [HMSET](https://redis.io/docs/latest/commands/hmset/)               | 值   | --       | 添加的字段数量。                                                                          |
| [HSETNX](https://redis.io/docs/latest/commands/hsetnx/)             | 值   | --       | 如果该字段已存在，则返回 0；否则，如果创建了一个新字段，则返回 1。                                               |
| [HPERSIST](https://redis.io/docs/latest/commands/hpersist/)         | 结果集 | multiple | RESULT 字段，LONG 类型                                                                 |
| [HTTL](https://redis.io/docs/latest/commands/httl/)                 | 结果集 | multiple | RESULT 字段，LONG 类型                                                                 |
| [HPTTL](https://redis.io/docs/latest/commands/hpttl/)               | 结果集 | multiple | RESULT 字段，LONG 类型                                                                 |
| [HRANDFIELD](https://redis.io/docs/latest/commands/hrandfield/)     | 结果集 | multiple | FIELD 字段，STRING 类型<br/>VALUE 字段，STRING 类型（当使用 WITHVALUES 时）                       |
| [HSCAN](https://redis.io/docs/latest/commands/hscan/)               | 结果集 | multiple | CURSOR 字段，STRING 类型<br/>FIELD 字段，STRING 类型<br/>VALUE 字段，STRING 类型（不使用 NOVALUES 时） |
| [HSTRLEN](https://redis.io/docs/latest/commands/hstrlen/)           | 结果集 | 1        | RESULT 字段，LONG 类型                                                                 |
| [HVALS](https://redis.io/docs/latest/commands/hvals/)               | 结果集 | multiple | VALUE 字段，STRING 类型                                                                |
