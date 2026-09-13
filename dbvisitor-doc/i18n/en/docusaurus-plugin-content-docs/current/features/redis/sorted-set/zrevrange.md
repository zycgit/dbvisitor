---
id: zrevrange
sidebar_position: 30
title: ZREVRANGE
---

:::info[Note]
Official reference: [ZREVRANGE](https://redis.io/docs/latest/commands/zrevrange/).
:::

Read a range by descending rank.

## Syntax

```text
ZREVRANGE key start stop [WITHSCORES]
```

Ranks are zero-based and the end position is inclusive; negative indices count from the end.

[ZRANGE ... REV](zrange.md) is also available.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZREVRANGE demo:ranking 0 9 WITHSCORES
```
