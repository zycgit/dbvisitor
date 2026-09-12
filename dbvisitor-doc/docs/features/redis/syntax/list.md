---
id: list
sidebar_position: 3
title: List 命令集
---


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
| [LINSERT](https://redis.io/docs/latest/commands/linsert/)       | 值   | --       | 插入操作后列表的长度，当 key 不存在时返回 0;当未找到 pivot 时返回 -1。                        |
| [LLEN](https://redis.io/docs/latest/commands/llen/)             | 结果集 | 1        | RESULT 字段，LONG 类型                                                   |
| [LPOS](https://redis.io/docs/latest/commands/lpos/)             | 结果集 | multiple | RESULT 字段，LONG 类型                                                   |
| [LPUSH](https://redis.io/docs/latest/commands/lpush/)           | 值   | --       | PUSH 操作后列表的长度。                                                      |
| [LPUSHX](https://redis.io/docs/latest/commands/lpushx/)         | 值   | --       | PUSH 操作后列表的长度。                                                      |
| [RPUSH](https://redis.io/docs/latest/commands/rpush/)           | 值   | --       | PUSH 操作后列表的长度。                                                      |
| [RPUSHX](https://redis.io/docs/latest/commands/rpushx/)         | 值   | --       | PUSH 操作后列表的长度。                                                      |
| [LRANGE](https://redis.io/docs/latest/commands/lrange/)         | 结果集 | multiple | ELEMENT 字段，STRING 类型                                                |
| [LREM](https://redis.io/docs/latest/commands/lrem/)             | 值   | --       | 移除的元素数量。                                                            |
| [LSET](https://redis.io/docs/latest/commands/lset/)             | 值   | --       | 操作是否成功，成功返回 1 否则返回 0，当状态为 “OK” 时表示成功                                |
| [LTRIM](https://redis.io/docs/latest/commands/ltrim/)           | 值   | --       | 操作是否成功，成功返回 1 否则返回 0，当状态为 “OK” 时表示成功                                |
