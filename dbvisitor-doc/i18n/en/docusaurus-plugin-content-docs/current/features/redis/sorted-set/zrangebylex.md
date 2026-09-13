---
id: zrangebylex
sidebar_position: 21
title: ZRANGEBYLEX
---

:::info[Note]
Official reference: [ZRANGEBYLEX](https://redis.io/docs/latest/commands/zrangebylex/).
:::

Read a lexicographical range in ascending order.

## Syntax

```text
ZRANGEBYLEX key min max [LIMIT offset count]
```

Members must have equal scores. `[value` is inclusive and `(value` exclusive; `-` and `+` mean unbounded lower and upper limits.

`LIMIT offset count` skips offset matching members and returns at most count.

[ZRANGE ... BYLEX](zrange.md) is also available.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type |

## Example

```text
ZADD demo:letters 0 alice 0 bob
ZRANGEBYLEX demo:letters [a [z LIMIT 0 10
```
