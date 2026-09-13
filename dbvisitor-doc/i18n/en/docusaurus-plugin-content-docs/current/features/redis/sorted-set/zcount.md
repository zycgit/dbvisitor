---
id: zcount
sidebar_position: 9
title: ZCOUNT
---

:::info[Note]
Official reference: [ZCOUNT](https://redis.io/docs/latest/commands/zcount/).
:::

Count members within a score range.

## Syntax

```text
ZCOUNT key min max
```

Numeric bounds are inclusive; `(10` excludes 10. `-inf/+inf` mean no lower/upper bound.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZCOUNT demo:ranking 10 20
```
