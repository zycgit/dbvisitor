---
id: hscan
sidebar_position: 22
title: HSCAN
---

:::info[说明]
官方文档：[HSCAN](https://redis.io/docs/latest/commands/hscan/)。
:::

按游标读取一批字段和值。

## 语法

```text
HSCAN key cursor [MATCH pattern] [COUNT count] [NOVALUES]
```

`cursor` 首次传 `0`；`MATCH` 按字段名过滤，`COUNT` 是扫描工作量提示，`NOVALUES` 只返回字段名（需要 Redis 7.4+）。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | CURSOR 字段，STRING 类型<br/>FIELD 字段，STRING 类型<br/>VALUE 字段，STRING 类型（不使用 NOVALUES 时） |

:::caution[注意]
当前游标附在每条结果行上，空批次无法读取后续游标。需要保证完整遍历时使用 Redis 客户端；不要把空批次当作遍历完成。
:::

## 示例

```text
HSET demo:user name mali age 18
HSCAN demo:user 0 MATCH n* COUNT 10
```
