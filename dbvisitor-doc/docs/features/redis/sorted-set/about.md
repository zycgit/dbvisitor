---
id: about
slug: /features/redis/commands/sorted-set
sidebar_position: 0
hide_table_of_contents: true
title: Sorted Set 有序集合
---

选择命令查看语法、参数、返回结果与示例。执行方式见[查询操作](../dbvisitor/query.mdx)和[数据写入](../dbvisitor/write.mdx)，参数占位符见[命令格式与参数](../basics/commands.md)。

## 成员与分数

[ZADD](zadd.md) · [ZINCRBY](zincrby.md) · [ZREM](zrem.md)<br />
[ZSCORE](zscore.md) · [ZMSCORE](zmscore.md) · [ZCARD](zcard.md)

## 排名与范围查询

[ZRANK](zrank.md) · [ZREVRANK](zrevrank.md)<br />
[ZRANGE](zrange.md) · [ZREVRANGE](zrevrange.md) · [ZRANGESTORE](zrangestore.md)<br />
[ZRANGEBYSCORE](zrangebyscore.md) · [ZREVRANGEBYSCORE](zrevrangebyscore.md) · [ZCOUNT](zcount.md)<br />
[ZRANGEBYLEX](zrangebylex.md) · [ZREVRANGEBYLEX](zrevrangebylex.md) · [ZLEXCOUNT](zlexcount.md)

## 随机读取与遍历

[ZRANDMEMBER](zrandmember.md) · [ZSCAN](zscan.md)

## 弹出成员

[ZPOPMIN](zpopmin.md) · [ZPOPMAX](zpopmax.md) · [ZMPOP](zmpop.md)

## 阻塞弹出

[BZPOPMIN](bzpopmin.md) · [BZPOPMAX](bzpopmax.md) · [BZMPOP](bzmpop.md)

## 范围删除

[ZREMRANGEBYRANK](zremrangebyrank.md) · [ZREMRANGEBYSCORE](zremrangebyscore.md) · [ZREMRANGEBYLEX](zremrangebylex.md)

## 交集、并集与差集

[ZINTER](zinter.md) · [ZINTERCARD](zintercard.md) · [ZINTERSTORE](zinterstore.md)<br />
[ZUNION](zunion.md) · [ZUNIONSTORE](zunionstore.md)<br />
[ZDIFF](zdiff.md) · [ZDIFFSTORE](zdiffstore.md)
