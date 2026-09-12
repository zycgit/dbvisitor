---
id: keys
sidebar_position: 2
title: Keys 命令集
---


| 命令                                                                        | 返回值 | 行数       | 结果                                              |
|---------------------------------------------------------------------------|-----|----------|-------------------------------------------------|
| [COPY](https://redis.io/docs/latest/commands/COPY/)                       | 值   | --       | 如果操作成功，则为 1；如果操作失败，则为 0                         |
| [DEL](https://redis.io/docs/latest/commands/del/)                         | 值   | --       | 如果移除一个或多个键，则为大于0的整数；如果指定的键都不存在，则为 0             |
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
| [PERSIST](https://redis.io/docs/latest/commands/persist/)                 | 值   | --       | 成功移除键的过期时间返回 1；键不存在或没有过期时间返回 0              |
| [TTL](https://redis.io/docs/latest/commands/ttl/)                         | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [PTTL](https://redis.io/docs/latest/commands/pttl/)                       | 结果集 | 1        | RESULT 字段，LONG 类型                               |
| [RANDOMKEY](https://redis.io/docs/latest/commands/randomkey/)             | 结果集 | 1        | KEY 字段，STRING 类型                                |
| [RENAME](https://redis.io/docs/latest/commands/rename/)                   | 值   | --       | 成功返回 1；目标键已存在时会被覆盖，源键不存在时报错              |
| [RENAMENX](https://redis.io/docs/latest/commands/renamenx/)               | 值   | --       | 1 表示键已重命名，0 表示目标键已存在.                           |
| [SCAN](https://redis.io/docs/latest/commands/scan/)                       | 结果集 | multiple | CURSOR 字段，STRING 类型<br/>KEY 字段，STRING 类型        |
| [TOUCH](https://redis.io/docs/latest/commands/touch/)                     | 值   | --       | 被 TOUCH 的键的数量。                                  |
| [TYPE](https://redis.io/docs/latest/commands/type/)                       | 结果集 | 1        | RESULT 字段，STRING 类型                             |
