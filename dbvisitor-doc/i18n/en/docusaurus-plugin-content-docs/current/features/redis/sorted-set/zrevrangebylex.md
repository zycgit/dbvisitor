---
id: zrevrangebylex
sidebar_position: 31
title: ZREVRANGEBYLEX
---

:::info[Note]
Official reference: [ZREVRANGEBYLEX](https://redis.io/docs/latest/commands/zrevrangebylex/).
:::

Read a lexicographical range in descending order.

## Syntax

```text
ZREVRANGEBYLEX key max min [LIMIT offset count]
```

Members must have equal scores. `[value` is inclusive and `(value` exclusive; `-` and `+` mean unbounded lower and upper limits.

Bounds are ordered `max min`. `LIMIT offset count` skips offset matching members and returns at most count.

[ZRANGE ... BYLEX REV](zrange.md) is also available.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type |

## Example

```text
ZADD demo:letters 0 alice 0 bob
ZREVRANGEBYLEX demo:letters [z [a LIMIT 0 10
```
