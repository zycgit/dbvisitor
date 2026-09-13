---
id: scan
sidebar_position: 23
title: SCAN
---

:::info[说明]
官方文档：[SCAN](https://redis.io/docs/latest/commands/scan/)。
:::

读取一批键名及后续游标。

## 语法

```text
SCAN cursor [MATCH pattern] [COUNT count] [TYPE type]
```

首次 `cursor` 传 `0`；`MATCH` 过滤键名，`COUNT` 提示扫描工作量，`TYPE` 可填 `string`、`hash`、`list`、`set`、`zset` 等 Redis 类型名。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | CURSOR 字段，STRING 类型<br/>KEY 字段，STRING 类型 |

:::caution[注意]
当前游标附在结果行上，空批次无法读取后续游标；COUNT 还会限制返回行数，可能截断该批结果。不要通过这条路径实现完整遍历。按模式读取键可用 [KEYS](keys.md)，需要自行管理游标时使用 Redis 客户端。
:::

## 示例

```text
SET demo:message hello
SCAN 0 MATCH demo:* COUNT 10 TYPE string
```
