---
id: zunionstore
sidebar_position: 35
title: ZUNIONSTORE
---

:::info[Note]
Official reference: [ZUNIONSTORE](https://redis.io/docs/latest/commands/zunionstore/).
:::

Store a union with aggregated scores.

## Syntax

```text
ZUNIONSTORE destination numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX]
```

`numkeys` must match the number of following source keys.

`WEIGHTS` supplies one weight per source key, in key order; the default is 1. `AGGREGATE` chooses sum, minimum or maximum, default `SUM`.

The result overwrites the destination; an empty result removes it.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of elements in the resulting sorted set. |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZADD demo:other 30 alice
ZUNIONSTORE demo:all 2 demo:ranking demo:other WEIGHTS 1 2 AGGREGATE MAX
```
