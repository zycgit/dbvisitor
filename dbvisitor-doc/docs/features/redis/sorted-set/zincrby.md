---
id: zincrby
sidebar_position: 12
title: ZINCRBY
---

:::info[说明]
官方文档：[ZINCRBY](https://redis.io/docs/latest/commands/zincrby/)。
:::

增加指定成员的分数。

## 语法

```text
ZINCRBY key increment member
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | SCORE 字段，DOUBLE 类型 |

:::caution[注意]
当前驱动将增量按整数读取，不适合小数增量。需要小数时可使用 [ZADD ... INCR](zadd.md)，例如 `ZADD demo:ranking INCR 0.5 alice`。
:::

## 示例

```text
ZADD demo:ranking 10 alice
ZINCRBY demo:ranking 5 alice
```
