---
id: zunion
sidebar_position: 34
title: ZUNION
---

:::info[Note]
Official reference: [ZUNION](https://redis.io/docs/latest/commands/zunion/).
:::

Read a union and aggregate scores of shared members.

## Syntax

```text
ZUNION numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX] [WITHSCORES]
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
ZUNION 2 demo:ranking demo:other WEIGHTS 1 2 AGGREGATE MAX WITHSCORES
```
