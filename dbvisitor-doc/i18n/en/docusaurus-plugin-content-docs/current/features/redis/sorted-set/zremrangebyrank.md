---
id: zremrangebyrank
sidebar_position: 28
title: ZREMRANGEBYRANK
---

:::info[Note]
Official reference: [ZREMRANGEBYRANK](https://redis.io/docs/latest/commands/zremrangebyrank/).
:::

Remove members in a rank range.

## Syntax

```text
ZREMRANGEBYRANK key start stop
```

Ranks are zero-based and the end position is inclusive; negative indices count from the end.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of members removed from the sorted set, not including non-existing members. |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZREMRANGEBYRANK demo:ranking 0 0
```
