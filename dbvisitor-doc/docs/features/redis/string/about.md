---
id: about
slug: /features/redis/commands/string
sidebar_position: 0
hide_table_of_contents: true
title: String 字符串
---

选择命令查看语法、参数、返回结果与示例。执行方式见[数据读写](../dbvisitor/usage.mdx)，参数占位符见[命令格式与参数](../basics/commands.md)。

| 命令 | 用途 |
| --- | --- |
| [SET](set.md) | 写入字符串，可按键是否存在决定是否覆盖。 |
| [GET](get.md) | 读取字符串。 |
| [INCR](incr.md) | 将整数值加一；键不存在时从零开始。 |
| [INCRBY](incrby.md) | 按指定整数增加计数。 |
| [DECR](decr.md) | 将整数值减一；键不存在时从零开始。 |
| [DECRBY](decrby.md) | 按指定整数减少计数。 |
| [APPEND](append.md) | 在字符串末尾追加文本。 |
| [GETDEL](getdel.md) | 读取字符串并删除键。 |
| [GETEX](getex.md) | 读取字符串，同时设置或移除过期时间。 |
| [GETRANGE](getrange.md) | 按字节位置读取字符串片段。 |
| [GETSET](getset.md) | 写入新值并返回旧值。 |
| [MGET](mget.md) | 读取多个字符串键。 |
| [MSET](mset.md) | 在一条命令中写入多个键值对。 |
| [MSETNX](msetnx.md) | 仅在所有目标键都不存在时写入全部键值对。 |
| [PSETEX](psetex.md) | 写入字符串并设置毫秒过期时间。 |
| [SETEX](setex.md) | 写入字符串并设置秒级过期时间。 |
| [SETNX](setnx.md) | 仅在键不存在时写入字符串。 |
| [SETRANGE](setrange.md) | 从指定字节位置开始覆盖字符串。 |
| [STRLEN](strlen.md) | 读取字符串的字节长度。 |
| [SUBSTR](substr.md) | 按字节位置读取字符串片段，是 GETRANGE 的旧名称。 |
