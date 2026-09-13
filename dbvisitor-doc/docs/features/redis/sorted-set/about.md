---
id: about
slug: /features/redis/commands/sorted-set
sidebar_position: 0
hide_table_of_contents: true
title: Sorted Set 有序集合
---

选择命令查看语法、参数、返回结果与示例。执行方式见[数据读写](../dbvisitor/usage.mdx)，参数占位符见[命令格式与参数](../basics/commands.md)。

| 命令 | 用途 |
| --- | --- |
| [ZMPOP](zmpop.md) | 从第一个非空有序集合弹出低分或高分成员。 |
| [BZMPOP](bzmpop.md) | 等待有序集合有成员后弹出。 |
| [ZPOPMAX](zpopmax.md) | 弹出分数最高的成员。 |
| [BZPOPMAX](bzpopmax.md) | 等待成员后，从第一个非空有序集合弹出最高分成员。 |
| [ZPOPMIN](zpopmin.md) | 弹出分数最低的成员。 |
| [BZPOPMIN](bzpopmin.md) | 等待成员后，从第一个非空有序集合弹出最低分成员。 |
| [ZADD](zadd.md) | 添加成员或修改分数。 |
| [ZCARD](zcard.md) | 读取有序集合的成员数量。 |
| [ZCOUNT](zcount.md) | 按分数范围统计成员数量。 |
| [ZDIFF](zdiff.md) | 读取第一个有序集合与其他集合的差集。 |
| [ZDIFFSTORE](zdiffstore.md) | 将有序集合差集保存到目标键。 |
| [ZINCRBY](zincrby.md) | 增加指定成员的分数。 |
| [ZINTER](zinter.md) | 读取交集，并汇总成员分数。 |
| [ZINTERCARD](zintercard.md) | 统计交集成员数量，可设置计数上限。 |
| [ZINTERSTORE](zinterstore.md) | 将交集和汇总后的分数保存到目标键。 |
| [ZLEXCOUNT](zlexcount.md) | 按字典序范围统计成员数量。 |
| [ZSCORE](zscore.md) | 读取一个成员的分数。 |
| [ZMSCORE](zmscore.md) | 依次读取多个成员的分数。 |
| [ZRANDMEMBER](zrandmember.md) | 随机读取成员，可同时返回分数。 |
| [ZRANGE](zrange.md) | 按排名、分数或字典序读取范围。 |
| [ZRANGEBYLEX](zrangebylex.md) | 按字典序升序读取范围。 |
| [ZRANGEBYSCORE](zrangebyscore.md) | 按分数升序读取范围。 |
| [ZRANGESTORE](zrangestore.md) | 将范围内的成员及分数保存到目标键。 |
| [ZRANK](zrank.md) | 读取成员的升序排名。 |
| [ZREVRANK](zrevrank.md) | 读取成员的降序排名。 |
| [ZREM](zrem.md) | 删除指定成员。 |
| [ZREMRANGEBYLEX](zremrangebylex.md) | 删除字典序范围内的成员。 |
| [ZREMRANGEBYRANK](zremrangebyrank.md) | 删除排名范围内的成员。 |
| [ZREMRANGEBYSCORE](zremrangebyscore.md) | 删除分数范围内的成员。 |
| [ZREVRANGE](zrevrange.md) | 按降序排名读取范围。 |
| [ZREVRANGEBYLEX](zrevrangebylex.md) | 按字典序降序读取范围。 |
| [ZREVRANGEBYSCORE](zrevrangebyscore.md) | 按分数降序读取范围。 |
| [ZSCAN](zscan.md) | 按游标读取一批成员和分数。 |
| [ZUNION](zunion.md) | 读取并集，并汇总重复成员的分数。 |
| [ZUNIONSTORE](zunionstore.md) | 将并集和汇总分数保存到目标键。 |
