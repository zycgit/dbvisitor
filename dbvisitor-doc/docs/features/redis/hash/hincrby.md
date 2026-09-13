---
id: hincrby
sidebar_position: 11
title: HINCRBY
---

:::info[说明]
官方文档：[HINCRBY](https://redis.io/docs/latest/commands/hincrby/)。
:::

按指定整数增加字段值。

## 语法

```text
HINCRBY key field increment
```

`increment` 是有符号 64 位整数；字段不存在时从 0 开始。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | VALUE 字段，LONG 类型 |

## 示例

```text
HSET demo:user age 18
HINCRBY demo:user age 1
```
