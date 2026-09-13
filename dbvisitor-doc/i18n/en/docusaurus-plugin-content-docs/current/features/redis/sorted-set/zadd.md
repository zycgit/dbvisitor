---
id: zadd
sidebar_position: 7
title: ZADD
---

:::info[Note]
Official reference: [ZADD](https://redis.io/docs/latest/commands/zadd/).
:::

Add members or update their scores.

## Syntax

```text
ZADD key [NX | XX] [GT | LT] [CH] [INCR] score member [score member ...]
```

`NX/XX` add only/update only; `GT/LT` only increase/decrease existing scores. Do not combine `NX` with `GT/LT`. `CH` also counts score changes. `INCR` accepts one score-member pair and returns the new score.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, DOUBLE type (when using INCR)<br/>RESULT field, LONG type (when not using INCR) |

:::caution[Caution]
If an `INCR` condition is not met, the current driver can fail instead of returning null normally.
:::

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZADD demo:ranking XX GT CH 30 bob
```
