---
id: zincrby
sidebar_position: 12
title: ZINCRBY
---

:::info[Note]
Official reference: [ZINCRBY](https://redis.io/docs/latest/commands/zincrby/).
:::

Increase a member's score.

## Syntax

```text
ZINCRBY key increment member
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | SCORE field, DOUBLE type |

:::caution[Caution]
The driver currently reads the increment as an integer. For fractional increments, use [ZADD ... INCR](zadd.md), for example `ZADD demo:ranking INCR 0.5 alice`.
:::

## Example

```text
ZADD demo:ranking 10 alice
ZINCRBY demo:ranking 5 alice
```
