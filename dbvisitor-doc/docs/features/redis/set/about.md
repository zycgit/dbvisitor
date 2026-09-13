---
id: about
slug: /features/redis/commands/set
sidebar_position: 0
hide_table_of_contents: true
title: Set 集合
---

选择命令查看语法、参数、返回结果与示例。执行方式见[数据读写](../dbvisitor/usage.mdx)，参数占位符见[命令格式与参数](../basics/commands.md)。

| 命令 | 用途 |
| --- | --- |
| [SADD](sadd.md) | 向集合添加不重复的元素。 |
| [SCARD](scard.md) | 读取集合元素数量。 |
| [SDIFF](sdiff.md) | 读取第一个集合中不在其他集合中的元素。 |
| [SDIFFSTORE](sdiffstore.md) | 将集合差集保存到目标键。 |
| [SINTER](sinter.md) | 读取多个集合共有的元素。 |
| [SINTERCARD](sintercard.md) | 读取交集的元素数量，可限制计数上限。 |
| [SINTERSTORE](sinterstore.md) | 将集合交集保存到目标键。 |
| [SISMEMBER](sismember.md) | 判断一个元素是否属于集合。 |
| [SMISMEMBER](smismember.md) | 依次判断多个元素是否属于集合。 |
| [SMEMBERS](smembers.md) | 读取集合的全部元素。 |
| [SMOVE](smove.md) | 将一个元素从源集合移到目标集合。 |
| [SPOP](spop.md) | 随机弹出并删除集合元素。 |
| [SRANDMEMBER](srandmember.md) | 随机读取集合元素，不删除数据。 |
| [SREM](srem.md) | 删除指定集合元素。 |
| [SSCAN](sscan.md) | 按游标读取一批集合元素。 |
| [SUNION](sunion.md) | 读取多个集合的并集。 |
| [SUNIONSTORE](sunionstore.md) | 将集合并集保存到目标键。 |
