---
id: zrangestore
sidebar_position: 23
title: ZRANGESTORE
---

:::info[Note]
Official reference: [ZRANGESTORE](https://redis.io/docs/latest/commands/zrangestore/).
:::

Store members and scores from a range at the destination.

## Syntax

```text
ZRANGESTORE destination source start stop [BYSCORE | BYLEX] [REV] [LIMIT offset count]
```

The result overwrites the destination; an empty result removes it.

By default, use zero-based inclusive ranks, with negative indices counting from the end. `BYSCORE` uses scores; `BYLEX` uses lexicographical order (equal-score members). `REV` reverses traversal and requires high-to-low bounds. `LIMIT offset count` applies to score or lexicographical ranges.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of elements in the resulting sorted set. |

:::caution[Caution]
Currently `BYSCORE` converts bounds to numbers and does not support exclusive bounds such as `(10`. Use [ZRANGEBYSCORE](zrangebyscore.md) to read exclusive ranges; ZRANGESTORE has no equivalent single-command workaround.
:::

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZRANGESTORE demo:top demo:ranking 0 9 REV
```
