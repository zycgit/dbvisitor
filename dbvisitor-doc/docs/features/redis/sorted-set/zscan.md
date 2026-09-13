---
id: zscan
sidebar_position: 33
title: ZSCAN
---

:::info[说明]
官方文档：[ZSCAN](https://redis.io/docs/latest/commands/zscan/)。
:::

按游标读取一批成员和分数。

## 语法

```text
ZSCAN key cursor [MATCH pattern] [COUNT count]
```

首次 `cursor` 传 `0`；`MATCH` 按成员过滤，`COUNT` 是扫描工作量提示，不是固定页大小。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | CURSOR 字段，STRING 类型<br/>ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型 |

:::caution[注意]
当前游标附在每条结果行上，空批次无法读取后续游标。需要保证完整遍历时使用 Redis 客户端；不要把空批次当作遍历完成。
:::

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZSCAN demo:ranking 0 MATCH a* COUNT 10
```
