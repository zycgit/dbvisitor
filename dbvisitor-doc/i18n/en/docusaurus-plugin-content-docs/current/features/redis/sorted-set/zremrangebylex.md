---
id: zremrangebylex
sidebar_position: 27
title: ZREMRANGEBYLEX
---

:::info[Note]
Official reference: [ZREMRANGEBYLEX](https://redis.io/docs/latest/commands/zremrangebylex/).
:::

Remove members in a lexicographical range.

## Syntax

```text
ZREMRANGEBYLEX key min max
```

Members must have equal scores. `[value` is inclusive and `(value` exclusive; `-` and `+` mean unbounded lower and upper limits.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of members removed from the sorted set, not including non-existing members. |

## Example

```text
ZADD demo:letters 0 alice 0 bob
ZREMRANGEBYLEX demo:letters [a [b
```
