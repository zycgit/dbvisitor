---
id: zrange
sidebar_position: 20
title: ZRANGE
---

:::info[Note]
Official reference: [ZRANGE](https://redis.io/docs/latest/commands/zrange/).
:::

Read a range by rank, score or lexicographical order.

## Syntax

```text
ZRANGE key start stop [BYSCORE | BYLEX] [REV] [LIMIT offset count] [WITHSCORES]
```

By default, use zero-based inclusive ranks, with negative indices counting from the end. `BYSCORE` uses scores; `BYLEX` uses lexicographical order (equal-score members). `REV` reverses traversal and requires high-to-low bounds. `LIMIT offset count` applies to score or lexicographical ranges.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |

:::caution[Caution]
Currently `BYSCORE` converts bounds to numbers and does not support exclusive bounds such as `(10`. Use [ZRANGEBYSCORE](zrangebyscore.md) to read exclusive ranges; ZRANGESTORE has no equivalent single-command workaround.
:::

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZRANGE demo:ranking 0 9 WITHSCORES
ZRANGE demo:ranking 10 20 BYSCORE LIMIT 0 10 WITHSCORES
```
