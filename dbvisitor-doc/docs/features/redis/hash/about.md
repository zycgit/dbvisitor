---
id: about
slug: /features/redis/commands/hash
sidebar_position: 0
hide_table_of_contents: true
title: Hash 散列
---

选择命令查看语法、参数、返回结果与示例。执行方式见[数据读写](../dbvisitor/usage.mdx)，参数占位符见[命令格式与参数](../basics/commands.md)。

| 命令 | 用途 |
| --- | --- |
| [HDEL](hdel.md) | 删除散列中的字段。 |
| [HEXISTS](hexists.md) | 判断字段是否存在。 |
| [HEXPIRE](hexpire.md) | 为字段设置秒级存活时间。 |
| [HEXPIREAT](hexpireat.md) | 用 Unix 秒时间戳设置字段过期时刻。 |
| [HEXPIRETIME](hexpiretime.md) | 读取字段的秒级过期时间戳。 |
| [HPEXPIRE](hpexpire.md) | 为字段设置毫秒级存活时间。 |
| [HPEXPIREAT](hpexpireat.md) | 用 Unix 毫秒时间戳设置字段过期时刻。 |
| [HPEXPIRETIME](hpexpiretime.md) | 读取字段的毫秒级过期时间戳。 |
| [HGET](hget.md) | 读取一个字段的值。 |
| [HGETALL](hgetall.md) | 读取散列的全部字段和值。 |
| [HINCRBY](hincrby.md) | 按指定整数增加字段值。 |
| [HKEYS](hkeys.md) | 列出散列的字段名。 |
| [HLEN](hlen.md) | 读取字段数量。 |
| [HMGET](hmget.md) | 按指定顺序读取多个字段。 |
| [HSET](hset.md) | 新增或覆盖字段值。 |
| [HMSET](hmset.md) | 一次写入多个字段值。 |
| [HSETNX](hsetnx.md) | 仅在字段不存在时写入。 |
| [HPERSIST](hpersist.md) | 移除字段的过期时间。 |
| [HTTL](httl.md) | 读取字段剩余存活秒数。 |
| [HPTTL](hpttl.md) | 读取字段剩余存活毫秒数。 |
| [HRANDFIELD](hrandfield.md) | 随机读取字段，可同时读取字段值。 |
| [HSCAN](hscan.md) | 按游标读取一批字段和值。 |
| [HSTRLEN](hstrlen.md) | 读取字段值的字节长度。 |
| [HVALS](hvals.md) | 读取全部字段值。 |
