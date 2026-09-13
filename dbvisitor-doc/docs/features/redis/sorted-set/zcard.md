---
id: zcard
sidebar_position: 8
title: ZCARD
---

:::info[说明]
官方文档：[ZCARD](https://redis.io/docs/latest/commands/zcard/)。
:::

读取有序集合的成员数量。

## 语法

```text
ZCARD key
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZCARD demo:ranking
```
