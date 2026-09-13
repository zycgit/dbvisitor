---
id: zrangebyscore
sidebar_position: 22
title: ZRANGEBYSCORE
---

:::info[Note]
Official reference: [ZRANGEBYSCORE](https://redis.io/docs/latest/commands/zrangebyscore/).
:::

Read a score range in ascending order.

## Syntax

```text
ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
```

Numeric bounds are inclusive; `(10` excludes 10. `-inf/+inf` mean no lower/upper bound.

`LIMIT offset count` skips offset matching members and returns at most count.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZRANGEBYSCORE demo:ranking 10 20 WITHSCORES LIMIT 0 10
```
