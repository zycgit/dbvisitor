---
id: zremrangebyscore
sidebar_position: 29
title: ZREMRANGEBYSCORE
---

:::info[Note]
Official reference: [ZREMRANGEBYSCORE](https://redis.io/docs/latest/commands/zremrangebyscore/).
:::

Remove members in a score range.

## Syntax

```text
ZREMRANGEBYSCORE key min max
```

Numeric bounds are inclusive; `(10` excludes 10. `-inf/+inf` mean no lower/upper bound.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of members removed from the sorted set, not including non-existing members. |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZREMRANGEBYSCORE demo:ranking 0 10
```
