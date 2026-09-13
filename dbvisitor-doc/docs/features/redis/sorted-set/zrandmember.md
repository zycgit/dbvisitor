---
id: zrandmember
sidebar_position: 19
title: ZRANDMEMBER
---

:::info[说明]
官方文档：[ZRANDMEMBER](https://redis.io/docs/latest/commands/zrandmember/)。
:::

随机读取成员，可同时返回分数。

## 语法

```text
ZRANDMEMBER key [count [WITHSCORES]]
```

省略 `count` 时随机读取一个成员；正数去重，负数允许重复。`WITHSCORES` 必须跟在 `count` 后面。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时） |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZRANDMEMBER demo:ranking 2 WITHSCORES
```
