---
id: string
sidebar_position: 7
title: String 命令集
---


| 命令                                                          | 返回值   | 行数       | 结果                                                                         |
|-------------------------------------------------------------|-------|----------|----------------------------------------------------------------------------|
| [SET](https://redis.io/docs/latest/commands/set/)           | 值/结果集 | --/1     | 值：当不使用 GET 时，返回 0（表示没有设置） 或 1（表示设置成功）<br/>结果集：当使用 GET 时，VALUE 字段，STRING 类型 |
| [GET](https://redis.io/docs/latest/commands/get/)           | 结果集   | 1        | VALUE 字段，STRING 类型                                                         |
| [INCR](https://redis.io/docs/latest/commands/incr/)         | 结果集   | 1        | VALUE 字段，LONG 类型                                                           |
| [INCRBY](https://redis.io/docs/latest/commands/incrby/)     | 结果集   | 1        | VALUE 字段，LONG 类型                                                           |
| [DECR](https://redis.io/docs/latest/commands/decr/)         | 结果集   | 1        | VALUE 字段，LONG 类型                                                           |
| [DECRBY](https://redis.io/docs/latest/commands/decrby/)     | 结果集   | 1        | VALUE 字段，LONG 类型                                                           |
| [APPEND](https://redis.io/docs/latest/commands/append/)     | 结果集   | 1        | RESULT 字段，LONG 类型                                                          |
| [GETDEL](https://redis.io/docs/latest/commands/getdel/)     | 结果集   | 1        | VALUE 字段，STRING 类型                                                         |
| [GETEX](https://redis.io/docs/latest/commands/getex/)       | 结果集   | 1        | VALUE 字段，STRING 类型                                                         |
| [GETRANGE](https://redis.io/docs/latest/commands/getrange/) | 结果集   | 1        | VALUE 字段，STRING 类型                                                         |
| [GETSET](https://redis.io/docs/latest/commands/getset/)     | 结果集   | 1        | VALUE 字段，STRING 类型                                                         |
| [MGET](https://redis.io/docs/latest/commands/mget/)         | 结果集   | multiple | KEY 字段，STRING 类型<br/>VALUE 字段，STRING 类型                                    |
| [MSET](https://redis.io/docs/latest/commands/mset/)         | 值     | --       | 添加的键数量。                                                                    |
| [MSETNX](https://redis.io/docs/latest/commands/msetnx/)     | 值     | --       | 如果没有设置任何键（至少有一个键已存在），则返回 0；如果所有键都已设置，则返回键数量。                               |
| [PSETEX](https://redis.io/docs/latest/commands/psetex/)     | 值     | --       | 如果操作成功，则返回 1；否则返回 0。（当状态为 “OK” 时，表示操作成功）                                   |
| [SETEX](https://redis.io/docs/latest/commands/setex/)       | 值     | --       | 如果操作成功，则返回 1；否则返回 0。（当状态为 “OK” 时，表示操作成功）                                   |
| [SETNX](https://redis.io/docs/latest/commands/setnx/)       | 值     | --       | 如果键被设置，则为 1，否则为 0                                                          |
| [SETRANGE](https://redis.io/docs/latest/commands/setrange/) | 值     | --       | 命令修改后字符串的长度。                                                               |
| [STRLEN](https://redis.io/docs/latest/commands/strlen/)     | 结果集   | 1        | RESULT 字段，LONG 类型                                                          |
| [SUBSTR](https://redis.io/docs/latest/commands/substr/)     | 结果集   | 1        | VALUE 字段，STRING 类型                                                         |
