---
id: zlexcount
sidebar_position: 16
title: ZLEXCOUNT
---

:::info[Note]
Official reference: [ZLEXCOUNT](https://redis.io/docs/latest/commands/zlexcount/).
:::

Count members within a lexicographical range.

## Syntax

```text
ZLEXCOUNT key min max
```

Members must have equal scores. `[value` is inclusive and `(value` exclusive; `-` and `+` mean unbounded lower and upper limits.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
ZADD demo:letters 0 alice 0 bob
ZLEXCOUNT demo:letters [a [z
```
