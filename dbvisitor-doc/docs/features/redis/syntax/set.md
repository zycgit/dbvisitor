---
id: set
sidebar_position: 5
title: Set 命令集
---


| 命令                                                                | 返回值 | 行数       | 结果                                           |
|-------------------------------------------------------------------|-----|----------|----------------------------------------------|
| [SADD](https://redis.io/docs/latest/commands/sadd/)               | 值   | --       | 添加到集合中的元素数量，不包括集合中已有的元素。                     |
| [SCARD](https://redis.io/docs/latest/commands/scard/)             | 结果集 | 1        | RESULT 字段，LONG 类型                            |
| [SDIFF](https://redis.io/docs/latest/commands/sdiff/)             | 结果集 | multiple | ELEMENT 字段，STRING 类型                         |
| [SDIFFSTORE](https://redis.io/docs/latest/commands/sdiffstore/)   | 值   | --       | 结果集合中的元素数量。                                  |
| [SINTER](https://redis.io/docs/latest/commands/sinter/)           | 结果集 | multiple | ELEMENT 字段，STRING 类型                         |
| [SINTERCARD](https://redis.io/docs/latest/commands/sintercard/)   | 结果集 | 1        | RESULT 字段，LONG 类型                            |
| [SINTERSTORE](https://redis.io/docs/latest/commands/sinterstore/) | 值   | --       | 结果集合中的元素数量。                                  |
| [SISMEMBER](https://redis.io/docs/latest/commands/sismember/)     | 结果集 | 1        | RESULT 字段，LONG 类型                            |
| [SMISMEMBER](https://redis.io/docs/latest/commands/smismember/)   | 结果集 | multiple | RESULT 字段，BOOLEAN 类型                         |
| [SMEMBERS](https://redis.io/docs/latest/commands/smembers/)       | 结果集 | multiple | ELEMENT 字段，STRING 类型                         |
| [SMOVE](https://redis.io/docs/latest/commands/smove/)             | 值   | --       | 如果元素被移动，则为 1；未被移动则为 0                        |
| [SPOP](https://redis.io/docs/latest/commands/spop/)               | 结果集 | multiple | ELEMENT 字段，STRING 类型                         |
| [SRANDMEMBER](https://redis.io/docs/latest/commands/srandmember/) | 结果集 | multiple | ELEMENT 字段，STRING 类型                         |
| [SREM](https://redis.io/docs/latest/commands/srem/)               | 值   | --       | 从集合中移除的成员数量，不包括不存在的成员。                       |
| [SSCAN](https://redis.io/docs/latest/commands/sscan/)             | 结果集 | multiple | CURSOR 字段，STRING 类型<br/>ELEMENT 字段，STRING 类型 |
| [SUNION](https://redis.io/docs/latest/commands/sunion/)           | 结果集 | multiple | ELEMENT 字段，STRING 类型                         |
| [SUNIONSTORE](https://redis.io/docs/latest/commands/sunionstore/) | 值   | --       | 结果集合中的元素数量。                                  |
