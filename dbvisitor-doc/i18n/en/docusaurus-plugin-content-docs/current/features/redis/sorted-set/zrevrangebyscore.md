---
id: zrevrangebyscore
sidebar_position: 32
title: ZREVRANGEBYSCORE
---

:::info[Note]
Official reference: [ZREVRANGEBYSCORE](https://redis.io/docs/latest/commands/zrevrangebyscore/).
:::

Read a score range in descending order.

## Syntax

```text
ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
```

Numeric bounds are inclusive; `(10` excludes 10. `-inf/+inf` mean no lower/upper bound.

Bounds are ordered `max min`. `LIMIT offset count` skips offset matching members and returns at most count.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZREVRANGEBYSCORE demo:ranking 20 10 WITHSCORES LIMIT 0 10
```
