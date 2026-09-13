---
id: zinter
sidebar_position: 13
title: ZINTER
---

:::info[Note]
Official reference: [ZINTER](https://redis.io/docs/latest/commands/zinter/).
:::

Read an intersection and aggregate member scores.

## Syntax

```text
ZINTER numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX] [WITHSCORES]
```

`numkeys` must match the number of following source keys.

`WEIGHTS` supplies one weight per source key, in key order; the default is 1. `AGGREGATE` chooses sum, minimum or maximum, default `SUM`.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZADD demo:other 30 alice
ZINTER 2 demo:ranking demo:other WEIGHTS 1 2 AGGREGATE SUM WITHSCORES
```
